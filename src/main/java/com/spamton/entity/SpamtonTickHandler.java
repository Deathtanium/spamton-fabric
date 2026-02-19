package com.spamton.entity;

import com.spamton.trade.SpamtonTradeGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpamtonTickHandler {

    private static final int PRICE_FLUCTUATION_INTERVAL_TICKS = 20;
    /** Only tick Spamton entities within this range of a player to avoid scanning whole world. */
    private static final double TICK_RANGE = 64.0;
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerLevel world : server.getAllLevels()) {
                tickWorld(world);
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (ServerLevel world : server.getAllLevels()) {
                removeAllMinisAndGlasses(world);
            }
        });
    }

    /**
     * Get Spamton merchants and minis only near players to avoid expensive world-wide entity scan.
     */
    private static void tickWorld(ServerLevel world) {
        List<ServerPlayer> players = world.players();
        if (players.isEmpty()) return;

        long time = world.getGameTime();
        Set<Villager> merchants = new HashSet<>();
        Set<Villager> minis = new HashSet<>();

        for (ServerPlayer player : players) {
            AABB box = new AABB(
                    player.getX() - TICK_RANGE, player.getY() - TICK_RANGE, player.getZ() - TICK_RANGE,
                    player.getX() + TICK_RANGE, player.getY() + TICK_RANGE, player.getZ() + TICK_RANGE
            );
            world.getEntities(EntityType.VILLAGER, box, e -> e.getTags().contains(SpamtonDamageHandler.TAG_SPAMTON_MERCHANT)).forEach(merchants::add);
            world.getEntities(EntityType.VILLAGER, box, e -> e.getTags().contains(SpamtonDamageHandler.TAG_SPAMTON_MINI)).forEach(minis::add);
        }

        for (Villager villager : merchants) {
            // Disable AI while invisible (recovery); re-enable when visible
            boolean inRecovery = SpamtonDamageHandler.isInRecovery(villager);
            villager.setNoAi(inRecovery);

            // Ensure merchant has glasses (respawn if lost after reload)
            SpamtonSpawner.ensureMerchantGlasses(world, villager);
            // Update glasses every tick when in range so they track head smoothly (glasses hidden when owner invisible)
            SpamtonSpawner.updateGlassesPosition(world, villager);

            if (villager.getTradingPlayer() != null) {
                // "The" trade display: new random item every tick
                if (time % PRICE_FLUCTUATION_INTERVAL_TICKS == 0) {
                    villager.setOffers(SpamtonTradeGenerator.generate(world.registryAccess()));
                } else {
                    villager.setOffers(SpamtonTradeGenerator.replaceTheDisplayOnly(villager.getOffers(), world.registryAccess()));
                }
                // Sync updated offers to client so trade screen updates live every tick
                ServerPlayer player = (ServerPlayer) villager.getTradingPlayer();
                if (player.containerMenu instanceof MerchantMenu menu && menu.getOffers() != null) {
                    player.connection.send(new ClientboundMerchantOffersPacket(
                            player.containerMenu.containerId,
                            villager.getOffers(),
                            menu.getTraderLevel(),
                            menu.getTraderXp(),
                            menu.showProgressBar(),
                            menu.canRestock()
                    ));
                }
            }

            Long until = SpamtonDamageHandler.getRecoveryUntil(villager.getUUID());
            if (until != null && time >= until) {
                villager.removeEffect(MobEffects.INVISIBILITY);
                SpamtonDamageHandler.clearRecoveryUntil(villager);
            }

        }

        for (Villager mini : minis) {
            SpamtonSpawner.updateGlassesPosition(world, mini, SpamtonSpawner.HEAD_HEIGHT_MINI, SpamtonSpawner.LENS_OFFSET_MINI, SpamtonSpawner.GLASSES_SCALE_MINI);
            Long explodeAt = SpamtonDamageHandler.getMiniExplodeAt(mini.getUUID());
            if (explodeAt != null && time >= explodeAt) {
                Integer batchSize = SpamtonDamageHandler.getMiniBatchSize(mini.getUUID());
                if (batchSize != null && batchSize > 0) {
                    SpamtonDamageHandler.setNextMiniExplosionVolumeScale(1.0f / batchSize);
                }
                world.explode(mini, mini.getX(), mini.getY(), mini.getZ(), 0.5f, Level.ExplosionInteraction.NONE);
                SpamtonDamageHandler.clearMiniExplodeAt(mini.getUUID());
                SpamtonDamageHandler.clearMiniBatchSize(mini.getUUID());
                SpamtonSpawner.deleteGlasses(world, mini.getUUID());
                mini.discard();
            }
        }
    }

    public static void onWorldLoad(ServerLevel world) {
        removeAllMinisAndGlasses(world);
        // Clear recovery state for merchants that were in recovery when the server shut down
        var dim = world.dimensionType();
        var wb = world.getWorldBorder();
        AABB bounds = new AABB(wb.getMinX(), dim.minY(), wb.getMinZ(), wb.getMaxX(), dim.minY() + dim.height(), wb.getMaxZ());
        List<Villager> loadMerchants = world.getEntities(EntityType.VILLAGER, bounds, e -> e.getTags().contains(SpamtonDamageHandler.TAG_SPAMTON_MERCHANT));
        for (Villager villager : loadMerchants) {
            if (SpamtonDamageHandler.isInRecovery(villager)) {
                villager.removeEffect(MobEffects.INVISIBILITY);
                SpamtonDamageHandler.clearRecoveryUntil(villager);
            }
        }
    }

    /**
     * Kill all mini-spamtons and remove their glasses. Used on server start and world load to handle the edge case
     * where the server was shut down during recovery phase (minis would otherwise persist with no merchant).
     */
    private static void removeAllMinisAndGlasses(ServerLevel world) {
        var dim = world.dimensionType();
        var wb = world.getWorldBorder();
        AABB bounds = new AABB(wb.getMinX(), dim.minY(), wb.getMinZ(), wb.getMaxX(), dim.minY() + dim.height(), wb.getMaxZ());
        List<Villager> toDiscard = world.getEntities(EntityType.VILLAGER, bounds, e -> e.getTags().contains(SpamtonDamageHandler.TAG_SPAMTON_MINI));
        for (Villager e : toDiscard) {
            SpamtonSpawner.deleteGlasses(world, e.getUUID());
            SpamtonDamageHandler.clearMiniExplodeAt(e.getUUID());
            SpamtonDamageHandler.clearMiniBatchSize(e.getUUID());
            e.discard();
        }
    }
}

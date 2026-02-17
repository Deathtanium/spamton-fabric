package com.spamton.entity;

import com.spamton.trade.SpamtonTradeGenerator;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class SpamtonTickHandler {

    private static final int PRICE_FLUCTUATION_INTERVAL_TICKS = 20;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerWorld world : server.getWorlds()) {
                tickWorld(world);
            }
        });
    }

    private static void tickWorld(ServerWorld world) {
        long time = world.getTime();

        for (VillagerEntity villager : world.getEntitiesByClass(VillagerEntity.class, world.getWorldBorder().asBox(), e ->
                e.getScoreboardTags().contains(SpamtonDamageHandler.TAG_SPAMTON_MERCHANT))) {
            SpamtonSpawner.updateGlassesPosition(world, villager);

            // Price fluctuation every 20 ticks (1 second) and "The" icon flip every tick when someone is trading
            if (villager.getCustomer() != null) {
                if (time % PRICE_FLUCTUATION_INTERVAL_TICKS == 0) {
                    villager.setOffersFromServer(SpamtonTradeGenerator.generate());
                } else {
                    villager.setOffersFromServer(SpamtonTradeGenerator.replaceTheDisplayOnly(villager.getOffers()));
                }
            }

            NbtCompound nbt = villager.getNbt();
            if (nbt != null && nbt.contains(SpamtonDamageHandler.TAG_RECOVERY_UNTIL)) {
                long until = nbt.getLong(SpamtonDamageHandler.TAG_RECOVERY_UNTIL);
                if (time >= until) {
                    villager.removeStatusEffect(StatusEffects.INVISIBILITY);
                    NbtCompound copy = villager.getNbt() != null ? villager.getNbt().copy() : new NbtCompound();
                    copy.remove(SpamtonDamageHandler.TAG_RECOVERY_UNTIL);
                    villager.readNbt(copy);
                }
            }
        }

        for (VillagerEntity mini : world.getEntitiesByClass(VillagerEntity.class, world.getWorldBorder().asBox(), e ->
                e.getScoreboardTags().contains(SpamtonDamageHandler.TAG_SPAMTON_MINI))) {
            NbtCompound nbt = mini.getNbt();
            if (nbt != null && nbt.contains(SpamtonDamageHandler.TAG_MINI_EXPLODE_AT)) {
                long explodeAt = nbt.getLong(SpamtonDamageHandler.TAG_MINI_EXPLODE_AT);
                if (time >= explodeAt) {
                    world.createExplosion(mini, mini.getX(), mini.getY(), mini.getZ(), 0.5f, World.ExplosionSourceType.NONE);
                    mini.discard();
                }
            }
        }
    }

    public static void onWorldLoad(ServerWorld world) {
        for (VillagerEntity mini : world.getEntitiesByClass(VillagerEntity.class, world.getWorldBorder().asBox(), e ->
                e.getScoreboardTags().contains(SpamtonDamageHandler.TAG_SPAMTON_MINI))) {
            mini.discard();
        }
        for (VillagerEntity villager : world.getEntitiesByClass(VillagerEntity.class, world.getWorldBorder().asBox(), e ->
                e.getScoreboardTags().contains(SpamtonDamageHandler.TAG_SPAMTON_MERCHANT))) {
            NbtCompound nbt = villager.getNbt();
            if (nbt != null && nbt.contains(SpamtonDamageHandler.TAG_RECOVERY_UNTIL)) {
                villager.removeStatusEffect(StatusEffects.INVISIBILITY);
                NbtCompound copy = villager.getNbt() != null ? villager.getNbt().copy() : new NbtCompound();
                copy.remove(SpamtonDamageHandler.TAG_RECOVERY_UNTIL);
                villager.readNbt(copy);
            }
        }
    }
}

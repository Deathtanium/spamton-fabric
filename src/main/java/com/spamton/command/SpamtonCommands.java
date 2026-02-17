package com.spamton.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.spamton.SpamtonConfig;
import com.spamton.SpamtonMod;
import com.spamton.entity.SpamtonDamageHandler;
import com.spamton.entity.SpamtonSpawner;
import com.spamton.item.KromerItem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SpamtonCommands {

    public static void register() {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> register(dispatcher)
        );
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal(SpamtonMod.MOD_ID)
                        .then(Commands.literal("spawn")
                                .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .executes(ctx -> spawn(ctx.getSource())))
                        .then(Commands.literal("reload")
                                .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .executes(ctx -> reload(ctx.getSource())))
                        .then(Commands.literal("killall")
                                .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .executes(ctx -> killall(ctx.getSource())))
                        .then(Commands.literal("givekromer")
                                .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                                .executes(ctx -> giveKromer(ctx.getSource(), ctx.getSource().getPlayer(), 1))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> giveKromer(ctx.getSource(), ctx.getSource().getPlayer(),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> giveKromer(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"), 1))
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> giveKromer(ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "amount"))))))
        );
    }

    private static int spawn(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            SpamtonSpawner.spawn(player.level(), player.blockPosition().above(), player);
            return 1;
        }
        return 0;
    }

    private static int reload(CommandSourceStack source) {
        SpamtonConfig.load();
        return 1;
    }

    private static int killall(CommandSourceStack source) {
        int total = 0;
        for (ServerLevel world : source.getServer().getAllLevels()) {
            var dim = world.dimensionType();
            var wb = world.getWorldBorder();
            AABB bounds = new AABB(wb.getMinX(), dim.minY(), wb.getMinZ(), wb.getMaxX(), dim.minY() + dim.height(), wb.getMaxZ());
            List<Villager> spamtons = world.getEntities(EntityType.VILLAGER, bounds, e ->
                    e.getTags().contains(SpamtonDamageHandler.TAG_SPAMTON_MERCHANT) || e.getTags().contains(SpamtonDamageHandler.TAG_SPAMTON_MINI));
            for (Villager e : spamtons) {
                SpamtonSpawner.deleteGlasses(world, e.getUUID());
                SpamtonDamageHandler.clearRecoveryUntil(e);
                SpamtonDamageHandler.clearMiniExplodeAt(e.getUUID());
                SpamtonDamageHandler.clearMiniBatchSize(e.getUUID());
                e.discard();
                total++;
            }
        }
        final int killed = total;
        source.sendSuccess(() -> Component.literal("Killed " + killed + " Spamton(s) and removed their display blocks."), true);
        return total;
    }

    private static int giveKromer(CommandSourceStack source, ServerPlayer target, int amount) {
        if (target == null) return 0;
        for (int i = 0; i < amount; i++) {
            target.getInventory().add(KromerItem.createKromer(1));
        }
        return 1;
    }
}

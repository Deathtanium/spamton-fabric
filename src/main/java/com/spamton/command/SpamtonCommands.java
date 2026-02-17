package com.spamton.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.spamton.SpamtonConfig;
import com.spamton.SpamtonMod;
import com.spamton.entity.SpamtonSpawner;
import com.spamton.item.KromerItem;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class SpamtonCommands {

    public static void register() {
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> register(dispatcher)
        );
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal(SpamtonMod.MOD_ID)
                        .then(CommandManager.literal("spawn")
                                .requires(s -> s.hasPermissionLevel(2))
                                .executes(ctx -> spawn(ctx.getSource())))
                        .then(CommandManager.literal("reload")
                                .requires(s -> s.hasPermissionLevel(2))
                                .executes(ctx -> reload(ctx.getSource())))
                        .then(CommandManager.literal("givekromer")
                                .requires(s -> s.hasPermissionLevel(2))
                                .executes(ctx -> giveKromer(ctx.getSource(), ctx.getSource().getPlayer(), 1))
                                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> giveKromer(ctx.getSource(), ctx.getSource().getPlayer(),
                                                IntegerArgumentType.getInteger(ctx, "amount"))))
                                .then(CommandManager.argument("player", net.minecraft.command.argument.EntityArgumentType.player())
                                        .executes(ctx -> giveKromer(ctx.getSource(),
                                                net.minecraft.command.argument.EntityArgumentType.getPlayer(ctx, "player"), 1))
                                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> giveKromer(ctx.getSource(),
                                                        net.minecraft.command.argument.EntityArgumentType.getPlayer(ctx, "player"),
                                                        IntegerArgumentType.getInteger(ctx, "amount"))))))
        );
    }

    private static int spawn(ServerCommandSource source) {
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            SpamtonSpawner.spawn(player.getWorld(), player.getBlockPos().up(), player);
            return 1;
        }
        return 0;
    }

    private static int reload(ServerCommandSource source) {
        SpamtonConfig.load();
        return 1;
    }

    private static int giveKromer(ServerCommandSource source, ServerPlayerEntity target, int amount) {
        if (target == null) return 0;
        for (int i = 0; i < amount; i++) {
            target.getInventory().insertStack(KromerItem.createKromer(1));
        }
        return 1;
    }
}

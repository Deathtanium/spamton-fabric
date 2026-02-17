package com.spamton.entity;

import com.spamton.SpamtonMod;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class SpamtonDamageHandler {

    public static final String TAG_SPAMTON_MERCHANT = "spamton_merchant";
    public static final String TAG_SPAMTON_MINI = "spamton_mini";
    public static final String TAG_RECOVERY_UNTIL = "SpamtonRecoveryUntil";
    public static final String TAG_MINI_EXPLODE_AT = "SpamtonMiniExplodeAt";
    private static final int RECOVERY_TICKS = 1200; // 1 minute
    private static final int MINI_COUNT_MIN = 5;
    private static final int MINI_COUNT_MAX = 10;
    private static final int MINI_EXPLODE_TICKS = 80;
    private static final Random RANDOM = new Random();

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getScoreboardTags().contains(TAG_SPAMTON_MERCHANT)) {
                if (!isInRecovery(entity)) {
                    triggerSpectacle((VillagerEntity) entity);
                }
                return false;
            }
            if (entity.getScoreboardTags().contains(TAG_SPAMTON_MINI)) {
                return false;
            }
            return true;
        });
    }

    private static boolean isInRecovery(net.minecraft.entity.LivingEntity villager) {
        NbtCompound nbt = villager.getNbt();
        if (nbt == null) return false;
        long until = nbt.getLong(TAG_RECOVERY_UNTIL);
        return until > 0 && villager.getWorld().getTime() < until;
    }

    private static void triggerSpectacle(VillagerEntity villager) {
        ServerWorld world = (ServerWorld) villager.getWorld();
        long now = world.getTime();

        int count = MINI_COUNT_MIN + RANDOM.nextInt(MINI_COUNT_MAX - MINI_COUNT_MIN + 1);
        Vec3d pos = villager.getPos();

        for (int i = 0; i < count; i++) {
            VillagerEntity mini = EntityType.VILLAGER.create(world);
            if (mini == null) continue;
            mini.setPosition(pos.x, pos.y, pos.z);
            mini.setBaby(true);
            mini.addScoreboardTag(TAG_SPAMTON_MINI);
            mini.setNoAi(false);
            NbtCompound miniNbt = mini.getNbt() != null ? mini.getNbt().copy() : new NbtCompound();
            miniNbt.putLong(TAG_MINI_EXPLODE_AT, now + MINI_EXPLODE_TICKS);
            mini.readNbt(miniNbt);
            world.spawnEntity(mini);

            double angle = 2 * Math.PI * RANDOM.nextDouble();
            double speed = 0.4 + RANDOM.nextDouble() * 0.3;
            mini.addVelocity(Math.cos(angle) * speed, 0.5, Math.sin(angle) * speed);
        }

        villager.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, RECOVERY_TICKS, 0, false, false));
        NbtCompound nbt = villager.getNbt() != null ? villager.getNbt().copy() : new NbtCompound();
        nbt.putLong(TAG_RECOVERY_UNTIL, now + RECOVERY_TICKS);
        villager.readNbt(nbt);
    }
}

package com.spamton.entity;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class SpamtonDamageHandler {

    public static final String TAG_SPAMTON_MERCHANT = "spamton_merchant";
    public static final String TAG_SPAMTON_MINI = "spamton_mini";
    public static final String TAG_RECOVERY_UNTIL = "SpamtonRecoveryUntil";
    public static final String TAG_MINI_EXPLODE_AT = "SpamtonMiniExplodeAt";
    private static final int RECOVERY_TICKS = 200; // 10 seconds (for debugging)
    private static final int MINI_COUNT_MIN = 5;
    private static final int MINI_COUNT_MAX = 10;
    private static final int MINI_EXPLODE_TICKS = 80;
    private static final Random RANDOM = new Random();

    /** In-memory storage for recovery end time (1.21.11 uses ValueInput/ValueOutput; use Fabric attachments for full persistence). */
    private static final Map<UUID, Long> RECOVERY_UNTIL = new HashMap<>();
    /** In-memory storage for mini explode time. */
    private static final Map<UUID, Long> MINI_EXPLODE_AT = new HashMap<>();
    /** Number of minis in the same spawn batch (for explosion sound volume 1/N). */
    private static final Map<UUID, Integer> MINI_BATCH_SIZE = new HashMap<>();
    /** Volume scale for the next explosion sound (minis); set before explode, consumed by playSound mixin. */
    private static volatile Float nextMiniExplosionVolumeScale = null;

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity.getTags().contains(TAG_SPAMTON_MERCHANT)) {
                // Allow /kill and other command-based death
                if (source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                    return true;
                }
                if (!isInRecovery(entity)) {
                    triggerSpectacle((Villager) entity);
                }
                return false;
            }
            if (entity.getTags().contains(TAG_SPAMTON_MINI)) {
                if (source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                    return true;
                }
                return false;
            }
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity.getTags().contains(TAG_SPAMTON_MERCHANT) || entity.getTags().contains(TAG_SPAMTON_MINI)) {
                if (entity.level() instanceof ServerLevel world) {
                    SpamtonSpawner.deleteGlasses(world, entity.getUUID());
                }
            }
        });
    }

    public static boolean isInRecovery(LivingEntity villager) {
        Long until = RECOVERY_UNTIL.get(villager.getUUID());
        return until != null && until > 0 && villager.level().getGameTime() < until;
    }

    public static void setRecoveryUntil(LivingEntity entity, long gameTime) {
        RECOVERY_UNTIL.put(entity.getUUID(), gameTime);
    }

    public static void clearRecoveryUntil(LivingEntity entity) {
        RECOVERY_UNTIL.remove(entity.getUUID());
    }

    public static Long getRecoveryUntil(java.util.UUID uuid) {
        return RECOVERY_UNTIL.get(uuid);
    }

    public static Long getMiniExplodeAt(UUID uuid) {
        return MINI_EXPLODE_AT.get(uuid);
    }

    public static void setMiniExplodeAt(UUID uuid, long gameTime) {
        MINI_EXPLODE_AT.put(uuid, gameTime);
    }

    public static void clearMiniExplodeAt(UUID uuid) {
        MINI_EXPLODE_AT.remove(uuid);
    }

    public static Integer getMiniBatchSize(UUID uuid) {
        return MINI_BATCH_SIZE.get(uuid);
    }

    public static void clearMiniBatchSize(UUID uuid) {
        MINI_BATCH_SIZE.remove(uuid);
    }

    public static void setNextMiniExplosionVolumeScale(float scale) {
        nextMiniExplosionVolumeScale = scale;
    }

    /** Consumed by mixin when explosion sound is played; returns null after read. */
    public static Float consumeNextMiniExplosionVolumeScale() {
        Float v = nextMiniExplosionVolumeScale;
        nextMiniExplosionVolumeScale = null;
        return v;
    }

    /**
     * Same villager is kept and only made invisible; minis spawn and explode.
     * No new Spamton entity is created—the original reappears when recovery ends.
     */
    private static void triggerSpectacle(Villager villager) {
        ServerLevel world = (ServerLevel) villager.level();
        long now = world.getGameTime();

        int count = MINI_COUNT_MIN + RANDOM.nextInt(MINI_COUNT_MAX - MINI_COUNT_MIN + 1);
        Vec3 pos = villager.position();

        for (int i = 0; i < count; i++) {
            Villager mini = EntityType.VILLAGER.create(world, net.minecraft.world.entity.EntitySpawnReason.NATURAL);
            if (mini == null) continue;
            mini.setPos(pos.x, pos.y, pos.z);
            mini.setBaby(true);
            mini.addTag(TAG_SPAMTON_MINI);
            mini.setNoAi(false);
            int tickOffset = RANDOM.nextInt(11) - 5;
            setMiniExplodeAt(mini.getUUID(), now + MINI_EXPLODE_TICKS + tickOffset);
            MINI_BATCH_SIZE.put(mini.getUUID(), count);
            world.addFreshEntity(mini);

            Display.BlockDisplay leftGlass = SpamtonSpawner.spawnGlassesBlock(world, mini.position(), net.minecraft.world.level.block.Blocks.PINK_STAINED_GLASS.defaultBlockState(), SpamtonSpawner.GLASSES_SCALE_MINI, SpamtonSpawner.HEAD_HEIGHT_MINI);
            Display.BlockDisplay rightGlass = SpamtonSpawner.spawnGlassesBlock(world, mini.position(), net.minecraft.world.level.block.Blocks.YELLOW_STAINED_GLASS.defaultBlockState(), SpamtonSpawner.GLASSES_SCALE_MINI, SpamtonSpawner.HEAD_HEIGHT_MINI);
            if (leftGlass != null && rightGlass != null) {
                SpamtonSpawnerData.setGlasses(mini.getUUID(), leftGlass.getUUID(), rightGlass.getUUID());
            }

            double angle = 2 * Math.PI * RANDOM.nextDouble();
            double speed = 0.4 + RANDOM.nextDouble() * 0.3;
            mini.push(Math.cos(angle) * speed, 0.5, Math.sin(angle) * speed);
        }

        villager.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, RECOVERY_TICKS, 0, false, false));
        setRecoveryUntil(villager, now + RECOVERY_TICKS);
        // Hide glasses immediately so they don't stay visible for a tick
        SpamtonSpawner.updateGlassesPosition(world, villager);
    }
}

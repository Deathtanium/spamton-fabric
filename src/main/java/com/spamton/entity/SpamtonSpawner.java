package com.spamton.entity;

import com.mojang.math.Transformation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

public class SpamtonSpawner {

    public static final String TAG_SPAMTON_MERCHANT = "spamton_merchant";
    public static final String TAG_GLASSES_LEFT = "SpamtonGlassesLeft";
    public static final String TAG_GLASSES_RIGHT = "SpamtonGlassesRight";
    private static final double HEAD_HEIGHT = 1.62;
    /** Baby villager head height for mini glasses. */
    public static final double HEAD_HEIGHT_MINI = 0.81;
    private static final double HEAD_HEIGHT_OFFSET = -0.125; // 2 pixels down
    private static final float GLASSES_SCALE = 0.28f;
    public static final float GLASSES_SCALE_MINI = 0.14f;
    private static final double LENS_OFFSET = 0.15;
    /** Shift both lenses to the left (correct left offset). */
    private static final double LENS_SHIFT_LEFT = 0.15;
    /** Same shift for minis (scaled). */
    private static final double LENS_SHIFT_LEFT_MINI = 0.075;
    public static final double LENS_OFFSET_MINI = 0.075;

    public static Villager spawn(ServerLevel world, BlockPos pos, ServerPlayer player) {
        Villager villager = EntityType.VILLAGER.create(world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (villager == null) return null;

        villager.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        villager.setPersistenceRequired();
        villager.setCustomName(Component.literal("Spamton").withStyle(Style.EMPTY.withBold(true)));
        villager.setCustomNameVisible(true);
        villager.setNoAi(false);
        villager.getAttribute(Attributes.MAX_HEALTH).setBaseValue(99999.0);
        villager.setHealth(99999.0f);
        villager.setOffers(new MerchantOffers());
        villager.addTag(TAG_SPAMTON_MERCHANT);

        world.addFreshEntity(villager);

        // Set profession after spawn so it isn't reset by finalizeSpawn
        villager.setVillagerData(new VillagerData(
                world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.VILLAGER_TYPE).getOrThrow(VillagerType.PLAINS),
                world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.VILLAGER_PROFESSION).getOrThrow(VillagerProfession.LIBRARIAN),
                1
        ));

        Display.BlockDisplay left = spawnGlassesBlock(world, villager.position(), Blocks.PINK_STAINED_GLASS.defaultBlockState());
        Display.BlockDisplay right = spawnGlassesBlock(world, villager.position(), Blocks.YELLOW_STAINED_GLASS.defaultBlockState());
        if (left != null && right != null) {
            // Store glasses UUIDs in our in-memory map or use Fabric attachment; for spawn we can store on a static map keyed by villager UUID
            SpamtonSpawnerData.setGlasses(villager.getUUID(), left.getUUID(), right.getUUID());
        }

        return villager;
    }

    /** Spawn glasses for an existing merchant that has none (e.g. after reload if display entities weren't saved). */
    public static void ensureMerchantGlasses(ServerLevel world, Villager villager) {
        if (SpamtonSpawnerData.getGlassesLeft(villager.getUUID()) != null) return;
        Display.BlockDisplay left = spawnGlassesBlock(world, villager.position(), Blocks.PINK_STAINED_GLASS.defaultBlockState());
        Display.BlockDisplay right = spawnGlassesBlock(world, villager.position(), Blocks.YELLOW_STAINED_GLASS.defaultBlockState());
        if (left != null && right != null) {
            SpamtonSpawnerData.setGlasses(villager.getUUID(), left.getUUID(), right.getUUID());
        }
    }

    private static Display.BlockDisplay spawnGlassesBlock(ServerLevel world, Vec3 at, BlockState state) {
        Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world);
        entity.setPos(at.x, at.y + HEAD_HEIGHT, at.z);
        entity.setBlockState(state);
        Vector3f scale = new Vector3f(GLASSES_SCALE, GLASSES_SCALE, GLASSES_SCALE);
        entity.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                scale,
                new Quaternionf()
        ));
        entity.setPosRotInterpolationDuration(2);
        world.addFreshEntity(entity);
        return entity;
    }

    public static void updateGlassesPosition(ServerLevel world, Villager villager) {
        UUID villagerId = villager.getUUID();
        UUID leftId = SpamtonSpawnerData.getGlassesLeft(villagerId);
        UUID rightId = SpamtonSpawnerData.getGlassesRight(villagerId);
        if (leftId == null || rightId == null) return;

        var leftEntity = world.getEntity(leftId);
        var rightEntity = world.getEntity(rightId);
        if (leftEntity == null || rightEntity == null || !leftEntity.isAlive() || !rightEntity.isAlive()) {
            deleteGlasses(world, villagerId);
            return;
        }

        // Use head rotation so glasses track where the villager is looking
        float headYaw = villager.getYHeadRot();
        float headPitch = villager.getXRot();
        double yawRad = Math.toRadians(headYaw);
        double headX = villager.getX();
        double headY = villager.getY() + HEAD_HEIGHT + HEAD_HEIGHT_OFFSET;
        double headZ = villager.getZ();

        // Perpendicular to look: villager's right = +offset. Shift both left.
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);
        double leftX = headX - LENS_OFFSET * cos - LENS_SHIFT_LEFT * cos;
        double leftZ = headZ - LENS_OFFSET * sin - LENS_SHIFT_LEFT * sin;
        double rightX = headX + LENS_OFFSET * cos - LENS_SHIFT_LEFT * cos;
        double rightZ = headZ + LENS_OFFSET * sin - LENS_SHIFT_LEFT * sin;

        boolean ownerInvisible = villager.hasEffect(MobEffects.INVISIBILITY);
        if (leftEntity instanceof Display.BlockDisplay leftDisplay) {
            leftDisplay.setPos(leftX, headY, leftZ);
            leftDisplay.setYRot(headYaw);
            leftDisplay.setXRot(headPitch);
            leftDisplay.setInvisible(ownerInvisible);
        }
        if (rightEntity instanceof Display.BlockDisplay rightDisplay) {
            rightDisplay.setPos(rightX, headY, rightZ);
            rightDisplay.setYRot(headYaw);
            rightDisplay.setXRot(headPitch);
            rightDisplay.setInvisible(ownerInvisible);
        }
    }

    /** Update glasses for a mini Spamton (half scale, smaller head height, same left offset). */
    public static void updateGlassesPosition(ServerLevel world, Villager mini, double headHeight, double lensOffset, float scale) {
        UUID miniId = mini.getUUID();
        UUID leftId = SpamtonSpawnerData.getGlassesLeft(miniId);
        UUID rightId = SpamtonSpawnerData.getGlassesRight(miniId);
        if (leftId == null || rightId == null) return;

        var leftEntity = world.getEntity(leftId);
        var rightEntity = world.getEntity(rightId);
        if (leftEntity == null || rightEntity == null || !leftEntity.isAlive() || !rightEntity.isAlive()) {
            deleteGlasses(world, miniId);
            return;
        }

        float headYaw = mini.getYHeadRot();
        float headPitch = mini.getXRot();
        double yawRad = Math.toRadians(headYaw);
        double headX = mini.getX();
        double headY = mini.getY() + headHeight + HEAD_HEIGHT_OFFSET * 0.5;
        double headZ = mini.getZ();

        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);
        double leftX = headX - lensOffset * cos - LENS_SHIFT_LEFT_MINI * cos;
        double leftZ = headZ - lensOffset * sin - LENS_SHIFT_LEFT_MINI * sin;
        double rightX = headX + lensOffset * cos - LENS_SHIFT_LEFT_MINI * cos;
        double rightZ = headZ + lensOffset * sin - LENS_SHIFT_LEFT_MINI * sin;

        boolean ownerInvisible = mini.hasEffect(MobEffects.INVISIBILITY);
        if (leftEntity instanceof Display.BlockDisplay leftDisplay) {
            leftDisplay.setPos(leftX, headY, leftZ);
            leftDisplay.setYRot(headYaw);
            leftDisplay.setXRot(headPitch);
            leftDisplay.setInvisible(ownerInvisible);
        }
        if (rightEntity instanceof Display.BlockDisplay rightDisplay) {
            rightDisplay.setPos(rightX, headY, rightZ);
            rightDisplay.setYRot(headYaw);
            rightDisplay.setXRot(headPitch);
            rightDisplay.setInvisible(ownerInvisible);
        }
    }

    public static void deleteGlasses(ServerLevel world, java.util.UUID villagerOrMiniId) {
        UUID leftId = SpamtonSpawnerData.getGlassesLeft(villagerOrMiniId);
        UUID rightId = SpamtonSpawnerData.getGlassesRight(villagerOrMiniId);
        if (leftId != null) {
            var e = world.getEntity(leftId);
            if (e != null) e.discard();
        }
        if (rightId != null) {
            var e = world.getEntity(rightId);
            if (e != null) e.discard();
        }
        SpamtonSpawnerData.clearGlasses(villagerOrMiniId);
    }

    public static Display.BlockDisplay spawnGlassesBlock(ServerLevel world, Vec3 at, BlockState state, float scale, double headHeight) {
        Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world);
        entity.setPos(at.x, at.y + headHeight, at.z);
        entity.setBlockState(state);
        Vector3f scaleVec = new Vector3f(scale, scale, scale);
        entity.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                scaleVec,
                new Quaternionf()
        ));
        entity.setPosRotInterpolationDuration(2);
        world.addFreshEntity(entity);
        return entity;
    }
}

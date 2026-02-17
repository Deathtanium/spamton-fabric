package com.spamton.entity;

import com.spamton.SpamtonMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

public class SpamtonSpawner {

    public static final String TAG_SPAMTON_MERCHANT = "spamton_merchant";
    public static final String TAG_GLASSES_LEFT = "SpamtonGlassesLeft";
    public static final String TAG_GLASSES_RIGHT = "SpamtonGlassesRight";
    private static final double HEAD_HEIGHT = 1.62;
    private static final float GLASSES_SCALE = 0.28f;
    private static final double LENS_OFFSET = 0.15;

    public static VillagerEntity spawn(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
        VillagerEntity villager = EntityType.VILLAGER.create(world);
        if (villager == null) return null;

        villager.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        villager.setPersistent();
        villager.setCustomName(Text.literal("Spamton"));
        villager.setCustomNameVisible(true);
        villager.setNoAi(true);
        villager.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(99999.0);
        villager.setHealth(99999.0f);
        villager.setOffers(new net.minecraft.village.TradeOfferList());
        villager.addScoreboardTag(TAG_SPAMTON_MERCHANT);

        NbtCompound nbt = villager.getNbt();
        if (nbt == null) nbt = new NbtCompound();
        nbt.putBoolean(TAG_SPAMTON_MERCHANT, true);
        villager.readNbt(nbt);

        world.spawnEntity(villager);

        DisplayEntity.BlockDisplayEntity left = spawnGlassesBlock(world, villager.getPos(), net.minecraft.block.Blocks.PINK_STAINED_GLASS.getDefaultState());
        DisplayEntity.BlockDisplayEntity right = spawnGlassesBlock(world, villager.getPos(), net.minecraft.block.Blocks.YELLOW_STAINED_GLASS.getDefaultState());
        if (left != null && right != null) {
            NbtCompound villagerNbt = villager.getNbt();
            if (villagerNbt == null) villagerNbt = new NbtCompound();
            villagerNbt.putUuid(TAG_GLASSES_LEFT, left.getUuid());
            villagerNbt.putUuid(TAG_GLASSES_RIGHT, right.getUuid());
            villager.readNbt(villagerNbt);
        }

        return villager;
    }

    private static DisplayEntity.BlockDisplayEntity spawnGlassesBlock(ServerWorld world, Vec3d at, net.minecraft.block.BlockState state) {
        DisplayEntity.BlockDisplayEntity entity = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
        entity.setPosition(at.x, at.y + HEAD_HEIGHT, at.z);
        entity.getDataTracker().set(DisplayEntity.BlockDisplayEntity.BLOCK_STATE, state);
        Vector3f scale = new Vector3f(GLASSES_SCALE, GLASSES_SCALE, GLASSES_SCALE);
        entity.setTransformation(new net.minecraft.util.math.Transformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                scale,
                new Quaternionf()
        ));
        entity.setPersistent();
        world.spawnEntity(entity);
        return entity;
    }

    public static void updateGlassesPosition(ServerWorld world, VillagerEntity villager) {
        NbtCompound nbt = villager.getNbt();
        if (nbt == null || !nbt.containsUuid(TAG_GLASSES_LEFT) || !nbt.containsUuid(TAG_GLASSES_RIGHT)) return;

        UUID leftId = nbt.getUuid(TAG_GLASSES_LEFT);
        UUID rightId = nbt.getUuid(TAG_GLASSES_RIGHT);
        double yaw = Math.toRadians(villager.getYaw());
        double headX = villager.getX();
        double headY = villager.getY() + HEAD_HEIGHT;
        double headZ = villager.getZ();

        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        double leftX = headX - LENS_OFFSET * sin;
        double leftZ = headZ + LENS_OFFSET * cos;
        double rightX = headX + LENS_OFFSET * sin;
        double rightZ = headZ - LENS_OFFSET * cos;

        var leftEntity = world.getEntity(leftId);
        var rightEntity = world.getEntity(rightId);
        if (leftEntity instanceof DisplayEntity.BlockDisplayEntity leftDisplay) {
            leftDisplay.setPosition(leftX, headY, leftZ);
            leftDisplay.setYaw((float) Math.toDegrees(yaw));
        }
        if (rightEntity instanceof DisplayEntity.BlockDisplayEntity rightDisplay) {
            rightDisplay.setPosition(rightX, headY, rightZ);
            rightDisplay.setYaw((float) Math.toDegrees(yaw));
        }
    }
}

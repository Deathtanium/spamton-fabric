package com.spamton.api;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Registry access by identifier only. Use this instead of {@code Items.*}, {@code EntityType.*},
 * or {@code Blocks.*} so the mod does not depend on mapping-specific field names (Mojang or Yarn).
 * Fabric API and the game use these identifiers as the stable contract.
 */
public final class GameRegistries {

    private GameRegistries() {}

    // --- Item identifiers (minecraft namespace) ---
    public static final Identifier PAPER = id("paper");
    public static final Identifier EMERALD = id("emerald");
    public static final Identifier OBSIDIAN = id("obsidian");
    public static final Identifier BARRIER = id("barrier");
    public static final Identifier OAK_LEAVES = id("oak_leaves");
    public static final Identifier LEAVES = id("leaves");
    public static final Identifier SPLASH_POTION = id("splash_potion");
    public static final Identifier POTION = id("potion");
    public static final Identifier DIAMOND = id("diamond");
    public static final Identifier AIR = id("air");
    public static final Identifier WOLF_ARMOR = id("wolf_armor");

    // --- Entity type identifiers ---
    public static final Identifier VILLAGER = id("villager");
    public static final Identifier BLOCK_DISPLAY = id("block_display");

    // --- Block identifiers ---
    public static final Identifier PINK_STAINED_GLASS = id("pink_stained_glass");
    public static final Identifier YELLOW_STAINED_GLASS = id("yellow_stained_glass");

    // --- Spawn egg item ids for "The" loot ---
    public static final String[] SPAWN_EGG_IDS = {
        "bat_spawn_egg", "bee_spawn_egg", "chicken_spawn_egg", "cow_spawn_egg",
        "pig_spawn_egg", "sheep_spawn_egg", "zombie_spawn_egg", "skeleton_spawn_egg",
        "creeper_spawn_egg", "enderman_spawn_egg"
    };

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("minecraft", path);
    }

    public static Item getItem(Identifier id) {
        return BuiltInRegistries.ITEM.getValue(id);
    }

    public static Item getItem(String path) {
        return getItem(id(path));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> EntityType<T> getEntityType(Identifier id) {
        return (EntityType<T>) BuiltInRegistries.ENTITY_TYPE.getValue(id);
    }

    public static Block getBlock(Identifier id) {
        return BuiltInRegistries.BLOCK.getValue(id);
    }
}

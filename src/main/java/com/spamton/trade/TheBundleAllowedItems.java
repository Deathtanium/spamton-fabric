package com.spamton.trade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Pool of items allowed for "The" bundle: every survival-obtainable item except
 * impossible/creative/structure items and items obtainable in the End (for servers with End disabled).
 */
public final class TheBundleAllowedItems {

    private static List<Item> cachedPool;
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "spawn_egg",
            "command_block",
            "chain_command_block",
            "repeating_command_block",
            "structure_block",
            "structure_void",
            "jigsaw",
            "barrier",
            "debug_stick",
            "knowledge_book",
            "bedrock",
            "end_portal_frame",
            "farmland",
            "dirt_path",
            "petrified_oak_slab",
            "infested_",
            "chorus_fruit",
            "chorus_flower",
            "popped_chorus_fruit",
            "end_stone",
            "end_stone_bricks",
            "purpur",
            "end_rod",
            "dragon_breath",
            "elytra",
            "shulker_shell",
            "end_crystal",
            "spawner",
            "command_block_minecart"
    );

    private TheBundleAllowedItems() {}

    /** Builds the allowed item pool (cached). Call after registries are frozen. */
    public static List<Item> getAllowedItems() {
        if (cachedPool != null) return cachedPool;
        List<Item> list = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (isAllowed(item)) list.add(item);
        }
        cachedPool = List.copyOf(list);
        return cachedPool;
    }

    public static boolean isAllowed(Item item) {
        if (item == null || item == Items.AIR || item.getDefaultInstance().isEmpty()) return false;
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        String namespace = id.getNamespace();
        for (String excluded : EXCLUDED_PATHS) {
            if (path.contains(excluded)) return false;
        }
        return true;
    }

    /** Clear cache (e.g. after config reload if we ever add per-world rules). */
    public static void clearCache() {
        cachedPool = null;
    }
}

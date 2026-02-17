package com.spamton.trade;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds special trade result items with custom names and lore (no italics).
 */
public final class SpamtonSpecialItems {

    private static final Style NO_ITALIC = Style.EMPTY.withItalic(false);

    private SpamtonSpecialItems() {}

    /** Name: "all dogs go to heaven" (lowercase), Lore: "THE [[tried and tested]] [[pet insurance program]]" */
    public static ItemStack allDogsGoToHeavenArmor(Item armorItem) {
        ItemStack stack = new ItemStack(armorItem, 1);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("ALL DOGS GO TO HEAVEN").setStyle(NO_ITALIC));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("THE [[tried and tested]] [[pet insurance program]]").setStyle(NO_ITALIC)
        )));
        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            mutable.add(Enchantments.PROTECTION, 4);
            mutable.add(Enchantments.FIRE_PROTECTION, 4);
            mutable.add(Enchantments.BLAST_PROTECTION, 4);
            mutable.add(Enchantments.PROJECTILE_PROTECTION, 4);
        });
        return stack;
    }

    /** Name: "Fried Pipis", Lore: line 1 "[[Ethically-Sourced(tm)]] GUARANTEED", line 2 "this 'snack' has the consistency, and edibility similar to that of obsidian" */
    public static ItemStack friedPipis(int count) {
        ItemStack stack = new ItemStack(Items.OBSIDIAN, count);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Fried Pipis").setStyle(NO_ITALIC));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("[[ETHically-Sourced(tm)]] GUARANTEED").setStyle(NO_ITALIC),
                Text.literal("this 'snack' has consistency, and edibility similar to that of obsidian").setStyle(NO_ITALIC)
        )));
        return stack;
    }

    /**
     * "Alpha leaves" – legacy broken leaf block (pink/black missing texture on old clients).
     * Uses minecraft:leaves if present (legacy ID), otherwise oak_leaves. Name: "Bit-rot-aged tea leaves".
     * Lore: "How did <italic>he</italic> get these from the Farlands of old?"
     */
    public static ItemStack alphaLeaves() {
        Item leaves = Registries.ITEM.get(Identifier.of("minecraft", "leaves"));
        if (leaves == null || leaves == Items.AIR) {
            leaves = Items.OAK_LEAVES;
        }
        ItemStack stack = new ItemStack(leaves, 1);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Bit-rot-aged tea leaves").setStyle(NO_ITALIC));
        Text loreLine = Text.literal("How did ")
                .append(Text.literal("he").setStyle(Style.EMPTY.withItalic(true)))
                .append(Text.literal(" get these from the Farlands of old?"));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(loreLine)));
        return stack;
    }

    /**
     * Splash potion of Instant Health 125 – health overflow kills even creative players.
     * Name: "Admin-b-gone". Lore: "ONE SPLASH OF THIS [[Radiation hazard]] GUARANTEED TO [[/kill @e]] IN RANGE"
     */
    public static ItemStack adminBGonePotion() {
        ItemStack stack = new ItemStack(Items.SPLASH_POTION, 1);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Admin-B-Gone").setStyle(NO_ITALIC));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("ONE SPLASH OF THIS [[Radiation hazard]] GUARANTEED TO [[/kill @e]] IN RANGE").setStyle(NO_ITALIC)
        )));
        // Instant Health 125: amplifier 124 (0-based), duration 1 tick
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(
                Optional.empty(),
                Optional.empty(),
                List.of(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH, 1, 124))
        ));
        return stack;
    }

    /** Wolf/dog armor items by material (wolf armor 1.21; fallback to horse armor if wolf armor id missing). */
    public static List<Item> getDogArmorMaterials() {
        List<Item> out = new ArrayList<>();
        String[] variants = {"leather_wolf_armor", "golden_wolf_armor", "iron_wolf_armor", "diamond_wolf_armor"};
        for (String id : variants) {
            Item item = Registries.ITEM.get(Identifier.of("minecraft", id));
            if (item != null && item != Items.AIR) {
                out.add(item);
            }
        }
        if (out.isEmpty()) {
            out.add(Items.LEATHER_HORSE_ARMOR);
            out.add(Items.GOLDEN_HORSE_ARMOR);
            out.add(Items.IRON_HORSE_ARMOR);
            out.add(Items.DIAMOND_HORSE_ARMOR);
        }
        return out;
    }
}

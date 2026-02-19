package com.spamton.trade;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Builds special trade result items with custom names and lore (no italics).
 */
public final class SpamtonSpecialItems {

    private static final Style NO_ITALIC = Style.EMPTY.withItalic(false);

    private SpamtonSpecialItems() {}

    /** Name: "all dogs go to heaven" (lowercase), Lore: "THE [[tried and tested]] [[pet insurance program]]" */
    public static ItemStack allDogsGoToHeavenArmor(Item armorItem, RegistryAccess registryAccess) {
        ItemStack stack = new ItemStack(armorItem, 1);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("ALL DOGS GO TO HEAVEN").withStyle(NO_ITALIC));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("THE [[tried and tested]] [[pet insurance program]]").withStyle(NO_ITALIC)
        )));
        if (registryAccess != null) {
            var reg = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
            EnchantmentHelper.updateEnchantments(stack, mutable -> {
                reg.getOptional(Enchantments.PROTECTION).ifPresent(e -> mutable.set(reg.wrapAsHolder(e), 4));
                reg.getOptional(Enchantments.FIRE_PROTECTION).ifPresent(e -> mutable.set(reg.wrapAsHolder(e), 4));
                reg.getOptional(Enchantments.BLAST_PROTECTION).ifPresent(e -> mutable.set(reg.wrapAsHolder(e), 4));
                reg.getOptional(Enchantments.PROJECTILE_PROTECTION).ifPresent(e -> mutable.set(reg.wrapAsHolder(e), 4));
            });
        }
        return stack;
    }

    /** Name: "Fried Pipis", Lore: line 1 "[[Ethically-Sourced(tm)]] GUARANTEED", line 2 "this 'snack' has the consistency, and edibility similar to that of obsidian" */
    public static ItemStack friedPipis(int count) {
        ItemStack stack = new ItemStack(Items.OBSIDIAN, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Fried Pipis").withStyle(NO_ITALIC));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("[[ETHically-Sourced(tm)]] GUARANTEED").withStyle(NO_ITALIC),
                Component.literal("this 'snack' has consistency, and edibility similar to that of obsidian").withStyle(NO_ITALIC)
        )));
        return stack;
    }

    /**
     * "Alpha leaves" – vanilla leaves. Name: "Alpha leaves" (no italics). Lore: "[[Certified Classic]] from the Farlands".
     */
    public static ItemStack alphaLeaves() {
        Item leaves = BuiltInRegistries.ITEM.getOptional(Identifier.parse("minecraft:leaves")).orElse(Items.AIR);
        if (leaves == null || leaves == Items.AIR) leaves = Items.OAK_LEAVES;
        ItemStack stack = new ItemStack(leaves, 1);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Alpha leaves").withStyle(NO_ITALIC));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("[[Certified Classic]] from the Farlands").withStyle(NO_ITALIC)
        )));
        return stack;
    }

    /**
     * Splash potion of Instant Health 125 – health overflow kills even creative players.
     * Name: "Admin-b-gone". Lore: "ONE SPLASH OF THIS [[Radiation hazard]] GUARANTEED TO [[/kill @e]] IN RANGE"
     */
    public static ItemStack adminBGonePotion() {
        ItemStack stack = new ItemStack(Items.SPLASH_POTION, 1);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Admin-B-Gone").withStyle(NO_ITALIC));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("ONE SPLASH OF THIS [[Radiation hazard]] GUARANTEED TO [[/kill @e]] IN RANGE").withStyle(NO_ITALIC)
        )));
        // Instant Health 1: amplifier 0 (0-based), duration 1 tick
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(
                Optional.empty(),
                Optional.empty(),
                List.of(new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 0)),
                Optional.empty()
        ));
        stack.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(false, new LinkedHashSet<>(Set.of(DataComponents.POTION_CONTENTS))));
        return stack;
    }

    /** Wolf armor (single type in 1.21). */
    public static List<Item> getDogArmorMaterials() {
        return List.of(Items.WOLF_ARMOR);
    }
}

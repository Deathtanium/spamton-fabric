package com.spamton.trade;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

public class TheTradeLootGenerator {

    private static final Random RANDOM = Random.create();

    public static ItemStack generate() {
        double r = RANDOM.nextDouble();
        if (r < 0.01) return randomSpawnEgg();
        if (r < 0.10) return randomPotion();
        return randomItemWithEnchant();
    }

    private static ItemStack randomItemWithEnchant() {
        List<net.minecraft.item.Item> items = new ArrayList<>();
        Registries.ITEM.forEach(items::add);
        if (items.isEmpty()) return new ItemStack(Items.DIAMOND);
        net.minecraft.item.Item item = items.get(RANDOM.nextInt(items.size()));
        ItemStack stack = new ItemStack(item, 1);
        try {
            EnchantmentHelper.enchant(stack, RANDOM.nextInt(30) + 1, RANDOM, false);
        } catch (Exception ignored) {
        }
        return stack;
    }

    private static ItemStack randomPotion() {
        ItemStack stack = new ItemStack(Items.POTION, 1);
        List<net.minecraft.entity.effect.StatusEffect> effects = new ArrayList<>();
        Registries.STATUS_EFFECT.forEach(effects::add);
        if (!effects.isEmpty()) {
            net.minecraft.entity.effect.StatusEffect effect = effects.get(RANDOM.nextInt(effects.size()));
            stack.add(net.minecraft.component.DataComponentTypes.POTION_CONTENTS,
                    new net.minecraft.component.type.PotionContentsComponent(
                            java.util.Optional.empty(),
                            java.util.Optional.of(new StatusEffectInstance(effect, 200 + RANDOM.nextInt(600), RANDOM.nextInt(2))),
                            java.util.Optional.empty()
                    ));
        }
        return stack;
    }

    private static final net.minecraft.item.Item[] SPAWN_EGGS = new net.minecraft.item.Item[]{
            Items.BAT_SPAWN_EGG, Items.BEE_SPAWN_EGG, Items.CHICKEN_SPAWN_EGG, Items.COW_SPAWN_EGG,
            Items.PIG_SPAWN_EGG, Items.SHEEP_SPAWN_EGG, Items.ZOMBIE_SPAWN_EGG, Items.SKELETON_SPAWN_EGG,
            Items.CREEPER_SPAWN_EGG, Items.ENDERMAN_SPAWN_EGG
    };

    private static ItemStack randomSpawnEgg() {
        return new ItemStack(SPAWN_EGGS[RANDOM.nextInt(SPAWN_EGGS.length)], 1);
    }
}

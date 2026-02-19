package com.spamton.item;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.trading.ItemCost;

import java.util.List;


/**
 * Kromer: paper with mandatory custom NBT so it cannot be faked by renaming.
 * Only createKromer() and Spamton trades produce valid Kromer; anvil-renamed paper is rejected.
 */
public class KromerItem {

    /** CustomData key that marks paper as Kromer. Players cannot add this in survival. */
    public static final String KROMER_TAG = "spamton_kromer";

    private static final Style KROMER_NAME_STYLE = Style.EMPTY.withItalic(false).withBold(true);
    private static final Style KROMER_LORE_STYLE = Style.EMPTY.withItalic(false);

    public static ItemStack createKromer(int count) {
        ItemStack stack = new ItemStack(Items.PAPER, count);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KROMER_TAG, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Kromer").withStyle(KROMER_NAME_STYLE));
        stack.set(DataComponents.RARITY, Rarity.UNCOMMON);
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal("THE [[currency of the future]]").withStyle(KROMER_LORE_STYLE),
                Component.literal("! ITS VALUE ADJUSTS IN REAL TIME TO [[The Market(tm)]]").withStyle(KROMER_LORE_STYLE),
                Component.literal(" TOTALLY REFLECTS REAL DEMAND").withStyle(KROMER_LORE_STYLE)
        )));
        return stack;
    }

    /** ItemCost for trade menu display: shows "Kromer" instead of "Paper" and still accepts only real Kromer when paying. Pass null registryAccess to fall back to plain Paper display. */
    public static ItemCost kromerCost(int count, RegistryAccess registryAccess) {
        if (registryAccess == null)
            return new ItemCost(Items.PAPER, count);
        // Predicate must include display components (name, lore, rarity) so the client shows "Kromer"; CUSTOM_DATA restricts payment to real Kromer.
        ItemStack template = createKromer(1);
        DataComponentExactPredicate predicate = DataComponentExactPredicate.allOf(template.getComponents());
        return new ItemCost(
                registryAccess.lookupOrThrow(Registries.ITEM).getOrThrow(ResourceKey.create(Registries.ITEM, Identifier.parse("minecraft:paper"))),
                count,
                predicate,
                createKromer(count)
        );
    }

    /** True only if stack is paper and has our KROMER_TAG in CustomData (not just renamed). */
    public static boolean isKromer(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.is(Items.PAPER)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        CompoundTag tag = data.copyTag();
        return tag != null && tag.contains(KROMER_TAG) && tag.getBoolean(KROMER_TAG).orElse(false);
    }
}

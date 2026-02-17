package com.spamton.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/**
 * Kromer: paper with mandatory custom NBT so it cannot be faked by renaming.
 * Only createKromer() and Spamton trades produce valid Kromer; anvil-renamed paper is rejected.
 */
public class KromerItem {

    /** CustomData key that marks paper as Kromer. Players cannot add this in survival. */
    public static final String KROMER_TAG = "spamton_kromer";

    public static ItemStack createKromer(int count) {
        ItemStack stack = new ItemStack(Items.PAPER, count);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KROMER_TAG, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Kromer"));
        return stack;
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

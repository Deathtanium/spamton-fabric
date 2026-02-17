package com.spamton.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

public class KromerItem {

    public static final String KROMER_TAG = "spamton_kromer";

    public static ItemStack createKromer(int count) {
        ItemStack stack = new ItemStack(Items.PAPER, count);
        NbtCompound tag = new NbtCompound();
        tag.putBoolean(KROMER_TAG, true);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(tag));
        return stack;
    }

    public static boolean isKromer(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (nbt == null) return false;
        NbtCompound tag = nbt.copyNbt();
        return tag != null && tag.getBoolean(KROMER_TAG);
    }
}

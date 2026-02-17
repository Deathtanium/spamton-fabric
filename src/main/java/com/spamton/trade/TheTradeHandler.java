package com.spamton.trade;

import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.trading.MerchantOffer;

public class TheTradeHandler {

    public static boolean isTheTrade(MerchantOffer offer) {
        ItemStack sell = offer.getResult();
        if (sell.isEmpty()) return false;
        Component name = sell.get(DataComponents.CUSTOM_NAME);
        return name != null && name.getString().equals("The");
    }

    /** True if stack is a bundle named "The" with no contents (the display placeholder). */
    public static boolean isEmptyTheBundle(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(Items.BUNDLE)) return false;
        Component name = stack.get(DataComponents.CUSTOM_NAME);
        if (name == null || !name.getString().equals("The")) return false;
        BundleContents contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        return contents == null || contents.isEmpty();
    }

    /** Remove one empty "The" bundle from the player (cursor then inventory). Use after giving the real bundle so any duplicate from Paper/vanilla is stripped. */
    public static void removeOneEmptyTheBundleFromPlayer(Player player) {
        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty() && isEmptyTheBundle(carried)) {
            carried.shrink(1);
            if (carried.isEmpty()) player.containerMenu.setCarried(ItemStack.EMPTY);
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && isEmptyTheBundle(stack)) {
                stack.shrink(1);
                return;
            }
        }
    }

    /** Called from notifyTrade mixin; payment was already consumed by MerchantResultSlot.onTake (offer.take()). */
    public static void onTheTrade(AbstractVillager merchant, Player player, MerchantOffer offer) {
        ItemStack result = TheTradeLootGenerator.generate(player.level().registryAccess());
        player.getInventory().add(result);
    }
}

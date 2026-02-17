package com.spamton.trade;

import com.spamton.item.KromerItem;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.village.TradeOffer;

public class TheTradeHandler {

    public static boolean isTheTrade(TradeOffer offer) {
        ItemStack sell = offer.getSellItem();
        if (sell.isEmpty()) return false;
        Text name = sell.get(net.minecraft.component.DataComponentTypes.CUSTOM_NAME);
        return name != null && name.getString().equals("The");
    }

    public static void onTheTrade(MerchantEntity merchant, PlayerEntity player, TradeOffer offer) {
        Inventory inv = merchant.getInventory();
        ItemStack first = inv.getStack(0);
        ItemStack second = inv.getStack(1);
        int cost = offer.getOriginalFirstBuyItem().getCount();
        int total = (KromerItem.isKromer(first) ? first.getCount() : 0) + (KromerItem.isKromer(second) ? second.getCount() : 0);
        if (total < cost) return;
        int remaining = cost;
        if (KromerItem.isKromer(first)) {
            int take = Math.min(remaining, first.getCount());
            first.decrement(take);
            remaining -= take;
        }
        if (remaining > 0 && KromerItem.isKromer(second)) {
            second.decrement(remaining);
        }
        inv.markDirty();
        ItemStack result = TheTradeLootGenerator.generate();
        player.getInventory().insertStack(result);
    }
}

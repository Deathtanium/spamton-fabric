package com.spamton.mixin;

import com.spamton.item.KromerItem;
import com.spamton.trade.TheTradeHandler;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantEntity.class)
public class SpamtonTradeExecutionMixin {

    @Inject(method = "trade", at = @At("HEAD"), cancellable = true)
    private void onTrade(TradeOffer offer, CallbackInfo ci) {
        MerchantEntity self = (MerchantEntity) (Object) this;
        if (!(self instanceof VillagerEntity villager) || !villager.getScoreboardTags().contains("spamton_merchant"))
            return;

        PlayerEntity customer = self.getCustomer();
        if (customer == null) return;

        ItemStack firstBuy = offer.getOriginalFirstBuyItem();
        if (firstBuy.getItem() == Items.PAPER) {
            net.minecraft.inventory.Inventory merchantInv = self.getInventory();
            ItemStack slot0 = merchantInv.getStack(0);
            ItemStack slot1 = merchantInv.getStack(1);
            int kromerCount = (KromerItem.isKromer(slot0) ? slot0.getCount() : 0) + (KromerItem.isKromer(slot1) ? slot1.getCount() : 0);
            if (kromerCount < firstBuy.getCount()) {
                ci.cancel();
                return;
            }
        }

        if (TheTradeHandler.isTheTrade(offer)) {
            TheTradeHandler.onTheTrade(self, customer, offer);
            ci.cancel();
        }
    }
}

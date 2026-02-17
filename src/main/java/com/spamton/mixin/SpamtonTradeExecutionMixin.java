package com.spamton.mixin;

import com.spamton.item.KromerItem;
import net.minecraft.world.Container;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractVillager.class)
public class SpamtonTradeExecutionMixin {

    @Inject(method = "notifyTrade", at = @At("HEAD"), cancellable = true)
    private void onTrade(MerchantOffer offer, CallbackInfo ci) {
        AbstractVillager self = (AbstractVillager) (Object) this;
        if (!(self instanceof Villager villager) || !villager.getTags().contains("spamton_merchant"))
            return;

        Player customer = villager.getTradingPlayer();
        if (customer == null) return;

        ItemStack firstBuy = offer.getCostA();
        // Trades that cost "paper" require real Kromer (paper + our tag); reject plain paper
        if (firstBuy.getItem() == Items.PAPER) {
            Container merchantInv = self.getInventory();
            ItemStack slot0 = merchantInv.getItem(0);
            ItemStack slot1 = merchantInv.getItem(1);
            int kromerCount = (KromerItem.isKromer(slot0) ? slot0.getCount() : 0) + (KromerItem.isKromer(slot1) ? slot1.getCount() : 0);
            if (kromerCount < firstBuy.getCount()) {
                ci.cancel();
                return;
            }
        }
        // "The" trade: result is already the bundle (from offer); slot gave it to player. No extra give.
    }
}

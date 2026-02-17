package com.spamton.mixin;

import com.spamton.entity.SpamtonSpawner;
import com.spamton.item.KromerItem;
import com.spamton.trade.TheTradeHandler;
import com.spamton.trade.TheTradeLootGenerator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MerchantMenu.class)
public class SpamtonMerchantMenuMixin {

    private static final int RESULT_SLOT = 2;

    @Shadow @Final private Merchant trader;
    @Shadow @Final private MerchantContainer tradeContainer;

    /**
     * Shift-clicking the result slot: validate Kromer for paper trades; for "The" give real bundle
     * and consume payment instead of moving the empty display bundle.
     */
    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void onQuickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index != RESULT_SLOT) return;
        if (!(trader instanceof Villager villager) || !villager.getTags().contains(SpamtonSpawner.TAG_SPAMTON_MERCHANT))
            return;

        MerchantOffer offer = tradeContainer.getActiveOffer();
        if (offer == null) {
            cir.setReturnValue(ItemStack.EMPTY);
            cir.cancel();
            return;
        }
        ItemStack slot0 = tradeContainer.getItem(0);
        ItemStack slot1 = tradeContainer.getItem(1);
        ItemStack costA = offer.getCostA();
        if (costA.getItem() != Items.PAPER) return;

        int kromerCount = (KromerItem.isKromer(slot0) ? slot0.getCount() : 0) + (KromerItem.isKromer(slot1) ? slot1.getCount() : 0);
        if (kromerCount < costA.getCount()) {
            cir.setReturnValue(ItemStack.EMPTY);
            cir.cancel();
            return;
        }
        if (TheTradeHandler.isTheTrade(offer)) {
            if (!offer.take(slot0, slot1)) offer.take(slot1, slot0);
            tradeContainer.setItem(0, slot0);
            tradeContainer.setItem(1, slot1);
            player.getInventory().add(TheTradeLootGenerator.generate(villager.level().registryAccess()));
            TheTradeHandler.removeOneEmptyTheBundleFromPlayer(player);
            tradeContainer.setItem(RESULT_SLOT, ItemStack.EMPTY);
            cir.setReturnValue(ItemStack.EMPTY);
            cir.cancel();
            return;
        }
        if (!offer.take(slot0, slot1) && !offer.take(slot1, slot0)) return;
        tradeContainer.setItem(0, slot0);
        tradeContainer.setItem(1, slot1);
    }
}

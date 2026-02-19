package com.spamton.mixin;

import com.spamton.entity.SpamtonSpawner;
import net.minecraft.world.Container;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla only fills the result slot when the cost predicate matches the input slots.
 * Our Kromer cost uses a strict predicate (allOf name/lore/rarity/custom_data), so empty or
 * plain-paper slots fail and the result stays empty. Force the result slot to show the offer's
 * result for Spamton so the purchase preview is visible; payment is still validated on take.
 */
@Mixin(MerchantMenu.class)
public class SpamtonMerchantContainerMixin {

    private static final int RESULT_SLOT = 2;

    @Shadow @Final private Merchant trader;
    @Shadow @Final private MerchantContainer tradeContainer;

    @Inject(method = "slotsChanged", at = @At("RETURN"))
    private void ensureResultPreviewShown(Container container, CallbackInfo ci) {
        if (container != tradeContainer) return;
        if (!(trader instanceof Villager villager) || !villager.getTags().contains(SpamtonSpawner.TAG_SPAMTON_MERCHANT))
            return;
        MerchantOffer offer = tradeContainer.getActiveOffer();
        if (offer == null) return;
        if (tradeContainer.getItem(RESULT_SLOT).isEmpty())
            tradeContainer.setItem(RESULT_SLOT, offer.getResult());
    }
}

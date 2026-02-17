package com.spamton.mixin;

import com.spamton.entity.SpamtonSpawner;
import com.spamton.trade.SpamtonTradeGenerator;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public class SpamtonMerchantMixin {

    @Inject(method = "setCustomer", at = @At("HEAD"))
    private void onSetCustomer(PlayerEntity customer, CallbackInfo ci) {
        if (customer == null) return;
        VillagerEntity self = (VillagerEntity) (Object) this;
        if (!self.getScoreboardTags().contains(SpamtonSpawner.TAG_SPAMTON_MERCHANT)) return;
        TradeOfferList newOffers = SpamtonTradeGenerator.generate();
        self.setOffersFromServer(newOffers);
    }
}

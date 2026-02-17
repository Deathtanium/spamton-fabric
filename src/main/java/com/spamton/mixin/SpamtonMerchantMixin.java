package com.spamton.mixin;

import com.spamton.entity.SpamtonSpawner;
import com.spamton.trade.SpamtonTradeGenerator;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public class SpamtonMerchantMixin {

    @Inject(method = "setTradingPlayer", at = @At("HEAD"))
    private void onSetCustomer(Player customer, CallbackInfo ci) {
        if (customer == null) return;
        Villager self = (Villager) (Object) this;
        if (!self.getTags().contains(SpamtonSpawner.TAG_SPAMTON_MERCHANT)) return;
        MerchantOffers newOffers = SpamtonTradeGenerator.generate(self.level().registryAccess());
        self.setOffers(newOffers);
    }
}

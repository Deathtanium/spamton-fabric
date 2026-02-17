package com.spamton.mixin;

import com.spamton.entity.SpamtonSpawner;
import com.spamton.item.KromerItem;
import com.spamton.trade.TheTradeHandler;
import com.spamton.trade.TheTradeLootGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When the player takes the result (clicks the output slot): block if paper cost but not enough
 * Kromer; for "The" trade give the real (rolled) bundle and consume payment instead of the empty
 * display bundle; when active offer is null, cancel and revert to prevent duplication.
 */
@Mixin(MerchantResultSlot.class)
public class SpamtonMerchantResultSlotMixin {

    private static final int RESULT_SLOT = 2;

    @Shadow @Final private MerchantContainer slots;
    @Shadow @Final private Merchant merchant;

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void requireKromerWhenTakingResult(Player player, ItemStack stack, CallbackInfo ci) {
        if (!(merchant instanceof Villager villager) || !villager.getTags().contains(SpamtonSpawner.TAG_SPAMTON_MERCHANT))
            return;
        ItemStack slot0 = slots.getItem(0);
        ItemStack slot1 = slots.getItem(1);
        MerchantOffer offer = slots.getActiveOffer();

        if (offer != null) {
            if (TheTradeHandler.isTheTrade(offer)) {
                ci.cancel();
                if (!offer.take(slot0, slot1)) offer.take(slot1, slot0);
                slots.setItem(0, slot0);
                slots.setItem(1, slot1);
                RegistryAccess ra = villager.level().registryAccess();
                player.getInventory().add(TheTradeLootGenerator.generate(ra));
                TheTradeHandler.removeOneEmptyTheBundleFromPlayer(player);
                slots.setItem(RESULT_SLOT, ItemStack.EMPTY);
                return;
            }
            ItemStack costA = offer.getCostA();
            if (costA.getItem() != Items.PAPER) return;
            int kromerCount = (KromerItem.isKromer(slot0) ? slot0.getCount() : 0) + (KromerItem.isKromer(slot1) ? slot1.getCount() : 0);
            if (kromerCount < costA.getCount()) {
                ci.cancel();
                slots.setItem(RESULT_SLOT, stack);
                revertResultToPlayer(player, stack);
            }
            return;
        }

        // Active offer is null: vanilla onTake skips offer.take() and setItem(), so payment is
        // never consumed but the result was already given → duplication. Cancel and revert.
        ci.cancel();
        slots.setItem(RESULT_SLOT, stack);
        revertResultToPlayer(player, stack);
    }

    /** Remove the result from the player (cursor or inventory) so we don't duplicate. */
    private void revertResultToPlayer(Player player, ItemStack stack) {
        ItemStack carried = player.containerMenu.getCarried();
        if (!carried.isEmpty() && ItemStack.isSameItemSameComponents(carried, stack)) {
            int take = Math.min(carried.getCount(), stack.getCount());
            carried.shrink(take);
            if (carried.isEmpty()) player.containerMenu.setCarried(ItemStack.EMPTY);
            if (take >= stack.getCount()) return;
            stack = stack.copyWithCount(stack.getCount() - take);
        }
        if (!stack.isEmpty()) player.getInventory().removeItem(stack);
    }
}

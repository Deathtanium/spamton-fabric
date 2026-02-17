package com.spamton.trade;

import com.spamton.SpamtonConfig;
import com.spamton.item.KromerItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class SpamtonTradeGenerator {

    private static final Random RANDOM = new Random();
    private static final int MAX_USES = 999;
    private static final int XP = 0;
    private static final float PRICE_MULT = 0.05f;

    public static MerchantOffers generate() {
        return generate(null);
    }

    public static MerchantOffers generate(net.minecraft.core.RegistryAccess registryAccess) {
        MerchantOffers list = new MerchantOffers();
        Random r = new Random();
        Set<String> disabled = Set.copyOf(SpamtonConfig.disabledTrades);

        if (!disabled.contains("kromer_for_emeralds")) {
            int emeraldCost = 1 + r.nextInt(64);
            int kromerAmount = 1 + r.nextInt(64);
            list.add(new MerchantOffer(
                    new ItemCost(Items.EMERALD, emeraldCost),
                    KromerItem.createKromer(kromerAmount),
                    MAX_USES, XP, PRICE_MULT
            ));
        }

        if (!disabled.contains("dog_armor")) {
            for (Item armorItem : SpamtonSpecialItems.getDogArmorMaterials()) {
                int armorKromerCost = 1 + r.nextInt(64);
                list.add(new MerchantOffer(
                        new ItemCost(Items.PAPER, armorKromerCost),
                        Optional.of(new ItemCost(armorItem, 1)),
                        SpamtonSpecialItems.allDogsGoToHeavenArmor(armorItem, registryAccess),
                        MAX_USES, XP, PRICE_MULT
                ));
            }
        }

        if (!disabled.contains("fried_pipis")) {
            int friedPipisKromer = 1 + r.nextInt(64);
            list.add(new MerchantOffer(
                    new ItemCost(Items.PAPER, friedPipisKromer),
                    SpamtonSpecialItems.friedPipis(64),
                    MAX_USES, XP, PRICE_MULT
            ));
        }

        if (!disabled.contains("alpha_leaves")) {
            int alphaLeavesKromer = 1 + r.nextInt(64);
            list.add(new MerchantOffer(
                    new ItemCost(Items.PAPER, alphaLeavesKromer),
                    SpamtonSpecialItems.alphaLeaves(),
                    MAX_USES, XP, PRICE_MULT
            ));
        }

        if (!disabled.contains("admin_b_gone")) {
            list.add(new MerchantOffer(
                    new ItemCost(Items.PAPER, 67),
                    SpamtonSpecialItems.adminBGonePotion(),
                    MAX_USES, XP, PRICE_MULT
            ));
        }

        if (!disabled.contains("the")) {
            int theKromerCost = 32 + r.nextInt(33);
            list.add(new MerchantOffer(
                    new ItemCost(Items.PAPER, theKromerCost),
                    TheTradeLootGenerator.createEmptyTheBundle(),
                    1, XP, PRICE_MULT
            ));
        }

        for (SpamtonConfig.CustomTradeEntry entry : SpamtonConfig.customTrades) {
            Item buyItem = entry.randomKromerPrice ? null : getItem(entry.buyId);
            Item sellItem = getItem(entry.sellId);
            if (sellItem == null || sellItem == Items.AIR) continue;
            if (!entry.randomKromerPrice && (buyItem == null || buyItem == Items.AIR)) continue;
            ItemStack result = new ItemStack(sellItem, Math.max(1, entry.sellCount));
            if (result.isEmpty()) continue;
            if (entry.sellName != null && !entry.sellName.isEmpty())
                result.set(DataComponents.CUSTOM_NAME, Component.literal(entry.sellName).withStyle(Style.EMPTY.withItalic(false)));
            if (entry.sellLore != null && !entry.sellLore.isEmpty()) {
                List<Component> lines = new ArrayList<>();
                for (String line : entry.sellLore)
                    lines.add(Component.literal(line).withStyle(Style.EMPTY.withItalic(false)));
                result.set(DataComponents.LORE, new ItemLore(lines));
            }
            Optional<ItemCost> second = Optional.empty();
            if (entry.buyBId != null && entry.buyBCount > 0) {
                Item buyB = getItem(entry.buyBId);
                if (buyB != null && buyB != Items.AIR)
                    second = Optional.of(new ItemCost(buyB, entry.buyBCount));
            }
            int kromerAmount = 1 + r.nextInt(64);
            if (entry.randomKromerPrice && (entry.minKromer != null || entry.maxKromer != null)) {
                int min = entry.minKromer != null ? Math.max(1, entry.minKromer) : 1;
                int max = entry.maxKromer != null ? Math.max(min, entry.maxKromer) : 64;
                kromerAmount = min + r.nextInt(Math.max(1, max - min + 1));
            }
            ItemCost firstCost = entry.randomKromerPrice
                    ? new ItemCost(Items.PAPER, kromerAmount)
                    : new ItemCost(buyItem, Math.max(1, entry.buyCount));
            list.add(new MerchantOffer(
                    firstCost,
                    second,
                    result,
                    Math.max(1, entry.maxUses), XP, PRICE_MULT
            ));
        }

        return list;
    }

    private static Item getItem(String id) {
        try {
            return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /** Empty bundle named "The" for display (no preview of contents). */
    public static ItemStack getRandomTheDisplayItem(RegistryAccess registryAccess) {
        return TheTradeLootGenerator.createEmptyTheBundle();
    }

    /** Rebuild offers; "The" stays an empty bundle so no preview. */
    public static MerchantOffers replaceTheDisplayOnly(MerchantOffers current, RegistryAccess registryAccess) {
        if (current.isEmpty()) return current;
        MerchantOffer last = current.get(current.size() - 1);
        if (!TheTradeHandler.isTheTrade(last)) return current;
        MerchantOffers next = new MerchantOffers();
        for (int i = 0; i < current.size() - 1; i++) next.add(current.get(i));
        int kromerCost = Math.max(1, last.getItemCostA().count());
        next.add(new MerchantOffer(
                new ItemCost(Items.PAPER, kromerCost),
                TheTradeLootGenerator.createEmptyTheBundle(),
                1, XP, PRICE_MULT
        ));
        return next;
    }
}

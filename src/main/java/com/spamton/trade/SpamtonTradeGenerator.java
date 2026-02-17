package com.spamton.trade;

import com.spamton.SpamtonConfig;
import com.spamton.item.KromerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class SpamtonTradeGenerator {

    private static final Random RANDOM = new Random();
    private static final int MAX_USES = 999;
    private static final int XP = 0;
    private static final float PRICE_MULT = 0.05f;

    public static TradeOfferList generate() {
        TradeOfferList list = new TradeOfferList();
        Random r = new Random();

        int a = 1 + r.nextInt(64);
        int b = 1 + r.nextInt(64);
        if (r.nextBoolean()) {
            list.add(new TradeOffer(
                    new TradedItem(Items.PAPER, a),
                    new ItemStack(Items.EMERALD, b),
                    MAX_USES, XP, PRICE_MULT
            ));
        } else {
            list.add(new TradeOffer(
                    new TradedItem(Items.EMERALD, b),
                    KromerItem.createKromer(a),
                    MAX_USES, XP, PRICE_MULT
            ));
        }

        for (String idStr : SpamtonConfig.placeholderItems) {
            Item item = Registries.ITEM.get(Identifier.of(idStr));
            if (item == Items.AIR) continue;
            int kromerCost = 1 + r.nextInt(64);
            list.add(new TradeOffer(
                    new TradedItem(Items.PAPER, kromerCost),
                    new ItemStack(item, 1),
                    MAX_USES, XP, PRICE_MULT
            ));
        }

        // Emeralds + unenchanted dog armor (one per material) -> "ALL DOGS GO TO HEAVEN" armor with all Protection enchants
        for (Item armorItem : SpamtonSpecialItems.getDogArmorMaterials()) {
            int emeraldCost = 1 + r.nextInt(64);
            list.add(new TradeOffer(
                    new TradedItem(Items.EMERALD, emeraldCost),
                    Optional.of(new TradedItem(armorItem, 1)),
                    SpamtonSpecialItems.allDogsGoToHeavenArmor(armorItem),
                    MAX_USES, XP, PRICE_MULT
            ));
        }

        // Emeralds -> stack of obsidian "Fried Pipis"
        int friedPipisEmeralds = 1 + r.nextInt(64);
        list.add(new TradeOffer(
                new TradedItem(Items.EMERALD, friedPipisEmeralds),
                SpamtonSpecialItems.friedPipis(64),
                MAX_USES, XP, PRICE_MULT
        ));

        // Emeralds -> 1 "alpha leaves" (Bit-rot-aged tea leaves)
        int alphaLeavesEmeralds = 1 + r.nextInt(64);
        list.add(new TradeOffer(
                new TradedItem(Items.EMERALD, alphaLeavesEmeralds),
                SpamtonSpecialItems.alphaLeaves(),
                MAX_USES, XP, PRICE_MULT
        ));

        // 99999 emeralds -> Admin-b-gone (splash Instant Health 125, kills via overflow)
        list.add(new TradeOffer(
                new TradedItem(Items.EMERALD, 99999),
                SpamtonSpecialItems.adminBGonePotion(),
                MAX_USES, XP, PRICE_MULT
        ));

        int theKromerCost = 1 + r.nextInt(64);
        ItemStack theDisplay = new ItemStack(Items.BARRIER, 1);
        theDisplay.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("The"));
        list.add(new TradeOffer(
                new TradedItem(Items.PAPER, theKromerCost),
                theDisplay,
                1, XP, PRICE_MULT
        ));

        return list;
    }

    /** Picks a random vanilla item for "The" slot-machine display. Name set to "The" so trade detection still works. */
    public static ItemStack getRandomTheDisplayItem() {
        List<Item> items = new ArrayList<>();
        Registries.ITEM.forEach(items::add);
        if (items.isEmpty()) return new ItemStack(Items.BARRIER, 1);
        Item item = items.get(RANDOM.nextInt(items.size()));
        ItemStack stack = new ItemStack(item, 1);
        stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("The"));
        return stack;
    }

    /**
     * Returns a new TradeOfferList with the same offers as current, except the last ("The") offer
     * has its sell-item display replaced by a random item (icon flips every tick).
     */
    public static TradeOfferList replaceTheDisplayOnly(net.minecraft.village.TradeOfferList current) {
        if (current.isEmpty()) return current;
        net.minecraft.village.TradeOfferList next = new net.minecraft.village.TradeOfferList();
        for (int i = 0; i < current.size() - 1; i++) {
            next.add(current.get(i));
        }
        TradeOffer theOffer = current.get(current.size() - 1);
        int kromerCost = theOffer.getOriginalFirstBuyItem().getCount();
        next.add(new TradeOffer(
                new TradedItem(Items.PAPER, kromerCost),
                getRandomTheDisplayItem(),
                1, XP, PRICE_MULT
        ));
        return next;
    }
}

package com.spamton.trade;

import com.spamton.SpamtonConfig;
import com.spamton.item.KromerItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.BundleContents;

import java.util.List;

public class TheTradeLootGenerator {

    private static final RandomSource RANDOM = RandomSource.create();

    /** Picks one entry by weight from config and returns a bundle named "The" containing that one item. */
    public static ItemStack generate() {
        return generate(null);
    }

    public static ItemStack generate(RegistryAccess registryAccess) {
        if (SpamtonConfig.theBundleUseAllSurvivalItems) {
            List<Item> pool = TheBundleAllowedItems.getAllowedItems();
            if (pool.isEmpty()) return fallbackBundle();
            Item item = pool.get(RANDOM.nextInt(pool.size()));
            ItemStack content = new ItemStack(item, 1);
            if (content.isEmpty()) return fallbackBundle();
            return createTheBundle(content);
        }
        List<SpamtonConfig.TheBundleLootEntry> loot = SpamtonConfig.theBundleLoot;
        if (loot.isEmpty()) return fallbackBundle();
        int totalWeight = 0;
        for (SpamtonConfig.TheBundleLootEntry e : loot) totalWeight += e.weight;
        if (totalWeight <= 0) return fallbackBundle();
        int r = RANDOM.nextInt(totalWeight);
        for (SpamtonConfig.TheBundleLootEntry e : loot) {
            if (r < e.weight) {
                ItemStack content = createStackForEntry(e);
                if (content.isEmpty()) continue;
                return createTheBundle(content);
            }
            r -= e.weight;
        }
        return fallbackBundle();
    }

    /** Empty bundle named "The" for the merchant slot (no contents = no preview). */
    public static ItemStack createEmptyTheBundle() {
        ItemStack bundle = new ItemStack(Items.BUNDLE, 1);
        bundle.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("The"));
        bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of()));
        return bundle;
    }

    /** Creates a bundle ItemStack named "The" containing exactly the given item. */
    public static ItemStack createTheBundle(ItemStack singleContent) {
        if (singleContent.isEmpty()) return fallbackBundle();
        ItemStack bundle = new ItemStack(Items.BUNDLE, 1);
        bundle.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("The"));
        bundle.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(List.of(singleContent.copy())));
        return bundle;
    }

    private static ItemStack createStackForEntry(SpamtonConfig.TheBundleLootEntry entry) {
        if ("kromer".equals(entry.id)) {
            return KromerItem.createKromer(entry.count);
        }
        String id = entry.id.contains(":") ? entry.id : "minecraft:" + entry.id;
        try {
            Item item = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null);
            if (item == null || item == Items.AIR) return ItemStack.EMPTY;
            return new ItemStack(item, entry.count);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack fallbackBundle() {
        return createTheBundle(KromerItem.createKromer(1));
    }
}

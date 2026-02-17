package com.spamton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpamtonConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static final String DEFAULT_JSON = """
        {
          "placeholderItems": [],
          "kromerBaseItem": "minecraft:paper",
          "enableRealtimeFluctuation": true,
          "disabledTrades": [],
          "theBundleUseAllSurvivalItems": true,
          "customTrades": [
            {
              "buy": { "id": "minecraft:emerald", "count": 1 },
              "sell": { "id": "minecraft:obsidian", "count": 4, "components": { "custom_name": { "text": "FR1ED PIPIS", "italic": false }, "lore": [[{ "text": "IT'S GOT A [[Consistency]] AND [[Edibility]] JUST LIKE THAT [Obsidian] GARBAGE!!", "italic": false }]] } },
              "maxUses": 12
            },
            {
              "sell": { "id": "minecraft:obsidian", "count": 1 },
              "maxUses": 999,
              "randomKromerPrice": true,
              "minKromer": 5,
              "maxKromer": 32
            }
          ]
        }
        """;

    public static List<String> placeholderItems = new ArrayList<>(List.of(
            "minecraft:gold_nugget", "minecraft:diamond", "minecraft:chorus_fruit",
            "minecraft:ender_pearl", "minecraft:blaze_rod"
    ));
    public static String kromerBaseItem = "minecraft:paper";
    public static boolean enableRealtimeFluctuation = true;

    /** Built-in trade ids that can be disabled. */
    public static final List<String> BUILTIN_TRADE_IDS = List.of(
            "kromer_for_emeralds", "dog_armor", "fried_pipis", "alpha_leaves", "admin_b_gone", "the"
    );
    public static List<String> disabledTrades = new ArrayList<>();
    /** When true, "The" bundle pool is every survival item (except creative/structure/End); when false, use theBundleLoot weights. */
    public static boolean theBundleUseAllSurvivalItems = true;
    /** Weighted entries for "The" trade bundle when theBundleUseAllSurvivalItems is false. id "kromer" = 1 Kromer. */
    public static List<TheBundleLootEntry> theBundleLoot = new ArrayList<>(List.of(
            new TheBundleLootEntry("kromer", 1, 99),
            new TheBundleLootEntry("minecraft:gold_nugget", 1, 1)
    ));

    public static List<CustomTradeEntry> customTrades = new ArrayList<>();

    public static void load() {
        try {
            configPath = Path.of("config", SpamtonMod.MOD_ID + ".json");
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());
                Files.writeString(configPath, DEFAULT_JSON);
            }
            String json = Files.readString(configPath);
            JsonObject root = GSON.fromJson(json, JsonObject.class);
            if (root.has("placeholderItems") && root.get("placeholderItems").isJsonArray()) {
                placeholderItems.clear();
                for (var e : root.getAsJsonArray("placeholderItems"))
                    placeholderItems.add(e.getAsString());
            }
            if (root.has("kromerBaseItem"))
                kromerBaseItem = root.get("kromerBaseItem").getAsString();
            if (root.has("enableRealtimeFluctuation"))
                enableRealtimeFluctuation = root.get("enableRealtimeFluctuation").getAsBoolean();
            if (root.has("disabledTrades") && root.get("disabledTrades").isJsonArray()) {
                disabledTrades.clear();
                for (var e : root.getAsJsonArray("disabledTrades"))
                    disabledTrades.add(e.getAsString());
            }
            if (root.has("theBundleUseAllSurvivalItems"))
                theBundleUseAllSurvivalItems = root.get("theBundleUseAllSurvivalItems").getAsBoolean();
            com.spamton.trade.TheBundleAllowedItems.clearCache();
            if (root.has("theBundleLoot") && root.get("theBundleLoot").isJsonArray()) {
                theBundleLoot.clear();
                for (var e : root.getAsJsonArray("theBundleLoot")) {
                    if (!e.isJsonObject()) continue;
                    TheBundleLootEntry entry = parseTheBundleLootEntry(e.getAsJsonObject());
                    if (entry != null) theBundleLoot.add(entry);
                }
            }
            if (root.has("customTrades") && root.get("customTrades").isJsonArray()) {
                customTrades.clear();
                for (var e : root.getAsJsonArray("customTrades")) {
                    if (!e.isJsonObject()) continue;
                    JsonObject o = e.getAsJsonObject();
                    CustomTradeEntry entry = parseCustomTrade(o);
                    if (entry != null) customTrades.add(entry);
                }
            }
        } catch (IOException e) {
            SpamtonMod.LOGGER.warn("Could not load config, using defaults", e);
        }
    }

    public static void save() {
        try {
            if (configPath == null) configPath = Path.of("config", SpamtonMod.MOD_ID + ".json");
            JsonObject root = new JsonObject();
            JsonArray arr = new JsonArray();
            for (String s : placeholderItems) arr.add(s);
            root.add("placeholderItems", arr);
            root.addProperty("kromerBaseItem", kromerBaseItem);
            root.addProperty("enableRealtimeFluctuation", enableRealtimeFluctuation);
            JsonArray disabled = new JsonArray();
            for (String s : disabledTrades) disabled.add(s);
            root.add("disabledTrades", disabled);
            JsonArray theLoot = new JsonArray();
            for (TheBundleLootEntry e : theBundleLoot) {
                JsonObject o = new JsonObject();
                o.addProperty("id", e.id);
                o.addProperty("count", e.count);
                o.addProperty("weight", e.weight);
                theLoot.add(o);
            }
            root.add("theBundleLoot", theLoot);
            JsonArray custom = new JsonArray();
            for (CustomTradeEntry e : customTrades) {
                JsonObject o = new JsonObject();
                if (e.buyId != null) {
                    JsonObject buy = new JsonObject();
                    buy.addProperty("id", e.buyId);
                    buy.addProperty("count", e.buyCount);
                    o.add("buy", buy);
                }
                if (e.buyBId != null) {
                    JsonObject buyB = new JsonObject();
                    buyB.addProperty("id", e.buyBId);
                    buyB.addProperty("count", e.buyBCount);
                    o.add("buyB", buyB);
                }
                JsonObject sell = new JsonObject();
                sell.addProperty("id", e.sellId);
                sell.addProperty("count", e.sellCount);
                if (e.sellName != null || (e.sellLore != null && !e.sellLore.isEmpty())) {
                    JsonObject comp = new JsonObject();
                    if (e.sellName != null) {
                        JsonObject name = new JsonObject();
                        name.addProperty("text", e.sellName);
                        name.addProperty("italic", false);
                        comp.add("custom_name", name);
                    }
                    if (e.sellLore != null && !e.sellLore.isEmpty()) {
                        JsonArray loreArr = new JsonArray();
                        for (String line : e.sellLore) {
                            JsonArray lineArr = new JsonArray();
                            JsonObject seg = new JsonObject();
                            seg.addProperty("text", line);
                            seg.addProperty("italic", false);
                            lineArr.add(seg);
                            loreArr.add(lineArr);
                        }
                        comp.add("lore", loreArr);
                    }
                    sell.add("components", comp);
                }
                o.add("sell", sell);
                o.addProperty("maxUses", e.maxUses);
                if (e.randomKromerPrice) o.addProperty("randomKromerPrice", true);
                if (e.minKromer != null) o.addProperty("minKromer", e.minKromer);
                if (e.maxKromer != null) o.addProperty("maxKromer", e.maxKromer);
                custom.add(o);
            }
            root.add("customTrades", custom);
            Files.writeString(configPath, GSON.toJson(root));
        } catch (IOException e) {
            SpamtonMod.LOGGER.warn("Could not save config", e);
        }
    }

    /** Parses one recipe: villager-style { buy?: {id, count}, buyB?: {id, count}, sell: {id, count, components?}, maxUses?, randomKromerPrice? }. Also legacy costItem/resultItem. */
    private static CustomTradeEntry parseCustomTrade(JsonObject o) {
        CustomTradeEntry entry = new CustomTradeEntry();
        entry.randomKromerPrice = o.has("randomKromerPrice") && o.get("randomKromerPrice").getAsBoolean();

        if (o.has("sell") && o.get("sell").isJsonObject()) {
            JsonObject sell = o.getAsJsonObject("sell");
            entry.sellId = normalizeItemId(sell.has("id") ? sell.get("id").getAsString() : null);
            entry.sellCount = sell.has("count") ? sell.get("count").getAsInt() : 1;
            if (sell.has("components") && sell.get("components").isJsonObject()) {
                JsonObject comp = sell.getAsJsonObject("components");
                if (comp.has("custom_name")) {
                    if (comp.get("custom_name").isJsonPrimitive())
                        entry.sellName = comp.get("custom_name").getAsString();
                    else if (comp.get("custom_name").isJsonObject() && comp.getAsJsonObject("custom_name").has("text"))
                        entry.sellName = comp.getAsJsonObject("custom_name").get("text").getAsString();
                }
                if (comp.has("lore") && comp.get("lore").isJsonArray()) {
                    entry.sellLore = new ArrayList<>();
                    for (var lineEl : comp.getAsJsonArray("lore")) {
                        if (!lineEl.isJsonArray()) continue;
                        StringBuilder line = new StringBuilder();
                        for (var segEl : lineEl.getAsJsonArray()) {
                            if (segEl.isJsonObject() && segEl.getAsJsonObject().has("text"))
                                line.append(segEl.getAsJsonObject().get("text").getAsString());
                        }
                        if (line.length() > 0) entry.sellLore.add(line.toString());
                    }
                }
            }
        }

        if (o.has("buy") && o.get("buy").isJsonObject()) {
            JsonObject buy = o.getAsJsonObject("buy");
            entry.buyId = normalizeItemId(buy.has("id") ? buy.get("id").getAsString() : null);
            entry.buyCount = buy.has("count") ? buy.get("count").getAsInt() : 1;
            if (o.has("buyB") && o.get("buyB").isJsonObject()) {
                JsonObject buyB = o.getAsJsonObject("buyB");
                entry.buyBId = normalizeItemId(buyB.has("id") ? buyB.get("id").getAsString() : null);
                entry.buyBCount = buyB.has("count") ? buyB.get("count").getAsInt() : 0;
            }
        } else if (o.has("costItem") && o.has("resultItem")) {
            entry.buyId = normalizeItemId(o.get("costItem").getAsString());
            entry.buyCount = o.has("costCount") ? o.get("costCount").getAsInt() : 1;
            entry.sellId = normalizeItemId(o.get("resultItem").getAsString());
            entry.sellCount = o.has("resultCount") ? o.get("resultCount").getAsInt() : 1;
            if (o.has("costItemB")) entry.buyBId = normalizeItemId(o.get("costItemB").getAsString());
            if (o.has("costCountB")) entry.buyBCount = o.get("costCountB").getAsInt();
        }

        if (entry.sellId == null) return null;
        if (!entry.randomKromerPrice && entry.buyId == null) return null;
        entry.maxUses = o.has("maxUses") ? o.get("maxUses").getAsInt() : 999;
        if (o.has("minKromer")) entry.minKromer = o.get("minKromer").getAsInt();
        if (o.has("maxKromer")) entry.maxKromer = o.get("maxKromer").getAsInt();
        return entry;
    }

    private static String normalizeItemId(String id) {
        if (id == null || id.isBlank()) return null;
        return id.contains(":") ? id : "minecraft:" + id;
    }

    private static TheBundleLootEntry parseTheBundleLootEntry(JsonObject o) {
        if (!o.has("id") || !o.has("weight")) return null;
        String id = o.get("id").getAsString();
        if (id.isBlank()) return null;
        int count = o.has("count") ? Math.max(1, o.get("count").getAsInt()) : 1;
        int weight = o.get("weight").getAsInt();
        if (weight <= 0) return null;
        return new TheBundleLootEntry(id, count, weight);
    }

    /** One weighted entry for "The" bundle loot. id "kromer" = Kromer (paper). */
    public static class TheBundleLootEntry {
        public final String id;
        public final int count;
        public final int weight;

        public TheBundleLootEntry(String id, int count, int weight) {
            this.id = id;
            this.count = count;
            this.weight = weight;
        }
    }

    /** One custom trade: villager-style buy/sell. */
    public static class CustomTradeEntry {
        public String buyId;
        public int buyCount = 1;
        public String buyBId;
        public int buyBCount = 0;
        public String sellId;
        public int sellCount = 1;
        public String sellName;
        public List<String> sellLore;
        public int maxUses = 999;
        /** When true, first cost is a random amount of kromer (paper) instead of buy/buyCount. */
        public boolean randomKromerPrice = false;
        /** Optional min/max kromer when randomKromerPrice is true; defaults 1–64 if unset. */
        public Integer minKromer;
        public Integer maxKromer;
    }
}

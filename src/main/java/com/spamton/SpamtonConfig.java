package com.spamton;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SpamtonConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static final String DEFAULT_JSON = """
        {
          "placeholderItems": ["minecraft:gold_nugget", "minecraft:diamond", "minecraft:chorus_fruit", "minecraft:ender_pearl", "minecraft:blaze_rod"],
          "kromerBaseItem": "minecraft:paper",
          "enableRealtimeFluctuation": true
        }
        """;

    public static List<String> placeholderItems = new ArrayList<>(List.of(
            "minecraft:gold_nugget", "minecraft:diamond", "minecraft:chorus_fruit",
            "minecraft:ender_pearl", "minecraft:blaze_rod"
    ));
    public static String kromerBaseItem = "minecraft:paper";
    public static boolean enableRealtimeFluctuation = true;

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
            Files.writeString(configPath, GSON.toJson(root));
        } catch (IOException e) {
            SpamtonMod.LOGGER.warn("Could not save config", e);
        }
    }
}

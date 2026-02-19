package com.spamton.trade;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spamton.SpamtonMod;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

/**
 * Applies item data components from JSON to an ItemStack using the game's
 * DataComponentType codecs, so any component the game supports can be parsed.
 */
public final class ItemStackComponents {

    private ItemStackComponents() {}

    /**
     * Applies all entries from the given components JSON to the stack.
     * Keys can be unnamespaced (e.g. "custom_name") or namespaced (e.g. "minecraft:enchantments").
     * Unknown keys or decode failures are logged and skipped.
     */
    @SuppressWarnings("unchecked")
    public static void apply(ItemStack stack, JsonObject components, RegistryAccess registryAccess) {
        if (components == null || components.isEmpty()) return;
        var ops = registryAccess != null
                ? RegistryOps.create(JsonOps.INSTANCE, registryAccess)
                : JsonOps.INSTANCE;
        for (var entry : components.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value == null) continue;
            String id = key.contains(":") ? key : "minecraft:" + key;
            DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(Identifier.parse(id)).orElse(null);
            if (type == null) {
                SpamtonMod.LOGGER.debug("Unknown item component in config: {}", id);
                continue;
            }
            try {
                Codec<Object> codec = (Codec<Object>) type.codec();
                var result = codec.parse(ops, value);
                result.result().ifPresent(v -> stack.set((DataComponentType<Object>) type, v));
                result.error().ifPresent(err -> SpamtonMod.LOGGER.warn("Failed to parse component {}: {}", id, err.message()));
            } catch (Exception e) {
                SpamtonMod.LOGGER.warn("Error applying component {}: {}", id, e.getMessage());
            }
        }
    }
}

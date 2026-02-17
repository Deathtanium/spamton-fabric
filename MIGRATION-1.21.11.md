# Migrating This Mod to Minecraft 1.21.11

The build is **configured** for 1.21.11 (Gradle, Loom, Fabric Loader/API, mod metadata).  
Minecraft 1.21.11 uses **reorganized package and class names**. The current source was written for 1.21.1, so imports and type references must be updated.

## Build (already done)

- **gradle.properties**: `minecraft_version=1.21.11`, `loader_version=0.18.1`, `loom_version=1.14.10`, `fabric_version=0.141.3+1.21.11`
- **fabric.mod.json**: `minecraft": "~1.21.11"`, `fabricloader": ">=0.18.0"`
- **Gradle**: Use `./gradlew` (Gradle 9.2.0). Run `./gradlew build` after code is migrated.

## Code changes required

1.21.11 uses `net.minecraft.world.*` and related renames. Examples:

| 1.21.1 (current code)              | 1.21.11 (target)                                |
|------------------------------------|--------------------------------------------------|
| `net.minecraft.entity.passive.VillagerEntity` | `net.minecraft.world.entity.npc.villager.Villager` |
| `net.minecraft.entity.passive.MerchantEntity` | `net.minecraft.world.entity.npc.AbstractVillager` (or the merchant interface used by Villager) |
| `net.minecraft.entity.player.PlayerEntity`    | `net.minecraft.world.entity.player.Player`      |
| `net.minecraft.item.Item`                    | `net.minecraft.world.item.Item`                  |
| `net.minecraft.item.ItemStack`                | `net.minecraft.world.item.ItemStack`              |
| `net.minecraft.item.Items`                    | `net.minecraft.world.item.Items`                  |
| `net.minecraft.village.TradeOffer`            | Check `net.minecraft.world.entity.npc.villager` / trade packages |
| `net.minecraft.village.TradeOfferList`       | Same package as above                            |
| `net.minecraft.village.TradedItem`           | Same package as above                            |
| `net.minecraft.component.*`                   | Check `net.minecraft.world.item.component` or equivalent |
| `net.minecraft.registry.Registries`           | May stay or move; check in-game package          |
| `net.minecraft.text.Text` / `Style`           | Check `net.minecraft.network.chat` or equivalent |
| `net.minecraft.enchantment.*`                 | Check `net.minecraft.world.item.enchantment`     |
| `net.minecraft.entity.effect.*`               | Check `net.minecraft.world.effect`               |
| `net.minecraft.inventory.Inventory`           | Check `net.minecraft.world.Container` or equivalent |

**How to find exact names:**  
After a failed compile, use your IDE “Go to definition” on the Minecraft classes, or inspect the Minecraft jar:

```bash
jar tf .gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-*/1.21.11-*/*.jar | grep -i villager
```

Then update all Java files to use the new imports and types. After that, `./gradlew build` should succeed.

## References

- [Fabric for Minecraft 1.21.11](https://fabricmc.net/2025/12/05/12111.html)
- [Porting to 1.21.11 (Fabric docs)](https://docs.fabricmc.net/develop/porting/current)
- [NeoForge 1.21.10 → 1.21.11 migration primer](https://github.com/neoforged/.github/blob/main/primers/1.21.11/index.md) (vanilla renames, useful for class/package mapping)

# Spamton (Fabric Mod)

Server-side Fabric mod adding a Spamton-inspired merchant NPC.

## Features

- **Spamton villager**: Visible villager with very high health. Spawn with `/spamton spawn`.
- **Block-display glasses**: Pink and yellow stained glass blocks that follow the villager's head.
- **Unkillable spectacle**: When hit, Spamton "explodes" into mini baby villagers that scatter and run, then explode and despawn. Spamton goes invisible for 1 minute, then reappears. Minis and Spamton take no damage.
- **Restart fallback**: On world load, any stuck recovery state is cleared and orphaned minis are removed.
- **Dynamic trades**: Each time you open the trade menu, offers are regenerated. Kromer (custom paper) trade is always first; all prices are random 1–64.
- **"The" trade**: Mystery trade that gives a random item (with random enchant), random potion, or random spawn egg (90% / 9% / 1%). One use per restock.

## Commands

- `/spamton spawn` – Spawn Spamton at your feet (OP 2).
- `/spamton reload` – Reload config (OP 2).
- `/spamton givekromer [player] [amount]` – Give kromer (OP 2).

## Config

`config/spamton.json`:

- `placeholderItems`: Item IDs for the placeholder trades (kromer → item).
- `kromerBaseItem`: Base item for kromer (default `minecraft:paper`).
- `enableRealtimeFluctuation`: Whether to try live price updates while the trade screen is open (best-effort).

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API

## Building

Not validated by compilation yet. Use Fabric Loom and run `gradlew build` when the project is ready.

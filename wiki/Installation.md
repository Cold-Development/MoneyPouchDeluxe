# Installation

## Requirements

| | |
|---|---|
| **Server** | Spigot, Paper, Purpur or Folia, **1.20+** |
| **Java** | 17 or newer |
| **Required plugins** | None |
| **Optional plugins** | [Nexo](https://nexomc.com) (Paper only) for custom items and glyphs |

To pay out a currency, the plugin that owns that currency must be installed (for example EssentialsX + Vault for money, PlayerPoints for points). MoneyPouchDeluxe doesn't hook into them directly: it just runs their "give" command from the console. See [Economies](Custom-Economy-Types).

## Steps

1. Download the jar from [SpigotMC](https://www.spigotmc.org/resources/moneypouchdeluxe.118795/).
2. Stop the server and put the jar in the `plugins` folder.
3. Start the server. The `plugins/MoneyPouchDeluxe/` folder is generated.
4. Edit the files (see below), then run `/mpa reload` in game or in the console.
5. Test: `/mp moneypouch` gives you the example pouch. Right-click it.

> 💡 Always use `/mpa reload` after editing the files. Avoid `/reload` and plugin managers (PlugMan, ...), which can break plugins in general.

## Generated files

```
plugins/MoneyPouchDeluxe/
├── config.yml              ← sounds, title animation, error handling, messages
├── pouches.yml             ← your pouches (one block per pouch)
└── customeconomytype/      ← one file per currency
    ├── vault.yml           ← money (eco give)          → economytype: "VAULT"
    ├── playerpoints.yml    ← PlayerPoints (points give) → economytype: "PlayerPoints"
    ├── examplecustomeconomy.yml  ← diamonds, as an example
    └── README.txt
```

| File | You edit it to... | Page |
|---|---|---|
| `pouches.yml` | add, remove or change pouches | [Creating a Pouch](Creating-a-Pouch), [Pouch Configuration](Configuration-for-Pouches) |
| `customeconomytype/*.yml` | add a currency or change how it is paid | [Economies](Custom-Economy-Types) |
| `config.yml` | change messages, sounds, the title | [Main Configuration](Configuration) |

### The example pouches

`pouches.yml` comes with three pouches to learn from:

| Pouch id | Item | Prize | Currency | Permission |
|---|---|---|---|---|
| `moneypouch` | Chest | 5,000 – 15,000 | `VAULT` (money) | none, anyone can open it |
| `pointspouch` | Custom head | 5 – 15 | `PlayerPoints` | none (`permission-required: false`) |
| `xppouch` | Ender chest | 10 – 150 | `XP` | `moneypouch.pouches.xppouch` |

- `moneypouch` needs an economy plugin that has `/eco give` (EssentialsX, CMI, ...). Without one, the player gets an error message and the console logs what they should have received.
- `pointspouch` needs PlayerPoints.
- `xppouch` works out of the box, but regular players can't open it until you give them the permission. See [Pouch Permissions](Pouch-Permissions).

Delete or change the examples you don't need.

## Updating

1. Stop the server, replace the old jar with the new one, start the server.
2. Your files are **not** overwritten. New options get their default value until you add them yourself.
3. Check the console for warnings (a pouch that was skipped, an unknown material, ...).

### Updating from 1.5.0 or older

Version 1.5.1 changed where things live:

- **Pouches moved from `config.yml` to `pouches.yml`.** This happens automatically on the first start: the old `pouches.tier` section is moved, and a backup of your old config is kept as `config.yml.before-pouches-yml`. In `pouches.yml` the pouches are at the top level (no more `pouches:` → `tier:`).
- **Every currency except XP is now a file in `customeconomytype/`.** The built-in Vault, PlayerPoints, TokenManager and LemonMobCoins hooks were removed.
  - If you are updating an existing install, the `customeconomytype/` folder already exists, so `vault.yml` and `playerpoints.yml` are **not** generated. Create them yourself (copy them from [Economies](Custom-Economy-Types#ready-to-use-files)), otherwise the pouches using them are skipped with a warning.
  - Then run `/mpa reload` and check `/mpa list`.
- **The pouch shop and the holograms were removed.** If you used the shop, sell pouches with a shop plugin instead: see [Using Pouches with Other Plugins](Using-Pouches-with-Other-Plugins).

Pouches already in players' inventories keep working, as long as a pouch with the same id still exists.

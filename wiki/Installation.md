# Installation

## Requirements

| | |
|---|---|
| **Server** | Spigot, Paper, Purpur or Folia, **1.17+** (up to 26.x) |
| **Java** | 17 or newer |
| **Required plugins** | None |
| **Optional plugins** | Vault, PlayerPoints, PlaceholderAPI, [Nexo](https://nexomc.com) (Paper only) |

To pay out a currency, the plugin that owns that currency must be installed (for example EssentialsX + Vault for money, PlayerPoints for points). Vault and PlayerPoints are paid through their API; any other currency through its "give" command. See [Economies](Custom-Economy-Types).

## Steps

1. Download the jar from [SpigotMC](https://www.spigotmc.org/resources/moneypouchdeluxe.118795/).
2. Stop the server and put the jar in the `plugins` folder.
3. Start the server. The `plugins/MoneyPouchDeluxe/` folder is generated.
4. Edit the files (see below), then run `/mp reload` in game or in the console.
5. Test: `/mp give moneypouch` gives you the example pouch. Right-click it.

> 💡 Always use `/mp reload` after editing the files. Avoid `/reload` and plugin managers (PlugMan, ...), which can break plugins in general.

## Generated files

```
plugins/MoneyPouchDeluxe/
├── config.yml              ← language, sounds, title animation, error handling, transaction log, database
├── pouches.yml             ← your pouches (one block per pouch)
├── customeconomytype/      ← one file per currency
│   ├── vault.yml           ← money through Vault             → economytype: "VAULT"
│   ├── playerpoints.yml    ← points through PlayerPoints     → economytype: "PlayerPoints"
│   ├── examplecustomeconomy.yml  ← diamonds through a command, as an example
│   └── README.txt
├── locale/                 ← the messages, one file per language
│   ├── en_US.yml
│   └── ro_RO.yml
├── commands/
│   └── moneypouchdeluxe.yml ← command names and aliases
├── logs/                   ← transaction log, one file per day
└── moneypouchdeluxe.db     ← player statistics (SQLite)
```

| File | You edit it to... | Page |
|---|---|---|
| `pouches.yml` | add, remove or change pouches | [Creating a Pouch](Creating-a-Pouch), [Pouch Configuration](Configuration-for-Pouches) |
| `customeconomytype/*.yml` | add a currency or change how it is paid | [Economies](Custom-Economy-Types) |
| `config.yml` | change the language, sounds, the title, the log | [Main Configuration](Configuration) |
| `locale/<language>.yml` | change the messages | [Messages and Languages](Messages-and-Languages) |
| `commands/moneypouchdeluxe.yml` | rename commands, change aliases, disable subcommands | [Commands and Permissions](Commands-and-Permissions#renaming-commands) |

### The example pouches

`pouches.yml` comes with three pouches to learn from:

| Pouch id | Item | Prize | Currency | Permission |
|---|---|---|---|---|
| `moneypouch` | Chest | 5,000 – 15,000 | `VAULT` (money) | none, anyone can open it |
| `pointspouch` | Custom head | 5 – 15 | `PlayerPoints` | none (`permission-required: false`) |
| `xppouch` | Ender chest | 10 – 150 | `XP` | `moneypouch.pouches.xppouch` |

- `moneypouch` needs Vault and an economy plugin (EssentialsX, CMI, ...). If the payment fails, the player gets an error message and the console logs what they should have received.
- `pointspouch` needs PlayerPoints.
- `xppouch` works out of the box, but regular players can't open it until you give them the permission. See [Pouch Permissions](Pouch-Permissions).

Delete or change the examples you don't need.

## Updating

1. Stop the server, replace the old jar with the new one, start the server.
2. **Your edits are kept.** New settings are added to `config.yml`, `vault.yml` and `playerpoints.yml` with their comments, and new messages to the `locale/` files, without changing anything you wrote. The console lists what was added, e.g. `config.yml: added 1 new setting(s): transaction-log`.
3. Check the console for warnings (a pouch that was skipped, an unknown material, ...).

### Updating from 1.x to 2.0

Everything is migrated on the first start; nothing has to be done by hand.

- **Messages move from `config.yml` to `locale/en_US.yml`**, prefix and customised messages included. A backup of the old config is kept as `config.yml.before-locale`. See [Messages and Languages](Messages-and-Languages).
- **Pouches move from `config.yml` to `pouches.yml`** (when updating from 1.5.0 or older). A backup is kept as `config.yml.before-pouches-yml`.
- **Commands**: `/mp <pouch> [player] [amount]` keeps working, so crates, votes and NPCs don't need changes. `/mpa list`, `/mpa economies` and `/mpa reload` keep working too. The new commands are `/mp give`, `/mp list`, `/mp economies`, `/mp reload` and `/mp help`.
- **Vault and PlayerPoints** are now paid through their API: `hook: vault` / `hook: playerpoints` is added to your `vault.yml` / `playerpoints.yml`. Your command stays in the file and is used if the plugin is missing. Set `hook: command` to keep using the command. `VAULT` and `PlayerPoints` pouches also work without these files.
- **`attributemodifiers`** on pouches was removed (it broke on 1.21.3+). `options.show-receive-message` was removed: set `receive-item: ''` to disable that message.

Pouches already in players' inventories keep working, as long as a pouch with the same id still exists.

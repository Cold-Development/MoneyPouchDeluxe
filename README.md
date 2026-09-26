![image](https://imgur.com/FRoQbVI.png)<br>
![Version](https://img.shields.io/badge/Version-v1.5.1-blue?color=799aca)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20%2B-green.svg)
![Folia](https://img.shields.io/badge/Folia-supported-green.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)

# 💰 MoneyPouchDeluxe

**MoneyPouchDeluxe** adds pouches that players right-click to win a random amount of any currency, revealed digit by digit in an animated title.

- Pay out **any currency** through a console command. Economy plugins are not required as dependencies.
- **Nexo** support: custom item models and `<glyph:id>` icons in names, lore, messages and titles.
- Continuation of the original [MoneyPouch](https://github.com/LMBishop/MoneyPouch) by LMBishop.

![opening](https://github.com/user-attachments/assets/a7889779-bceb-42c8-b573-8d05e9d49070)

## 📦 Features

- **Custom pouches** in their own `pouches.yml`: name, lore, amount range, item (vanilla, custom head texture or Nexo item) and permission.
- **Any economy, no hard dependencies**:
  - XP is built in.
  - Every other currency is a small file in `customeconomytype/` that runs a command (`eco give`, `points give`, anything).
  - Each currency has its own name, prefix and suffix, with colors.
- **Failed payouts are logged**: if an economy's command is missing, the console logs the player, the amount and the command, and the player is told.
- **Animated title** reveal: obfuscated digits, left-to-right or right-to-left, with a configurable number separator (`1,000` / `1.000` / `1 000`).
- **Nexo integration** (optional):
  - `item: "nexo:<id>"` uses a Nexo custom item as the pouch.
  - `<glyph:id>` renders Nexo icons everywhere: item names, lore, chat messages, titles.
- **Colors**: legacy `&a`, hex `&#RRGGBB` / `<#RRGGBB>`, gradients and rainbow.
- **Messages**: one shared `prefix`, and any message can be disabled by setting it to `""`.
- **Never lose a pouch**: if the inventory is full, it is dropped at the player's feet.
- **One pouch at a time**: players can't open a second pouch while one is still being revealed.
- **Folia supported**.

## 🛠️ Installation

1. Download the latest version from [SpigotMC](https://www.spigotmc.org/resources/moneypouchdeluxe.118795/).
2. Put the `.jar` in your server's `plugins` folder.
3. Start the server once to generate the files, then edit them:
   - `config.yml`: sounds, title, messages
   - `pouches.yml`: your pouches
   - `customeconomytype/`: your currencies
4. Run `/mpa reload`.

**Optional:** [Nexo](https://nexomc.com) for custom items and glyphs. It requires Paper.

## ⚙️ Configuration

### `pouches.yml`: pouch tiers

Each top-level key is a pouch id, used by `/mp <id>`.

```yaml
moneypouch:
  name: "&#D93663&lMoney Pouch <glyph:icons_money>"
  item: "CHEST"              # a material, PLAYER_HEAD (+ texture-url) or "nexo:<item_id>"
  texture-url: ""            # base64 head texture, only for PLAYER_HEAD
  pricerange:
    from: 5000               # both ends can be won; from == to gives a fixed amount
    to: 15000
  options:
    economytype: "VAULT"     # XP, or the name of a file in customeconomytype/
    permission-required: moneypouch.pouches.moneypouch   # remove the line (or false) for no permission
  lore:
    - ""
    - "&7Between &f%pricerange_from%$ &7and &f%pricerange_to%$"
    - ""
```

### `customeconomytype/*.yml`: currencies

The file name is the economy id, so `vault.yml` is used with `economytype: "VAULT"`.

```yaml
# customeconomytype/vault.yml
transaction-prize-command: "eco give %player% %prize%"   # runs from the console
name: "money"          # shown by %economy%
prefix: "&#57951E$"    # shown around the amount, in messages and in the title
suffix: ""
```

`vault.yml`, `playerpoints.yml` and a diamonds example are generated on the first start.

### `config.yml`: messages

```yaml
messages:
  prefix: "&8「&6MoneyPouch&8」&7» "      # added in front of every message
  prize-message: "&fYou have received %prefix%%prize%%suffix%&f!"
  already-opening: ""                     # "" disables a message
```

| Placeholder | Where | Value |
|---|---|---|
| `%prize%` | prize-message, reward-error | amount won, with the configured separator |
| `%prefix%` / `%suffix%` | prize-message, reward-error | the economy's prefix / suffix |
| `%economy%` | prize-message, reward-error | the economy's name |
| `%item%` | give-item, receive-item | the pouch's name |
| `%player%` | give-item, full-inv | the target player |
| `%pricerange_from%` / `%pricerange_to%` | pouch lore | the pouch's range |

## 💻 Commands

| Command | Description | Permission |
|---|---|---|
| `/mp <pouch> [player] [amount]` | Give a pouch (to yourself if no player) | `moneypouch.admin` |
| `/mp <pouch> * [amount]` | Give a pouch to every online player | `moneypouch.admin.giveall` |
| `/mpa list` | List all pouches | `moneypouch.admin` |
| `/mpa economies` | List all loaded economies | `moneypouch.admin` |
| `/mpa reload` | Reload `config.yml`, `pouches.yml` and the economies | `moneypouch.admin` |

Aliases: `/moneypouch`, `/cp`, `/moneypouchadmin`, `/cpa`.

## ⚠️ Permissions

- **moneypouch.admin**: give pouches and use admin commands.
- **moneypouch.admin.giveall**: give a pouch to everyone online with `*`.
- **Per pouch**: whatever you set in `options.permission-required`, e.g. `moneypouch.pouches.<id>`.
- Pouches without `permission-required` can be opened by anyone.

## ⬆️ Updating from 1.5.0 or older

- Pouches are moved from `config.yml` to `pouches.yml` automatically on first start. A backup is kept as `config.yml.before-pouches-yml`.
- Currencies other than XP are now files in `customeconomytype/`.
  - If your pouches use `VAULT` or `PlayerPoints`, create `vault.yml` / `playerpoints.yml` there (see the examples above).
  - Then run `/mpa reload`.
- The pouch shop and the holograms were removed. Stacker plugins (RoseStacker, ...) already handle dropped items.

#### Downloads
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/Cold-Development/MoneyPouchDeluxe/total?color=green)

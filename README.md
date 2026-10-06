![image](https://imgur.com/FRoQbVI.png)<br>
![Version](https://img.shields.io/badge/Version-v2.0.0-blue?color=799aca)
![Minecraft](https://img.shields.io/badge/Minecraft-1.17%2B-green.svg)
![Folia](https://img.shields.io/badge/Folia-supported-green.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)

# 💰 MoneyPouchDeluxe

**MoneyPouchDeluxe** adds pouches that players right-click to win a random amount of any currency, revealed digit by digit in an animated title.

- Pay out **money, points, XP or any currency**: Vault and PlayerPoints are hooked directly, anything else through a console command. No hard dependencies.
- **Failed payments are caught**: the player gets an error instead of a fake prize message, the failure is logged, and the pouch can be refunded.
- **Nexo** support: custom item models and `<glyph:id>` icons in names, lore, messages and titles.
- Continuation of the original [MoneyPouch](https://github.com/LMBishop/MoneyPouch) by LMBishop.

![opening](https://github.com/user-attachments/assets/a7889779-bceb-42c8-b573-8d05e9d49070)

## 📦 Features

- **Custom pouches** in their own `pouches.yml`: name, lore, amount range, item (vanilla, custom head texture or Nexo item) and permission.
- **Any economy, no hard dependencies**:
  - XP is built in.
  - **Vault** and **PlayerPoints** are paid through their API, so a refused payment is detected.
  - Any other currency is a small file in `customeconomytype/` that runs a command (`give %player% diamond %prize%`, anything).
  - Each currency has its own name, prefix and suffix, with colors.
- **Open a whole stack**: sneak + right click opens every pouch in your hand at once, with one reveal and the total.
- **Failed payments are handled**: the player is told, the console logs the player, amount and reason, and the pouch can be refunded (`refund-pouch`).
- **Transaction log**: every pouch opened and given is written to `logs/<date>.log`, one file per day, deleted after a configurable number of days.
- **PlaceholderAPI**: statistics placeholders, and placeholders work in every message.
- **Developer API**: events to cancel an opening or change the amount (boosters), and to react to the result.
- **Animated title** reveal: obfuscated digits, left-to-right or right-to-left, with a configurable number separator (`1,000` / `1.000` / `1 000`).
- **Custom head textures** from [minecraft-heads.com](https://minecraft-heads.com/custom-heads): paste the Value, the texture URL or just the hash. Works on old versions and on 26.x.
- **Nexo integration** (optional):
  - `item: "nexo:<id>"` uses a Nexo custom item as the pouch.
  - `<glyph:id>` renders Nexo icons everywhere: item names, lore, chat messages, titles.
- **Languages**: every message is in `locale/` (`en_US`, `ro_RO`, or add your own).
- **Updates keep your edits**: new settings and messages are added to your files automatically, without touching what you changed (comments included).
- **Colors**: legacy `&a`, hex `&#RRGGBB` / `<#RRGGBB>`, gradients and rainbow.
- **Never lose a pouch**: if the inventory is full, it is dropped at the player's feet.
- **One pouch at a time**: players can't open a second pouch while one is still being revealed.
- **Folia supported**.

## 🛠️ Installation

1. Download the latest version from [SpigotMC](https://www.spigotmc.org/resources/moneypouchdeluxe.118795/).
2. Put the `.jar` in your server's `plugins` folder.
3. Start the server once to generate the files, then edit them:
   - `config.yml`: language, sounds, title, transaction log, database
   - `pouches.yml`: your pouches
   - `customeconomytype/`: your currencies
   - `locale/en_US.yml`: your messages
4. Run `/mp reload`.

**Optional:** Vault, PlayerPoints, PlaceholderAPI, [Nexo](https://nexomc.com) (Nexo requires Paper).

## ⚙️ Configuration

### `pouches.yml`: pouch tiers

Each top-level key is a pouch id, used by `/mp <id>`.

```yaml
moneypouch:
  name: "&#D93663&lMoney Pouch <glyph:icons_money>"
  item: "CHEST"              # a material, PLAYER_HEAD (+ texture-url) or "nexo:<item_id>"
  texture-url: ""            # only for PLAYER_HEAD: the Value, texture URL or hash from minecraft-heads.com
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

Optional per pouch: `custommodeldata`, `enchantments` (`minecraft:unbreaking:1`), `itemflags` (`HIDE_ENCHANTS`), `unbreakable`.

### `customeconomytype/*.yml`: currencies

The file name is the economy id, so `vault.yml` is used with `economytype: "VAULT"`.

```yaml
# customeconomytype/vault.yml
hook: vault                                              # vault, playerpoints or command
transaction-prize-command: "eco give %player% %prize%"   # used by hook: command, or if Vault isn't installed
name: "money"          # shown by %economy%
prefix: "&#57951E$"    # shown around the amount, in messages and in the title
suffix: ""
```

`vault.yml`, `playerpoints.yml` and a diamonds example are generated on the first start. `VAULT` and `PlayerPoints` also work without their file, as long as the plugin is installed.

### `locale/<language>.yml`: messages

Pick the language with `locale: en_US` in `config.yml`.

```yaml
prefix: "&8「&6MoneyPouch&8」&7» "      # added in front of most messages
prize-message: "&fYou have received %prefix%%prize%%suffix%&f!"
already-opening: ''                     # '' disables a message
```

| Placeholder | Where | Value |
|---|---|---|
| `%prize%` | prize-message, prize-message-stack, reward-error | amount won, with the configured separator |
| `%prefix%` / `%suffix%` | prize-message, prize-message-stack, reward-error | the economy's prefix / suffix |
| `%economy%` | prize-message, prize-message-stack, reward-error | the economy's name |
| `%amount%` | prize-message-stack, give and receive messages | number of pouches |
| `%item%` | give and receive messages | the pouch's name |
| `%player%` | give messages | the target player |
| `%pricerange_from%` / `%pricerange_to%` | pouch lore | the pouch's range |

PlaceholderAPI placeholders work in every message too.

## 💻 Commands

| Command | Description | Permission |
|---|---|---|
| `/mp give <pouch> [player] [amount]` | Give pouches (to yourself if no player) | `moneypouch.admin` |
| `/mp <pouch> [player] [amount]` | Same, shorter (works like in older versions) | `moneypouch.admin` |
| `/mp give <pouch> * [amount]` | Give pouches to every online player | `moneypouch.admin.giveall` |
| `/mp list` | List all pouches | `moneypouch.admin` |
| `/mp economies` | List all loaded economies | `moneypouch.admin` |
| `/mp reload` | Reload the config, messages, pouches and economies | `moneypouch.admin` |
| `/mp help` | List the commands you can use | |

Aliases: `/moneypouchdeluxe`, `/moneypouch`, `/cp`, `/mpa`, `/cpa`. Command names and aliases can be changed, and subcommands disabled, in `commands/moneypouchdeluxe.yml`.

## ⚠️ Permissions

- **moneypouch.admin**: give pouches and use admin commands.
- **moneypouch.admin.giveall**: give a pouch to everyone online with `*`.
- **moneypouchdeluxe.updates**: get a message on join when a new version is released (`colddev.updates` does it for every Cold Development plugin). OPs and LuckPerms `*` have it.
- **Per pouch**: whatever you set in `options.permission-required`, e.g. `moneypouch.pouches.<id>`.
- Pouches without `permission-required` can be opened by anyone.

## 📊 PlaceholderAPI

| Placeholder | Value |
|---|---|
| `%moneypouch_opened%` | pouches opened |
| `%moneypouch_opened_<pouch>%` | pouches of one kind opened |
| `%moneypouch_won_<economy>%` | amount won in an economy (`won_vault`, `won_xp`, ...) |
| `%moneypouch_won_pouch_<pouch>%` | amount won from one kind of pouch |

Add `_formatted` to any of them for the number with separators (`%moneypouch_won_vault_formatted%` → `1,250,000`). Statistics are saved in SQLite, or MySQL with `mysql-settings` in `config.yml`.

## 🧩 Developer API

```java
@EventHandler
public void onPouchOpen(PouchOpenEvent event) {
    // Cancellable, before the pouch is used. The amount is the total for event.getCount() pouches.
    if (event.getPlayer().hasPermission("vip.booster")) {
        event.setAmount(event.getAmount() * 2);
    }
}

@EventHandler
public void onPouchReward(PouchRewardEvent event) {
    // After the payment: event.isSuccessful(), event.getFailureReason(), event.getAmount()
}
```

`MoneyPouchDeluxeAPI.get()` gives access to the pouches (`getPouches`, `getPouch`), pouch items (`createItem`, `isPouch`, `getPouchId`), `givePouch` and `registerEconomyType` for your own currencies.

## ⬆️ Updating from 1.x

Everything is migrated on the first start; nothing has to be done by hand.

- **Messages** move from `config.yml` to `locale/en_US.yml`, prefix included. A backup is kept as `config.yml.before-locale`.
- **Pouches** move from `config.yml` to `pouches.yml` (from 1.5.0 or older). A backup is kept as `config.yml.before-pouches-yml`.
- **New settings** are added to `config.yml`, `vault.yml` and `playerpoints.yml` with their comments; your values and comments are kept.
- **Commands**: `/mp <pouch> [player] [amount]` keeps working, so crates and NPCs don't need changes. `/mpa list|economies|reload` still work too.
- **Vault / PlayerPoints** pouches now pay through the plugin's API (`hook:` in their file). Set `hook: command` to keep using the command.

#### Downloads
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/Cold-Development/MoneyPouchDeluxe/total?color=green)

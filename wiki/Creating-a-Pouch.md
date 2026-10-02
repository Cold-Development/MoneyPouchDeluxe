# Creating a Pouch

This guide walks you through creating a pouch from scratch. As an example, we'll make a **"Gold Pouch"** that gives between 1,000 and 5,000 money.

> All pouches live in `plugins/MoneyPouchDeluxe/pouches.yml`. Every option is described in detail on [Pouch Configuration](Configuration-for-Pouches).

---

## Step 1: Make sure the currency exists

A pouch pays out in a currency (an "economy"). Run this in game or in the console:

```
/mpa economies
```

You'll see something like:

```
xp XP [/ XP]
vault Custom (/eco give %player% %prize%) [&a$/]
playerpoints Custom (/points give %player% %prize%) [/ Points]
```

The first word of each line is the **economy id** you will put in the pouch. For money we'll use `vault`.

- Is the currency you want missing? Create a file for it in `customeconomytype/`: see [Economies](Custom-Economy-Types). It takes one line.
- Ids are **not** case-sensitive: `VAULT`, `Vault` and `vault` are the same.

## Step 2: Pick an id for the pouch

The id is the name of the block in `pouches.yml`, and the name you'll use in `/mp <id>`.

- Use lowercase letters, numbers, `-` or `_`. **No spaces and no dots.**
- Choose it carefully: every pouch item a player owns remembers its id. If you rename it later, the pouches already given out stop working (see [Editing a pouch later](#editing-a-pouch-later)).

We'll use `goldpouch`.

## Step 3: Write the pouch

Open `pouches.yml` and add this at the end. **Indentation matters**: use spaces, never tabs, and keep the same alignment as below.

```yaml
goldpouch:
  name: "&6&lGold Pouch &7(Right Click)"
  item: "GOLD_BLOCK"
  pricerange:
    from: 1000
    to: 5000
  options:
    economytype: "VAULT"
  lore:
    - ""
    - "&7Right-click to open!"
    - "&7Contains between &f%pricerange_from%$ &7and &f%pricerange_to%$"
    - ""
```

What each part does:

| Line | Meaning |
|---|---|
| `goldpouch:` | The pouch id. Starts at the very beginning of the line. |
| `name` | The item name. Colors allowed, see [Colors and Formatting](Colors-and-Formatting). |
| `item` | The item it looks like. Any [Material](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html) in CAPITALS, a custom head (`PLAYER_HEAD` + `texture-url`) or a Nexo item (`nexo:<id>`). |
| `pricerange.from` / `to` | The smallest and largest amount that can be won. **Both are included.** Set them equal for a fixed amount. |
| `options.economytype` | The economy id from step 1. |
| `lore` | The description lines. `%pricerange_from%` and `%pricerange_to%` are replaced with the range. `""` is an empty line. |

There is no `permission-required` line, so **anyone can open this pouch**. To restrict it, see [Step 6](#step-6-optional-require-a-permission).

## Step 4: Reload and check

```
/mpa reload
/mpa list
```

`goldpouch` must appear in the list:

```
goldpouch (min: 1000, max: 5000, economy: Custom (/eco give %player% %prize%) [&a$/])
```

**Not in the list?** Look at the console, it says why:

| Console message | Fix |
|---|---|
| `Skipping pouch 'goldpouch': economy type 'X' is missing` | The economy id is wrong, or its file in `customeconomytype/` is missing. Compare with `/mpa economies`. |
| `Unrecognised material: X` | The `item` isn't a valid material. The pouch still loads, but as **stone**. |
| A YAML error / nothing at all | The file has a syntax error (usually tabs or wrong indentation). Paste it into [yamllint.com](https://www.yamllint.com/) to find the line. |

## Step 5: Give it and test it

```
/mp goldpouch                   → gives one to yourself
/mp goldpouch Steve             → gives one to Steve
/mp goldpouch Steve 5           → gives 5 to Steve
/mp goldpouch * 1               → gives one to every online player (needs moneypouch.admin.giveall)
```

Hold it in your **main hand** and right-click (in the air or on a block). The title animation plays, and when it ends the money is paid and you get the `prize-message`.

> 💡 Test with a non-OP account too. OPs have every permission, so a permission mistake won't show up on an OP account.

## Step 6 (optional): Require a permission

To make a pouch only openable by some players (VIPs, a rank, ...), add `permission-required` under `options`:

```yaml
goldpouch:
  ...
  options:
    economytype: "VAULT"
    permission-required: moneypouch.pouches.goldpouch
```

Then give that permission to the players or the group, e.g. with LuckPerms:

```
/lp group vip permission set moneypouch.pouches.goldpouch true
```

Everything about this (and the common mistakes) is on **[Pouch Permissions](Pouch-Permissions)**.

## Step 7 (optional): Make it look nicer

- **Custom head** instead of a block: [Custom heads](Configuration-for-Pouches#custom-heads-player_head--texture-url).
- **Enchantment glow**: add an enchantment and hide it:
  ```yaml
    enchantments:
      - "minecraft:unbreaking:1"
    itemflags:
      - "HIDE_ENCHANTS"
  ```
- **Resource pack model**: `custommodeldata: 1001`, or a Nexo item with `item: "nexo:gold_pouch"`.
- **Gradients and hex colors** in the name: [Colors and Formatting](Colors-and-Formatting).

## The finished pouch

```yaml
goldpouch:
  name: "<g:#FFD700:#FFA500>&lGold Pouch &7(Right Click)"
  item: "GOLD_BLOCK"
  pricerange:
    from: 1000
    to: 5000
  options:
    economytype: "VAULT"
    permission-required: moneypouch.pouches.goldpouch   # remove this line to let everyone open it
  enchantments:
    - "minecraft:unbreaking:1"
  itemflags:
    - "HIDE_ENCHANTS"
  lore:
    - ""
    - "&7Right-click to open!"
    - "&7Contains between &f%pricerange_from%$ &7and &f%pricerange_to%$"
    - ""
```

---

## Editing a pouch later

Every pouch item stores **only its pouch id**. When it's opened, the plugin looks up the pouch with that id in the current `pouches.yml`. This means:

| You change... | Pouches that players already have... |
|---|---|
| `pricerange`, `economytype`, `permission-required` | **use the new values** right after `/mpa reload`. |
| `name`, `lore`, `item`, texture, enchantments | **keep their old look**. Only newly given pouches look different. They still open normally. |
| the **id** (rename the block) | **stop working**: "This pouch no longer exists!". Keep the old id, or add a pouch with the old id back. |
| delete the pouch | **stop working**: they show the `invalid-pouch` message and can't be opened or placed. |
| the pouch's economy disappears (file deleted, plugin removed) | the pouch is skipped at load, so its items behave as if it was deleted, until the economy is back. |

## Good to know

- Only the **main hand** works. Right-clicking with a pouch in the off-hand does nothing.
- A pouch can never be **placed** (even a chest or a head), right-clicking always tries to open it.
- A player can only open **one pouch at a time**. Right-clicking another during the animation shows `already-opening` and doesn't use it.
- If the player **logs out** during the animation, the prize is still paid at that moment (except XP, which can only be given to an online player: that failure is logged in the console).
- If the inventory is **full** when a pouch is given, it's **dropped at the player's feet** (never lost) and they get a message.
- A stack of pouches opens **one per click**.

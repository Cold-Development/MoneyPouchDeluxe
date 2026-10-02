# Pouch Configuration (`pouches.yml`)

This page lists every option a pouch can have. For a guided walkthrough, start with [Creating a Pouch](Creating-a-Pouch).

Default file: [pouches.yml](https://github.com/Cold-Development/MoneyPouchDeluxe/blob/master/src/main/resources/pouches.yml)

#### Jump to
- [Layout](#layout)
- [All options](#all-options)
- [name](#name) · [item](#item) · [Custom heads](#custom-heads-player_head--texture-url) · [pricerange](#pricerange) · [options.economytype](#optionseconomytype) · [options.permission-required](#optionspermission-required) · [lore](#lore)
- [Extra item options](#extra-item-options): enchantments, itemflags, unbreakable, custommodeldata, attributemodifiers
- [Full examples](#full-examples)

---

## Layout

Every top-level key is **one pouch**. The key is the **pouch id**, used in `/mp <id>`.

```yaml
<pouch id>:
  name: "..."
  item: "..."
  texture-url: "..."        # only for PLAYER_HEAD
  pricerange:
    from: <number>
    to: <number>
  options:
    economytype: "..."
    permission-required: ... # optional
  lore:
    - "..."
  # optional extras: enchantments, itemflags, unbreakable, custommodeldata, attributemodifiers
```

Rules for the id:
- Letters, numbers, `-` and `_`. **No spaces, no dots** (a dot creates a sub-section in YAML).
- Must be unique.
- Pouch items remember their id. **Renaming it breaks the pouches players already have.**

Rules for YAML:
- Indent with **spaces**, never tabs. 2 spaces per level.
- Put text in `"double quotes"`, especially if it contains `&`, `#`, `:` or starts with `%`, `<`, `*`.
- Run `/mpa reload` after every change, then `/mpa list` to check.

## All options

| Option | Required | Default | Description |
|---|---|---|---|
| [`name`](#name) | ✅ | `Unnamed Pouch` | Item name. Colors and glyphs allowed. |
| [`item`](#item) | ✅ | `CHEST` | Material, `PLAYER_HEAD` or `nexo:<id>`. |
| [`texture-url`](#custom-heads-player_head--texture-url) | ❌ | — | Head texture, only for `PLAYER_HEAD`. |
| [`pricerange.from`](#pricerange) | ✅ | `0` | Smallest prize (included). |
| [`pricerange.to`](#pricerange) | ✅ | `0` | Largest prize (included). |
| [`options.economytype`](#optionseconomytype) | ✅ | `VAULT` | Currency id: `XP` or a file in `customeconomytype/`. |
| [`options.permission-required`](#optionspermission-required) | ❌ | none (public) | Permission needed to open it. |
| [`lore`](#lore) | ❌ | none | Description lines. |
| [`enchantments`](#enchantments) | ❌ | none | `namespace:name:level` list. |
| [`itemflags`](#itemflags) | ❌ | none | Hide enchantments, attributes, ... |
| [`unbreakable`](#unbreakable) | ❌ | `false` | |
| [`custommodeldata`](#custommodeldata) | ❌ | none | For resource packs. |
| [`attributemodifiers`](#attributemodifiers) | ❌ | none | Attribute modifiers on the item. |

---

## `name`

The item name. Supports `&` colors, hex, gradients, rainbow ([Colors and Formatting](Colors-and-Formatting)) and Nexo glyphs. It's also what `%item%` shows in the `give-item` / `receive-item` messages.

```yaml
  name: "&6&lMoney Pouch &7(Right Click)"
```

## `item`

What the pouch looks like. Three possibilities:

### 1. A vanilla material

Any name from the [Material list](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html), **in capitals**, exactly as written there.

```yaml
  item: "CHEST"
  item: "ENDER_CHEST"
  item: "BUNDLE"
  item: "GOLD_NUGGET"
```

A wrong name (`chest`, `GOLDEN_BLOCK`, ...) gives a `Unrecognised material` warning in the console, and the pouch becomes **stone**.

> Any item works, even blocks: pouches can never be placed, right-clicking them always opens them.

### 2. A custom head

`item: "PLAYER_HEAD"` + `texture-url`. See [Custom heads](#custom-heads-player_head--texture-url) below.

### 3. A Nexo item

```yaml
  item: "nexo:ruby_pouch"
```

Requires Nexo. See [Nexo Integration](Nexo-Integration).

## Custom heads (`PLAYER_HEAD` + `texture-url`)

1. Find a head on [minecraft-heads.com](https://minecraft-heads.com/custom-heads).
2. On the head's page, scroll to **"For Developers"** and copy the **Value** (a long text starting with `eyJ0ZXh0dXJlcy...`).
3. Paste it in `texture-url`:

```yaml
pointspouch:
  name: "&6&lPoints Pouch"
  item: "PLAYER_HEAD"
  texture-url: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTVmZDY3ZDU2ZmZjNTNmYjM2MGExNzg3OWQ5YjUzMzhkNzMzMmQ4ZjEyOTQ5MWE1ZTE3ZThkNmU4YWVhNmMzYSJ9fX0="
  ...
```

`texture-url` accepts either:
- the **Base64 value** (recommended), or
- the direct texture link: `"http://textures.minecraft.net/texture/95fd67d56ffc53fb360a17879d9b5338d7332d8f129491a5e17e8d6e8aea6c3a"`.

Notes:
- `texture-url` is only used with `item: "PLAYER_HEAD"`. With any other item, leave it `""` or remove it.
- An invalid value logs `Invalid player-head texture` and gives a Steve/Alex head.
- A textured head keeps only its **name and lore**: `enchantments`, `itemflags`, `custommodeldata`, `unbreakable` and `attributemodifiers` are ignored on it.

<details>
<summary>Alternatives to <code>texture-url</code> (player skins)</summary>

If you remove the `texture-url` line entirely, a `PLAYER_HEAD` can use one of these instead:

```yaml
  item: "PLAYER_HEAD"
  owner-username: "Notch"         # the head of a player (must have joined the server, may contact Mojang)
  # owner-uuid: "069a79f4-44e9-4726-a5be-fca90e38aaf5"
  # owner-base64: "eyJ0ZXh0dXJlcyI6..."
```

`texture-url` is the recommended way: it doesn't depend on a player and never contacts Mojang.
</details>

## `pricerange`

```yaml
  pricerange:
    from: 5000
    to: 15000
```

- The prize is a random **whole number** between `from` and `to`, **both included**. Every value has the same chance.
- `from` equal to `to` = a **fixed amount** (e.g. a pouch that always gives exactly 1,000).
- If you swap them by mistake (`from` bigger than `to`), the plugin fixes it.
- Only whole numbers. No decimals.
- For `XP`, the maximum is 2,147,483,647, and the amount is **experience points, not levels**.

## `options.economytype`

The currency the prize is paid in.

```yaml
  options:
    economytype: "VAULT"
```

| Value | Pays out | Needs |
|---|---|---|
| `XP` | Experience points | Nothing, built in |
| `VAULT` | Money via `customeconomytype/vault.yml` (`eco give`) | An economy plugin with `/eco give` (EssentialsX, CMI, ...) |
| `PlayerPoints` | Points via `customeconomytype/playerpoints.yml` | PlayerPoints |
| `<file name>` | Whatever command that file runs | See [Economies](Custom-Economy-Types) |

- Not case-sensitive: `VAULT` = `vault` = `Vault`.
- The value is the name of a file in `customeconomytype/` **without `.yml`**.
- Run `/mpa economies` to see the valid values.
- If the economy doesn't exist, the **pouch is skipped** (missing from `/mpa list`) with a warning in the console.

## `options.permission-required`

The permission a player needs to **open** the pouch.

```yaml
  options:
    economytype: "VAULT"
    permission-required: moneypouch.pouches.vippouch
```

| Written as | Result |
|---|---|
| line missing | Anyone can open it |
| `false` or `""` | Anyone can open it |
| `some.permission.node` | Only players with `some.permission.node` (and OPs) |
| `true` | ❌ Don't. Looks for a permission literally called `true`. |

It must be **under `options:`**, at the same indentation as `economytype`. Full guide: [Pouch Permissions](Pouch-Permissions).

## `lore`

The lines under the name.

```yaml
  lore:
    - ""
    - "&7&oHow much money do you think is in here?"
    - ""
    - "&8» &f%pricerange_from%&a$ &8- &f%pricerange_to%&a$ &8«"
    - ""
```

| Placeholder | Replaced with |
|---|---|
| `%pricerange_from%` | `pricerange.from`, with thousands separators (e.g. `5,000`) |
| `%pricerange_to%` | `pricerange.to`, with thousands separators (e.g. `15,000`) |

- `""` is an empty line.
- Using the placeholders means you never have to update the lore by hand when you change the range.
- The separator in the lore follows the server's Java locale (usually `,`), not the `format.separator` of the title. If you need a specific format, type the numbers by hand.

---

## Extra item options

All optional. They work on any item except a textured `PLAYER_HEAD`.

### `enchantments`

Format: `namespace:enchantment:level`. Vanilla enchantments use the `minecraft` namespace and their [vanilla id](https://minecraft.wiki/w/Enchanting#Summary_of_enchantments).

```yaml
  enchantments:
    - "minecraft:unbreaking:1"
    - "minecraft:mending:1"
```

The most common use is the **enchantment glow**: add any enchantment and hide it with `HIDE_ENCHANTS`.

### `itemflags`

Hide parts of the tooltip. Full list: [ItemFlag](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemFlag.html).

```yaml
  itemflags:
    - "HIDE_ENCHANTS"
    - "HIDE_ATTRIBUTES"
```

### `unbreakable`

```yaml
  unbreakable: true
```

### `custommodeldata`

Gives the item a custom model from your resource pack.

```yaml
  item: "PAPER"
  custommodeldata: 1001
```

### `attributemodifiers`

Adds attribute modifiers. Attributes: [Attribute](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html), operations: [Operation](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/AttributeModifier.Operation.html). Always set a fixed `uuid`.

```yaml
  attributemodifiers:
    - attribute: GENERIC_MOVEMENT_SPEED
      modifier:
        uuid: "49dc07dc-bfdb-4dc7-85d3-66ef52b51858"
        name: "generic.movementSpeed"
        operation: ADD_NUMBER
        amount: 0.03
        equipmentslot: HAND
```

Rarely useful for a pouch (it's used up on the first click), mostly for the tooltip.

---

## Full examples

### Public money pouch

```yaml
moneypouch:
  name: "&8➥ &6&lMoney Pouch &6✦&7✦✦✦"
  item: "CHEST"
  texture-url: ""
  pricerange:
    from: 5000
    to: 15000
  options:
    economytype: "VAULT"
  lore:
    - ""
    - "&7&oHow much money do you think is in here?"
    - ""
    - "       &8» &f&l%pricerange_from%&a&l$ &8- &f&l%pricerange_to%&a&l$ &8«"
    - ""
```

### VIP-only pouch with a custom head

```yaml
vippouch:
  name: "<g:#00C6FF:#0072FF>&lVIP Pouch"
  item: "PLAYER_HEAD"
  texture-url: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTVmZDY3ZDU2ZmZjNTNmYjM2MGExNzg3OWQ5YjUzMzhkNzMzMmQ4ZjEyOTQ5MWE1ZTE3ZThkNmU4YWVhNmMzYSJ9fX0="
  pricerange:
    from: 25000
    to: 100000
  options:
    economytype: "VAULT"
    permission-required: moneypouch.pouches.vippouch
  lore:
    - ""
    - "&7Only &bVIPs &7can open this."
    - "&7Prize: &f%pricerange_from%$ &7- &f%pricerange_to%$"
    - ""
```

### Fixed-amount XP pouch with glow

```yaml
xp500:
  name: "&a&lXP Bottle &7(500 XP)"
  item: "EXPERIENCE_BOTTLE"
  pricerange:
    from: 500
    to: 500
  options:
    economytype: "XP"
  enchantments:
    - "minecraft:unbreaking:1"
  itemflags:
    - "HIDE_ENCHANTS"
  lore:
    - "&7Right-click for &a500 XP"
```

### Points pouch

```yaml
pointspouch:
  name: "&d&lPoints Pouch"
  item: "AMETHYST_SHARD"
  pricerange:
    from: 5
    to: 15
  options:
    economytype: "PlayerPoints"
    permission-required: false
  lore:
    - "&7Contains &d%pricerange_from% - %pricerange_to% &7points"
```

### Nexo pouch with a glyph

```yaml
rubypouch:
  name: "<glyph:icons_ruby> &c&lRuby Pouch"
  item: "nexo:ruby_pouch"
  pricerange:
    from: 100
    to: 1000
  options:
    economytype: "rubies"        # customeconomytype/rubies.yml
  lore:
    - "&7Contains &c%pricerange_from% - %pricerange_to% &7rubies"
```

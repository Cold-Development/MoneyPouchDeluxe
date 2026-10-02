# Nexo Integration

[Nexo](https://nexomc.com) is optional. When it's installed, MoneyPouchDeluxe can:

1. use a **Nexo custom item** as the pouch,
2. show **Nexo glyphs** (icons) with `<glyph:id>` in names, lore, messages and the title.

Nexo requires **Paper** (or a fork). No configuration is needed in MoneyPouchDeluxe to enable it: it's detected automatically.

---

## Nexo items as pouches

```yaml
rubypouch:
  name: "&c&lRuby Pouch"
  item: "nexo:ruby_pouch"        # nexo:<item id from your Nexo config>
  pricerange:
    from: 100
    to: 1000
  options:
    economytype: "VAULT"
  lore:
    - "&7A pouch full of rubies."
```

- The item id is the one from your Nexo item files (the same id as in `/nexo give <id>`).
- The pouch keeps the Nexo item's **model / texture**. The `name` and `lore` from `pouches.yml` replace Nexo's (leaving `lore` out gives a pouch with no lore).
- Nexo loads its items **after** all plugins start. MoneyPouchDeluxe reloads its pouches automatically when Nexo finishes, and after every `/nexo reload`.
- If the id is wrong, or Nexo isn't installed, the console shows `Unrecognised Nexo item: nexo:...` and the pouch becomes **stone**.

Tip: give the Nexo item a pouch-looking model in your resource pack (e.g. a bag), so pouches don't look like regular items.

## Glyphs

Write `<glyph:id>` anywhere text is shown:

```yaml
# pouches.yml
moneypouch:
  name: "<glyph:icons_money> &6&lMoney Pouch"
  lore:
    - "&7Prize: <glyph:icons_coin> &f%pricerange_from% - %pricerange_to%"

# config.yml
messages:
  prefix: "&8「<glyph:icons_beetroot>&8」&7» "
  prize-message: "&fYou won %prefix%%prize%%suffix% <glyph:icons_money>"
```

```yaml
# customeconomytype/rubies.yml
transaction-prize-command: "rubies give %player% %prize%"
prefix: "<glyph:icons_ruby> "
suffix: ""
```

- `id` is the glyph id from your Nexo glyph config.
- Works in: pouch `name` and `lore`, every message, the title `subtitle`, and economy `prefix` / `suffix` (so in the title too).
- Players need the Nexo **resource pack** to see the icons.
- An unknown glyph id is left as plain text, so you can spot the typo.

## Without Nexo

`<glyph:...>` tags are **not removed** when Nexo isn't installed: players will see the raw `<glyph:icons_beetroot>` text. The default `messages.prefix` contains one, so if you don't use Nexo, change it:

```yaml
messages:
  prefix: "&8「&6MoneyPouch&8」&7» "
```

Also check your pouch names, lore and economy prefixes for leftover `<glyph:...>` tags.

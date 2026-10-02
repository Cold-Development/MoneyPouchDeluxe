# Colors and Formatting

Colors work in pouch **names and lore**, **messages**, the **title** settings, and economy **prefixes / suffixes**.

## Legacy color codes

| Code | | Code | |
|---|---|---|---|
| `&0` | Black | `&8` | Dark gray |
| `&1` | Dark blue | `&9` | Blue |
| `&2` | Dark green | `&a` | Green |
| `&3` | Dark aqua | `&b` | Aqua |
| `&4` | Dark red | `&c` | Red |
| `&5` | Dark purple | `&d` | Light purple |
| `&6` | Gold | `&e` | Yellow |
| `&7` | Gray | `&f` | White |

| Code | Format |
|---|---|
| `&l` | **Bold** |
| `&o` | *Italic* |
| `&n` | Underline |
| `&m` | ~~Strikethrough~~ |
| `&k` | Obfuscated (scrambled) |
| `&r` | Reset |

Put format codes **after** the color: `&6&lGold bold`, not `&l&6`.

## Hex colors

Any of these work:

```yaml
name: "&#FFD700Gold Pouch"
name: "<#FFD700>Gold Pouch"
name: "{#FFD700}Gold Pouch"
```

## Gradients

```yaml
name: "<g:#FFD700:#FF4500>Gold Pouch"
name: "<gradient:#00C6FF:#0072FF:#7F00FF>VIP Pouch"     # 3 or more colors work too
name: "<g:#FFD700:#FF4500>&lGold Pouch"                  # bold gradient
```

The gradient spreads over the text that follows it, until the next color code.

## Rainbow

```yaml
name: "<rainbow>Rainbow Pouch"
name: "<r:0.6>Pastel Rainbow Pouch"      # saturation 0 – 1
name: "<r:0.8:0.7>Darker Rainbow"        # saturation, brightness
```

## Nexo glyphs

With Nexo installed, `<glyph:id>` shows a Nexo icon. See [Nexo Integration](Nexo-Integration).

## Tips

- Always put colored text in `"double quotes"` in YAML.
- If a name or lore line shows in italics, start it with a color code (e.g. `&f`), or use `&r` first.
- Test colors quickly: edit, `/mpa reload`, `/mp <pouch>`. Pouches given **before** the change keep their old look.
- [Birdflop's RGB tool](https://www.birdflop.com/resources/rgb/) is handy to design gradients; use the `&#rrggbb` output format.

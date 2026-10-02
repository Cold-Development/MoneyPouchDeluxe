# Main Configuration (`config.yml`)

`config.yml` controls how opening a pouch looks and sounds, what happens when a payout fails, and every message. The pouches themselves are in `pouches.yml` ([Pouch Configuration](Configuration-for-Pouches)).

Default file: [config.yml](https://github.com/Cold-Development/MoneyPouchDeluxe/blob/master/src/main/resources/config.yml)

Run `/mpa reload` after editing.

#### Jump to
- [Sounds](#sounds)
- [Title animation](#title-animation)
- [Error handling](#error-handling)
- [Economy (XP)](#economy)
- [Messages](#messages)
- [Hidden options](#hidden-options)

---

## Sounds

```yaml
pouches:
  sound:
    enabled: true
    opensound: "BLOCK_CHEST_OPEN"       # when the pouch is right-clicked
    revealsound: "BLOCK_ANVIL_LAND"     # at every revealed digit
    endsound: "ENTITY_GENERIC_EXPLODE"  # when the prize is paid
```

- Names come from the [Sound list](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html), in capitals.
- A wrong name is ignored silently (no sound, no error).
- **To turn a sound off, set it to `""`.** (In 1.5.1, `enabled: false` does not turn the sounds off, so empty them instead.)

## Title animation

When a pouch is opened, the prize is shown as a title with hidden (obfuscated) digits that are revealed one at a time.

```yaml
pouches:
  title:
    speed-in-tick: 10
    subtitle: "&7&oOpening..."
    obfuscate-colour: "&6"
    reveal-colour: "&f&l"
    prefix-colour: "&a&l"
    suffix-colour: "&a"
    obfuscate-digit-char: "#"
    obfuscate-format-char: "|"
    format:
      enabled: true
      separator: ","
      reveal-comma: true

reverse-pouch-reveal: true
```

| Option | Description |
|---|---|
| `speed-in-tick` | Time between two revealed digits, in ticks (20 ticks = 1 second). Lower = faster. A 5-digit prize with `10` takes about 3 seconds. |
| `subtitle` | Text under the number during the animation. `""` for none. |
| `obfuscate-colour` | Color of the hidden digits. |
| `reveal-colour` | Color of the revealed digits. |
| `prefix-colour` / `suffix-colour` | Color put before the economy's prefix / suffix (`$`, ` Points`, ...). The economy's own colors are applied after, so they win. |
| `obfuscate-digit-char` | Character drawn (scrambled) for a hidden digit. |
| `obfuscate-format-char` | Character drawn (scrambled) for a hidden separator, when `reveal-comma` is `false`. |
| `format.enabled` | `true`: the title groups digits (`1,924,281`). `false`: `1924281`. |
| `format.separator` | `","` → `1,924,281` · `"."` → `1.924.281` · `" "` → `1 924 281`. Also used for `%prize%` in messages. |
| `format.reveal-comma` | `true`: separators are visible from the start. `false`: they're hidden like digits. |
| `reverse-pouch-reveal` | `true`: digits are revealed **right to left** (units first, the suspense is on the big digits). `false`: left to right. |

The title is shown with no fade-in, 2.5 seconds on screen and 1 second fade-out.

> `%prize%` in `prize-message` / `reward-error` always uses `format.separator`, even when `format.enabled` is `false`. Set `separator: ""` for plain numbers everywhere.

## Error handling

What happens when a prize can't be paid.

```yaml
error-handling:
  log-failed-transactions: true
  refund-pouch: false
```

| Option | Description |
|---|---|
| `log-failed-transactions` | Logs the pouch, player and amount in the console when a payment fails, so you can pay the player by hand. **Keep it `true`.** |
| `refund-pouch` | Gives the player a new pouch when the payment fails. Off by default: the second opening rolls a different prize, and if the economy is broken it will fail again anyway. |

In every case, the player gets the `reward-error` message, asking them to contact an admin.

Failures from a [custom economy](Custom-Economy-Types#what-if-the-command-fails) command that doesn't exist are **always** logged (with the exact command to run), and don't refund the pouch.

## Economy

```yaml
economy:
  xp:
    name: "XP"       # %economy%
    prefix: ""
    suffix: " XP"
```

The name, prefix and suffix of the built-in **XP** economy. Every other currency sets them in its own file in `customeconomytype/`.

You can also add `economy.<id>` sections here for custom economies: they're used only when the economy's file has no `name` / `prefix` / `suffix`.

## Messages

```yaml
messages:
  prefix: "&8「&6MoneyPouch&8」&7» "
  full-inv: "&6%player%'s &finventory is &cfull&f. The pouch was dropped near the player."
  player-full-inv: "&fYour inventory is &cfull&f. A pouch was dropped near you. Make sure to pick it up."
  give-item: "&fYou have given &6%player%&f %item%&f."
  receive-item: "&fYou have received &6%item%&f."
  prize-message: "&fYou have received %prefix%%prize%%suffix%&f!"
  already-opening: "&fPlease wait until you open the first pouch!"
  invalid-pouch: "&fThis pouch no longer exists! &7(contact an administrator)"
  reward-error: "&fThe reward %prefix%%prize%%suffix% &fhas failed."
  no-permission: "&fYou do not have permission to open this pouch!"
  reloaded: "&fMoneyPouchDeluxe has been reloaded."
```

- `prefix` is put in front of **every** message. Set it to `""` for no prefix.
- **Set any message to `""` to disable it.**
- Colors: [Colors and Formatting](Colors-and-Formatting). Nexo glyphs: [Nexo Integration](Nexo-Integration).

> ⚠️ The default `prefix` contains `<glyph:icons_beetroot>`. **Without Nexo, this shows as plain text.** Replace it, e.g. `prefix: "&8「&6MoneyPouch&8」&7» "`.

### When each message is sent

| Message | Sent to | When | Placeholders |
|---|---|---|---|
| `give-item` | the giver | a pouch was given with `/mp` | `%player%`, `%item%` |
| `receive-item` | the receiver | they got a pouch from `/mp` | `%player%`, `%item%` |
| `full-inv` | the giver | the target's inventory was full, the pouch was dropped | `%player%` |
| `player-full-inv` | the receiver | their inventory was full, the pouch was dropped at their feet | — |
| `prize-message` | the opener | the prize was paid | `%prize%`, `%prefix%`, `%suffix%`, `%economy%` |
| `reward-error` | the opener | the payment failed | `%prize%`, `%prefix%`, `%suffix%`, `%economy%` |
| `already-opening` | the opener | they tried to open a pouch during another pouch's animation | — |
| `no-permission` | the opener | the pouch needs a permission they don't have | — |
| `invalid-pouch` | the opener | the pouch was deleted/renamed in `pouches.yml`, or its economy is missing | — |
| `reloaded` | the admin | after `/mpa reload` | — |

### Placeholders

| Placeholder | Value |
|---|---|
| `%prize%` | Amount won, with `format.separator` (e.g. `12,345`) |
| `%prefix%` / `%suffix%` | The economy's prefix / suffix |
| `%economy%` | The economy's `name` |
| `%item%` | The pouch's `name` |
| `%player%` | The player's name (`everyone` for `/mp <pouch> *`) |

Examples:

```yaml
  prize-message: "&fYou won &a%prefix%%prize%%suffix%&f!"             # You won $12,345!
  prize-message: "&fYou won &e%prize% &f%economy%!"                   # You won 12,345 money!
  receive-item: ""                                                    # no message when receiving a pouch
```

## Hidden options

Not in the default file, add them yourself if needed:

```yaml
options:
  show-receive-message: true   # false: never send 'receive-item' (same as receive-item: "")
```

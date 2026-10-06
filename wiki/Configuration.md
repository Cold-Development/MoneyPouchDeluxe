# Main Configuration (`config.yml`)

`config.yml` controls the language, how opening a pouch looks and sounds, what happens when a payout fails, the transaction log and where statistics are saved. The pouches themselves are in `pouches.yml` ([Pouch Configuration](Configuration-for-Pouches)), the messages in `locale/` ([Messages and Languages](Messages-and-Languages)).

Default file: [config.yml](https://github.com/Cold-Development/MoneyPouchDeluxe/blob/master/src/main/resources/config.yml)

Run `/mp reload` after editing. When the plugin updates, new settings are added to your file automatically, with their comments; your values and comments are kept.

#### Jump to
- [Language](#language)
- [Sounds](#sounds)
- [Title animation](#title-animation)
- [Opening a whole stack](#opening-a-whole-stack)
- [Error handling](#error-handling)
- [Transaction log](#transaction-log)
- [Economy (XP)](#economy)
- [Database](#database)

---

## Language

```yaml
locale: en_US
```

The name of a file in the `locale/` folder (without `.yml`). `en_US` and `ro_RO` are included. Every message, including the prefix, is in that file: see [Messages and Languages](Messages-and-Languages).

## Sounds

```yaml
pouches:
  sound:
    enabled: true
    opensound: "BLOCK_CHEST_OPEN"       # when the pouch is right-clicked
    revealsound: "BLOCK_ANVIL_LAND"     # at every revealed digit
    endsound: "ENTITY_GENERIC_EXPLODE"  # when the prize is paid
```

- Either the [Sound](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html) name in capitals (`BLOCK_CHEST_OPEN`), or the sound key (`block.chest.open`). Keys also work for sounds from a resource pack (`myserver:pouch_open`).
- A wrong name is ignored silently (no sound, no error).
- **To turn a sound off, set it to `""`.** (`enabled: false` doesn't turn them off, so empty them instead.)

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
| `format.separator` | `","` → `1,924,281` · `"."` → `1.924.281` · `" "` → `1 924 281`. Also used for `%prize%` in messages, `/mp list` and the `_formatted` placeholders. |
| `format.reveal-comma` | `true`: separators are visible from the start. `false`: they're hidden like digits. |
| `reverse-pouch-reveal` | `true`: digits are revealed **right to left** (units first, the suspense is on the big digits). `false`: left to right. |

The title is shown with no fade-in, 2.5 seconds on screen and 1 second fade-out.

> `%prize%` in the messages always uses `format.separator`, even when `format.enabled` is `false`. Set `separator: ""` for plain numbers everywhere.

## Opening a whole stack

```yaml
open-whole-stack-sneaking: true
```

With `true`, **sneak (shift) + right click** opens every pouch of the stack in the player's hand at once. Each pouch draws its own amount, the player gets the total in one payment, with one reveal and the `prize-message-stack` message. A normal right click still opens one pouch. `false` turns it off.

## Error handling

What happens when a prize can't be paid (the economy refused it, its plugin is missing, its command doesn't exist, ...).

```yaml
error-handling:
  log-failed-transactions: true
  refund-pouch: false
```

| Option | Description |
|---|---|
| `log-failed-transactions` | Logs the pouch, player, amount, economy and the reason in the console when a payment fails, so you can pay the player by hand. **Keep it `true`.** |
| `refund-pouch` | Gives the player their pouch(es) back when the payment fails (the whole stack, for a sneak-opened stack). Off by default: the second opening rolls a different prize, and if the economy is broken it will fail again anyway. |

In every case the player gets the `reward-error` message **instead of** the prize message, never both. Failures are also written to the [transaction log](#transaction-log).

What counts as a failure depends on the economy: see [Economies](Custom-Economy-Types#what-if-a-payment-fails).

## Transaction log

```yaml
transaction-log:
  enabled: true
  keep-days: 30
```

Every pouch opened and every pouch given is written to `plugins/MoneyPouchDeluxe/logs/`, one file per day (`2026-10-06.log`):

```
[13:36:27] OPEN Steve (069a79f4-...) moneypouch x1 -> 12,345 vault: OK
[13:37:05] OPEN Steve (069a79f4-...) moneypouch x6 -> 61,200 vault: OK
[13:38:10] OPEN Alex (853c80ef-...) moneypouch x1 -> 8,400 vault: FAILED (no economy plugin is registered with Vault)
[13:40:00] GIVE CONSOLE -> Steve (069a79f4-...) moneypouch x5
[13:41:00] GIVE Admin -> * (12 players) votepouch x1
```

| Option | Description |
|---|---|
| `enabled` | `false` stops writing new lines. |
| `keep-days` | Log files older than this many days are deleted automatically. `0` keeps them all. |

The amount is the one actually paid (with boosters from other plugins, if any). The file is written in the background, so it never slows the server down. To find what a player got: search the files for their name or UUID.

## Economy

```yaml
economy:
  xp:
    name: "XP"       # %economy%
    prefix: ""
    suffix: " XP"
```

The name, prefix and suffix of the built-in **XP** economy. Every other currency sets them in its own file in `customeconomytype/`.

You can also add `economy.<id>` sections here for other economies: they're used when the economy's file has no `name` / `prefix` / `suffix`, and by `VAULT` / `PlayerPoints` when their file doesn't exist.

## Database

```yaml
mysql-settings:
  enabled: false
  hostname: 127.0.0.1
  port: 3306
  database-name: ''
  user-name: ''
  user-password: ''
  use-ssl: false
  connection-pool-size: 3
```

Where the player statistics used by the [PlaceholderAPI placeholders](PlaceholderAPI) are saved. By default (`enabled: false`) they're in a SQLite file, `moneypouchdeluxe.db`, with nothing to set up. Set `enabled: true` and fill in the connection to use a MySQL database instead, e.g. to share statistics between servers. Restart the server after changing it.

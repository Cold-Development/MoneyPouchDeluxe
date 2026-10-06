# Messages and Languages

Every message of the plugin, including the prefix, is in a language file in `plugins/MoneyPouchDeluxe/locale/`:

```
locale/
├── en_US.yml     ← English (default)
└── ro_RO.yml     ← Romanian
```

The file in use is picked with `locale` in `config.yml`:

```yaml
locale: en_US
```

Run `/mp reload` after editing a message or changing the language.

Default files: [en_US.yml](https://github.com/Cold-Development/MoneyPouchDeluxe/blob/master/src/main/resources/locale/en_US.yml) · [ro_RO.yml](https://github.com/Cold-Development/MoneyPouchDeluxe/blob/master/src/main/resources/locale/ro_RO.yml)

---

## Editing messages

```yaml
prefix: '&8「&6MoneyPouch&8」&7» '
prize-message: '&fYou have received %prefix%%prize%%suffix%&f!'
already-opening: ''
```

- `prefix` is put in front of most messages (not the lines of `/mp list`, `/mp economies` and the help).
- **Set a message to `''` to disable it.**
- Colors, hex and gradients: [Colors and Formatting](Colors-and-Formatting). Nexo glyphs: [Nexo Integration](Nexo-Integration).
- [PlaceholderAPI](PlaceholderAPI) placeholders work in every message too (`%player_name%`, `%vault_eco_balance%`, ...).
- When the plugin updates, new messages are added to your file automatically. The ones you changed are never touched.

## Adding a language

1. Copy `locale/en_US.yml` to e.g. `locale/de_DE.yml`.
2. Translate the messages.
3. Set `locale: de_DE` in `config.yml` and run `/mp reload`.

A message missing from your file falls back to the English one.

## Every message

### Opening a pouch

| Message | Sent when | Placeholders |
|---|---|---|
| `prize-message` | the prize was paid | `%prize%`, `%prefix%`, `%suffix%`, `%economy%` |
| `prize-message-stack` | a stack was opened with sneak + right click and paid | the same, and `%amount%` (pouches opened) |
| `reward-error` | the payment failed (sent **instead of** the prize message) | `%prize%`, `%prefix%`, `%suffix%`, `%economy%`, `%amount%` |
| `already-opening` | the player tried to open a pouch during another pouch's animation | — |
| `invalid-pouch` | the pouch was deleted/renamed in `pouches.yml`, or its economy is missing | — |
| `pouch-no-permission` | the pouch needs a permission the player doesn't have | — |

### Receiving a pouch

| Message | Sent to | When | Placeholders |
|---|---|---|---|
| `receive-item` | the receiver | they got pouches from `/mp give` | `%player%`, `%item%`, `%amount%` |
| `player-full-inv` | the receiver | their inventory was full, the pouch was dropped at their feet | — |

### Commands

| Message | When | Placeholders |
|---|---|---|
| `command-give-success` | `/mp give` worked (to the giver) | `%player%`, `%item%`, `%amount%` |
| `command-give-all-success` | `/mp give <pouch> *` worked | `%item%`, `%amount%` |
| `command-give-full-inventory` | the target's inventory was full (to the giver) | `%player%` |
| `command-list-header` / `-entry` / `-empty` | `/mp list` | `%count%`; per pouch `%pouch%`, `%min%`, `%max%`, `%economy%`, `%permission%` |
| `command-economies-header` / `-entry` | `/mp economies` | `%count%`; per economy `%id%`, `%type%`, `%prefix%`, `%suffix%` |
| `command-reload-reloaded` | after `/mp reload` | — |
| `command-help-title`, `command-help-list-description(-no-args)` | `/mp help` | `%cmd%`, `%subcmd%`, `%args%`, `%desc%` |
| `command-<name>-description` | the description of each command in `/mp help` | — |

### Errors

| Message | When | Placeholders |
|---|---|---|
| `no-permission` | a command the sender isn't allowed to use | — |
| `only-player` | a player-only command run from the console | — |
| `invalid-subcommand` | `/mp <something>` that is neither a command nor a pouch | — |
| `pouch-not-found` | `/mp give <pouch>` with an unknown pouch | `%pouch%` |
| `player-not-found` | the player isn't online | `%player%` |
| `player-required` | `/mp give <pouch>` from the console without a player | — |
| `invalid-amount` | the amount isn't a whole number from 1 to 2304 | `%amount%`, `%max%` |
| `command-usage` | a command with missing arguments | `%cmd%`, `%args%` |
| `command-disabled` | a subcommand disabled in `commands/moneypouchdeluxe.yml` | `%command%` |
| `invalid-argument`, `argument-handler-*` | an argument of the wrong type | `%input%`, `%message%` |
| `unknown-command-error` | an unexpected error (details in the console) | — |

### Placeholders

| Placeholder | Value |
|---|---|
| `%prize%` | Amount won, with `format.separator` from `config.yml` (e.g. `12,345`) |
| `%prefix%` / `%suffix%` | The economy's prefix / suffix |
| `%economy%` | The economy's `name` |
| `%amount%` | Number of pouches |
| `%item%` | The pouch's `name` |
| `%player%` | The player's name |

Examples:

```yaml
prize-message: '&fYou won &a%prefix%%prize%%suffix%&f!'             # You won $12,345!
prize-message: '&fYou won &e%prize% &f%economy%!'                   # You won 12,345 money!
receive-item: ''                                                    # no message when receiving a pouch
```

## Updating from 1.x

The messages used to be in `config.yml` under `messages:`. On the first start of 2.0 they're moved to your locale file (customised messages and the prefix included), and the section is removed from `config.yml` (the old file is kept as `config.yml.before-locale`). A few messages were renamed:

| 1.x (`config.yml`) | 2.0 (`locale/`) |
|---|---|
| `no-permission` | `pouch-no-permission` |
| `no-permission-command` | `no-permission` |
| `full-inv` | `command-give-full-inventory` |
| `give-item` | `command-give-success` |
| `give-all` | `command-give-all-success` |
| `reloaded` | `command-reload-reloaded` |
| `list-*`, `economies-*` | `command-list-*`, `command-economies-*` |
| `help` (list) | replaced by `/mp help` |

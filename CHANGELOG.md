# Changelog

## 2.0.1

### Fixed
- When ColdDev adds a setting to `config.yml` (the language, the database settings) or a message to a `locale/` file, the comments at the end of lines and the formatting of your files are kept.
- Command messages in a custom language fall back to English instead of "Missing locale string".
- The comments in `commands/moneypouchdeluxe.yml` mentioned `/bits reload`.
- Gradients followed directly by a color are handled by ColdDev itself (built on ColdDev 1.5.6).

## 2.0.0

Everything from 1.x is migrated automatically on the first start: messages, pouches and settings keep your values and comments.

### New
- **Open a whole stack**: sneak + right click opens every pouch in your hand at once, with one reveal, the total amount and the new `prize-message-stack` message (`open-whole-stack-sneaking` in `config.yml`).
- **Vault and PlayerPoints hooks**: payments go through their API (`hook: vault` / `hook: playerpoints` in the economy file), so a refused payment is detected. The command is used when the plugin isn't installed, or with `hook: command`.
- **Transaction log**: every pouch opened (amount, economy, success or failure) and given is written to `logs/<date>.log`, one file per day, deleted after `transaction-log.keep-days`.
- **PlaceholderAPI**: `%moneypouch_opened%`, `%moneypouch_opened_<pouch>%`, `%moneypouch_won_<economy>%`, `%moneypouch_won_pouch_<pouch>%`, each with a `_formatted` variant. Statistics are saved in SQLite, or MySQL (`mysql-settings`). PlaceholderAPI placeholders also work in every message.
- **Developer API**: `PouchOpenEvent` (cancellable, the amount can be changed), `PouchRewardEvent`, and `MoneyPouchDeluxeAPI` (pouches, pouch items, giving pouches, registering economies).
- **Languages**: messages moved to `locale/en_US.yml` and `locale/ro_RO.yml`, picked with `locale` in `config.yml`.
- **Automatic updates of your files**: new settings and messages are added to `config.yml`, the locale files, `vault.yml` and `playerpoints.yml`, without changing anything you edited.
- **Commands**: `/mp give`, `/mp list`, `/mp economies`, `/mp reload` and `/mp help`, with names and aliases editable in `commands/moneypouchdeluxe.yml`. `/mp <pouch> [player] [amount]` and `/mpa` keep working.
- **Custom head textures**: `texture-url` accepts the Value, the texture URL or just the hash from minecraft-heads.com.
- Tab completion for pouches, online players, `*` and amounts, also from the console.

### Fixed
- A failed payment through a command sent both the prize message and the error message; now the player gets one or the other.
- `refund-pouch` and `log-failed-transactions` didn't work for custom economies.
- Negative, zero or huge amounts in `/mp`; amounts above 64 are now split into normal stacks (max 2304).
- `/mp <pouch> <name>` could give the pouch to another player whose name starts the same way; names must now match exactly.
- Giving a pouch to another player changed their inventory from the wrong thread on Folia.
- Custom heads lost their texture with `owner-base64` on 1.20.5+.
- Pouches given before a `/mp reload` didn't stack with the ones given after.
- Player head pouches lost their custom model data, enchantments and item flags.
- Sounds could break on 1.21.3+ (sound keys like `block.chest.open` work too now).
- Moving pouches out of `config.yml` (from 1.5.0 or older) removed the comments of the file.
- Messages that couldn't be configured (command errors, list output, a Romanian permission message).
- Hooking into Vault/PlayerPoints when updating from versions that had them built in, without a `vault.yml` / `playerpoints.yml`.

### Removed
- `attributemodifiers` on pouches (broke on 1.21.3+).
- `options.show-receive-message` (set `receive-item` to `''` to disable the message).

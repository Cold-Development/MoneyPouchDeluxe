# FAQ and Troubleshooting

**First steps for any problem:**

1. Run `/mp reload`.
2. Read the **console** right after: most problems are explained there.
3. Run `/mp list` (is the pouch loaded?) and `/mp economies` (is the currency loaded?).
4. Test with a **non-OP** account.

---

## Pouches

### My pouch is not in `/mp list` / "The pouch X does not exist"

The pouch wasn't loaded. Check the console after `/mp reload`:

- `Skipping pouch 'X': economy type 'Y' is missing` → the `economytype` is wrong, or its file is missing in `customeconomytype/`. Compare with `/mp economies`. See [Economies](Custom-Economy-Types).
- A YAML error, or the pouch is simply missing → the file has a syntax error. Usually tabs instead of spaces, wrong indentation, or a missing `"`. Check it on [yamllint.com](https://www.yamllint.com/).

### My pouch is a stone block

- `Unrecognised material: X` → the `item` is not a valid [Material](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html), e.g. `"ENDER_CHEST"`.
- `Unrecognised Nexo item: nexo:X` → wrong Nexo id, or Nexo isn't installed/loaded.

### My custom head is a Steve / Alex head

The `texture-url` is invalid (console: `Invalid texture-url for pouch '<id>'`). From [minecraft-heads.com](https://minecraft-heads.com/custom-heads), copy the **Value** from the "For Developers" section (or the texture URL, or just its hash), not the "Give command". Also check that `item` is `"PLAYER_HEAD"`.

### My head pouch doesn't glow

The client draws heads as 3D models, without the enchantment glow. The enchantment is there (remove `HIDE_ENCHANTS` to see it in the lore). Use another item for a glowing pouch, or a Nexo / resource pack item.

### My changes don't show on pouches players already have

Normal: a given pouch keeps the **look** (name, lore, item) it had when it was given. The **prize range, economy and permission** do use the new values. Give a new pouch to see the new look. See [Editing a pouch later](Creating-a-Pouch#editing-a-pouch-later).

### "This pouch no longer exists!"

The pouch's id isn't loaded anymore: it was deleted or renamed in `pouches.yml`, or its economy is missing (so it was skipped at load). Put back a pouch with the **same id** and run `/mp reload`: the old items work again.

### Right-clicking the pouch does nothing

- Hold it in your **main hand**, not the off-hand.
- Another plugin (protection, region, anti-cheat) may cancel the click before MoneyPouchDeluxe sees it. Try in an unprotected area.
- A plugin using the [API](Developer-API) may block opening pouches in that place.
- It's not a pouch: items renamed in an anvil to look like a pouch don't work, only items given by `/mp give` (or copies of them).

### "Please wait until you open the first pouch!"

A player can only open one pouch at a time. Wait until the title animation ends. To open many pouches at once, **sneak (shift) + right click** on the stack.

### Sneak + right click opens only one pouch

`open-whole-stack-sneaking` is `false` in `config.yml`. See [Opening a whole stack](Configuration#opening-a-whole-stack).

### Can players place a pouch (chest, head...)?

No. Right-clicking a pouch always opens it (or shows an error), it's never placed.

### A pouch was lost when the inventory was full

It wasn't: it was dropped at the player's feet, with a message. It can be picked up after 2 seconds.

---

## Permissions

### Everyone can open my VIP pouch

`permission-required` is missing, `false`, empty, or not indented under `options:`. See [Pouch Permissions](Pouch-Permissions).

### Nobody can open my pouch, even with the permission

- `permission-required: true` doesn't work: write the full node, e.g. `moneypouch.pouches.vippouch`.
- The node in `pouches.yml` and the one you gave must be **exactly** the same. Check with `/lp user <name> permission check <node>`.
- Run `/mp reload` after changing `pouches.yml`.

### It works for me but not for players

You're OP, and OPs have every permission. Test without OP.

### Players can give themselves pouches

They have `moneypouch.admin`. Remove it: only staff should have it. Players don't need any permission to open public pouches.

---

## Payouts

### The player didn't get the money / points

1. Check the console. A failed payment is logged as `Failed to process payment from pouch '...' for player '...' amount ... : <reason>`, and the player got `reward-error`. The [transaction log](Configuration#transaction-log) (`logs/<date>.log`) has a `FAILED (...)` line too. Give the prize by hand, then fix the cause:
   - `no economy plugin is registered with Vault` → install an economy plugin (EssentialsX, CMI, ...).
   - `unknown command: /...` → the command in the economy file doesn't exist: its plugin isn't installed, or the command name is wrong.
2. No error and the player got the prize message? With `hook: command`, run the command from the economy file yourself in the console, with a real player name and amount (e.g. `eco give Steve 100`). If it gives nothing, the command is wrong for your plugin. Better: use `hook: vault` / `hook: playerpoints`, which detect a refused payment.
3. Is the right economy used? `/mp list` shows each pouch's economy, `/mp economies` how each one pays.

### Which economy should my money pouches use?

`VAULT` with `hook: vault` (the default `vault.yml`): it pays through Vault, so it works with any economy plugin behind it, and a refused payment is detected.

### `/eco give` doesn't exist

Only relevant with `hook: command`, or when Vault isn't installed. `eco give` comes from EssentialsX (and some other economy plugins). Use your economy plugin's give command instead, e.g. `cmi money give %player% %prize%` for CMI, or install Vault and use `hook: vault`.

### `vault.yml` / `playerpoints.yml` don't exist

They're only generated when the `customeconomytype/` folder doesn't exist. `VAULT` and `PlayerPoints` pouches work without them anyway (while Vault / PlayerPoints is installed); create them only to change the name, prefix or suffix: [Ready-to-use files](Custom-Economy-Types#ready-to-use-files).

### My custom economy file isn't loaded

- The file name must contain **only letters and numbers**: `mob_coins.yml` → `mobcoins.yml`.
- It must end in `.yml` and have a working `hook` or a `transaction-prize-command`.
- The console says which of these is the problem.

### XP pouch: the player got fewer levels than expected

XP pouches give **experience points**, not levels. 150 XP points is a few levels at low level and much less than one level at high level.

### Where can I see what a player got?

In the transaction log, `plugins/MoneyPouchDeluxe/logs/<date>.log`: every pouch opened (amount, economy, OK or FAILED) and given. Search for the player's name or UUID. See [Transaction log](Configuration#transaction-log).

---

## Messages and display

### Where are the messages?

In `plugins/MoneyPouchDeluxe/locale/en_US.yml` (or the language set by `locale` in `config.yml`). They moved out of `config.yml` in 2.0. See [Messages and Languages](Messages-and-Languages).

### `<glyph:icons_beetroot>` shows in chat

That's a Nexo glyph tag, in your prefix (servers updated from 1.x keep their old prefix). Without Nexo, replace it in `locale/en_US.yml`: `prefix: '&8「&6MoneyPouch&8」&7» '`. See [Nexo Integration](Nexo-Integration#without-nexo).

### `&a` or a glyph tag shows in a message with a gradient

Fixed in 2.0: update the plugin. See [Gradients](Colors-and-Formatting#gradients) for how gradients end.

### How do I disable a message?

Set it to `''` in your locale file, e.g. `receive-item: ''`.

### Numbers show as `1.000` instead of `1,000` (or the reverse)

- Title, `%prize%`, `/mp list` and the `_formatted` placeholders: change `pouches.title.format.separator` in `config.yml`.
- `%pricerange_from%` / `%pricerange_to%` in the lore follow the server's Java locale. To control it exactly, write the numbers in the lore by hand.

### The title animation is too slow / too fast

`pouches.title.speed-in-tick` in `config.yml`: ticks between digits (20 = 1 second). Lower is faster.

### How do I turn the sounds off?

Set `opensound`, `revealsound` and `endsound` to `""`.

### A placeholder shows `0`

Statistics only count successful payments since 2.0 was installed. For a player whose statistics aren't loaded yet (an offline player), the first request shows `0` and the real value right after. See [PlaceholderAPI](PlaceholderAPI).

---

## Other

### Does it work on Folia?

Yes.

### Does it need Vault?

No. MoneyPouchDeluxe has no hard dependencies. Vault and PlayerPoints are used when installed; any other currency is paid through its console command.

### My settings are gone after an update / I'm missing new settings

Updates never reset your files: new settings and messages are **added** to them, with their comments, and the console lists what was added. If something looks wrong, the backups from the 1.x → 2.0 migration are next to `config.yml` (`config.yml.before-locale`, `config.yml.before-pouches-yml`).

### Can I rename the commands?

Yes, in `commands/moneypouchdeluxe.yml`. See [Renaming commands](Commands-and-Permissions#renaming-commands).

### Is there still a pouch shop?

No, it was removed in 1.5.1. Sell pouches with a shop or menu plugin: [Using Pouches with Other Plugins](Using-Pouches-with-Other-Plugins#shops--menus-deluxemenus-shopgui-).

### Still stuck?

Ask on [our Discord](https://discord.colddev.dev) with:
- your server version and MoneyPouchDeluxe version,
- the pouch from `pouches.yml` and the economy file,
- the console output after `/mp reload`.

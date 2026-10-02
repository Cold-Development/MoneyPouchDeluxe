# FAQ and Troubleshooting

**First steps for any problem:**

1. Run `/mpa reload`.
2. Read the **console** right after: most problems are explained there.
3. Run `/mpa list` (is the pouch loaded?) and `/mpa economies` (is the currency loaded?).
4. Test with a **non-OP** account.

---

## Pouches

### My pouch is not in `/mpa list` / "The pouch X could not be found"

The pouch wasn't loaded. Check the console after `/mpa reload`:

- `Skipping pouch 'X': economy type 'Y' is missing` → the `economytype` is wrong, or its file is missing in `customeconomytype/`. Compare with `/mpa economies`. See [Economies](Custom-Economy-Types).
- A YAML error, or the pouch is simply missing → the file has a syntax error. Usually tabs instead of spaces, wrong indentation, or a missing `"`. Check it on [yamllint.com](https://www.yamllint.com/).

### My pouch is a stone block

- `Unrecognised material: X` → the `item` is not a valid [Material](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html). Use capitals: `"ENDER_CHEST"`, not `"ender_chest"` or `"Ender Chest"`.
- `Unrecognised Nexo item: nexo:X` → wrong Nexo id, or Nexo isn't installed/loaded.

### My custom head is a Steve / Alex head

The `texture-url` is invalid (console: `Invalid player-head texture`). Copy the **Value** from the "For Developers" section of [minecraft-heads.com](https://minecraft-heads.com/custom-heads), not the "Give command". Also check that `item` is `"PLAYER_HEAD"`.

### My changes don't show on pouches players already have

Normal: a given pouch keeps the **look** (name, lore, item) it had when it was given. The **prize range, economy and permission** do use the new values. Give a new pouch to see the new look. See [Editing a pouch later](Creating-a-Pouch#editing-a-pouch-later).

### "This pouch no longer exists!"

The pouch's id isn't loaded anymore: it was deleted or renamed in `pouches.yml`, or its economy is missing (so it was skipped at load). Put back a pouch with the **same id** and run `/mpa reload`: the old items work again.

### Right-clicking the pouch does nothing

- Hold it in your **main hand**, not the off-hand.
- Another plugin (protection, region, anti-cheat) may cancel the click before MoneyPouchDeluxe sees it. Try in an unprotected area.
- It's not a pouch: items renamed in an anvil to look like a pouch don't work, only items given by `/mp` (or copies of them).

### "Please wait until you open the first pouch!"

A player can only open one pouch at a time. Wait until the title animation ends.

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
- Run `/mpa reload` after changing `pouches.yml`.

### It works for me but not for players

You're OP, and OPs have every permission. Test without OP.

### Players can give themselves pouches

They have `moneypouch.admin`. Remove it: only staff should have it. Players don't need any permission to open public pouches.

---

## Payouts

### The player didn't get the money / points

1. Check the console. If you see `Custom economy command failed, Steve did not receive 12345. Command: /eco give Steve 12345`, the command doesn't exist: the currency plugin isn't installed, or the command name is wrong. Fix `customeconomytype/<file>.yml`, and give the prize by hand with the logged command.
2. No error in the console? Run the command from the economy file yourself in the console, with a real player name and amount (e.g. `eco give Steve 100`). If it fails or gives nothing, the command is wrong for your plugin (wrong arguments, wrong currency name).
3. Is the right economy used? `/mpa list` shows each pouch's economy and command.

### `/eco give` doesn't exist

`vault.yml` uses `eco give`, which comes from EssentialsX (and some other economy plugins). Vault alone doesn't add any command. Use your economy plugin's give command instead, e.g. `cmi money give %player% %prize%` for CMI. See [Ready-to-use files](Custom-Economy-Types#ready-to-use-files).

### `vault.yml` / `playerpoints.yml` don't exist after updating

They're only generated when the `customeconomytype/` folder doesn't exist. Create them yourself: [Ready-to-use files](Custom-Economy-Types#ready-to-use-files).

### My custom economy file isn't loaded

- The file name must contain **only letters and numbers**: `mob_coins.yml` → `mobcoins.yml`.
- It must end in `.yml` and contain `transaction-prize-command`.
- The console says which of these is the problem.

### The player got an error message AND the prize message

When a custom economy's command doesn't exist, the command's failure is only detected once it runs, so the player can get both `prize-message` and `reward-error`. The `reward-error` is the one that counts: the prize was **not** given. Check the console log for the command to run by hand.

### XP pouch: the player got fewer levels than expected

XP pouches give **experience points**, not levels. 150 XP points is a few levels at low level and much less than one level at high level.

---

## Messages and display

### `<glyph:icons_beetroot>` shows in chat

That's a Nexo glyph tag in the default `messages.prefix`. Without Nexo, replace it: `prefix: "&8「&6MoneyPouch&8」&7» "`. See [Nexo Integration](Nexo-Integration#without-nexo).

### How do I disable a message?

Set it to `""` in `config.yml`, e.g. `receive-item: ""`.

### Numbers show as `1.000` instead of `1,000` (or the reverse)

- Title and `%prize%`: change `pouches.title.format.separator` in `config.yml`.
- `%pricerange_from%` / `%pricerange_to%` in the lore follow the server's Java locale. To control it exactly, write the numbers in the lore by hand.

### The title animation is too slow / too fast

`pouches.title.speed-in-tick` in `config.yml`: ticks between digits (20 = 1 second). Lower is faster.

### How do I turn the sounds off?

Set `opensound`, `revealsound` and `endsound` to `""`.

---

## Other

### Does it work on Folia?

Yes.

### Does it need Vault?

No. MoneyPouchDeluxe has no hard dependencies. It runs your economy plugin's console command, so you only need the plugin that owns the currency.

### Is there still a pouch shop?

No, it was removed in 1.5.1. Sell pouches with a shop or menu plugin: [Using Pouches with Other Plugins](Using-Pouches-with-Other-Plugins#shops--menus-deluxemenus-shopgui-).

### Still stuck?

Ask on [our Discord](https://discord.colddev.dev) with:
- your server version and MoneyPouchDeluxe version,
- the pouch from `pouches.yml` and the economy file,
- the console output after `/mpa reload`.

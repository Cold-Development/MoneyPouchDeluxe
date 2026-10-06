![MoneyPouchDeluxe](https://imgur.com/FRoQbVI.png)

![Version](https://img.shields.io/badge/Version-v2.0.1-blue?color=799aca)
![Minecraft](https://img.shields.io/badge/Minecraft-1.17%2B-green.svg)
![Folia](https://img.shields.io/badge/Folia-supported-green.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)

# MoneyPouchDeluxe

**MoneyPouchDeluxe** adds pouches: items that a player right-clicks to win a random amount of a currency (money, points, XP, tokens, anything). The amount is revealed digit by digit in an animated title.

- Works with **any currency**: Vault and PlayerPoints are hooked directly, anything else through a console command. No economy plugin is a hard dependency.
- **Failed payments are caught**: the player gets an error instead of a fake prize message, the failure is logged, and the pouch can be refunded.
- Pouches can be **free for everyone** or **locked behind a permission** (VIP pouches, rank pouches, ...).
- **Sneak + right click** opens a whole stack at once.
- **Transaction log**, **PlaceholderAPI** statistics and a **developer API**.
- Messages in **any language**, and your files are **updated automatically** without losing your edits.
- Optional **[Nexo](https://nexomc.com)** support for custom item models and `<glyph:id>` icons.
- Continuation of the original [MoneyPouch](https://github.com/LMBishop/MoneyPouch) by LMBishop.

![opening](https://github.com/user-attachments/assets/a7889779-bceb-42c8-b573-8d05e9d49070)

---

## Start here

New to the plugin? Read these in order:

1. **[Installation](Installation)**: requirements, first start, the files that are generated, updating.
2. **[Creating a Pouch](Creating-a-Pouch)**: a step-by-step guide, from an empty file to a pouch in your hand.
3. **[Pouch Permissions](Pouch-Permissions)**: pouches that everyone can open vs. pouches that need a permission.
4. **[Commands and Permissions](Commands-and-Permissions)**: every command and every permission node.

## Reference

| Page | What's in it |
|---|---|
| [Pouch Configuration](Configuration-for-Pouches) | Every option of `pouches.yml` (item, name, lore, range, heads, enchantments, ...) |
| [Economies](Custom-Economy-Types) | XP, Vault, PlayerPoints, and how to add any other currency |
| [Main Configuration](Configuration) | `config.yml`: language, sounds, the title animation, error handling, transaction log, database |
| [Messages and Languages](Messages-and-Languages) | The `locale/` files: every message, placeholders, adding a language |
| [Colors and Formatting](Colors-and-Formatting) | `&a`, hex colors, gradients, rainbow |
| [PlaceholderAPI](PlaceholderAPI) | Statistics placeholders (`%moneypouch_opened%`, ...) |
| [Nexo Integration](Nexo-Integration) | Nexo items as pouches, `<glyph:id>` icons |
| [Using Pouches with Other Plugins](Using-Pouches-with-Other-Plugins) | Crates, votes, quests, shops: giving pouches from the console |
| [Developer API](Developer-API) | Events (boosters, cancelling), giving pouches, your own economies |
| [FAQ and Troubleshooting](FAQ-and-Troubleshooting) | "My pouch doesn't show up", "the player didn't get the money", ... |

## How it works, in 30 seconds

```
pouches.yml                      customeconomytype/vault.yml
───────────                      ───────────────────────────
moneypouch:                      hook: vault        (or a command: eco give %player% %prize%)
  pricerange: 5000 → 15000            ▲
  economytype: "VAULT"  ──────────────┘
  permission-required: ...   (optional)
```

1. You define a pouch in `pouches.yml`: what it looks like, how much it can give and in which currency.
2. You (or a crate / vote plugin) give it to a player with `/mp give <pouch> <player>` (or the short `/mp <pouch> <player>`).
3. The player right-clicks it. If they are allowed to open it, a random amount between `from` and `to` is rolled, revealed in a title, and paid out through the currency's hook or command. If the payment fails, the player is told and the failure is logged.

## Support

Questions, bugs, suggestions: [join our Discord](https://discord.colddev.dev). Bugs can also be reported on [GitHub Issues](https://github.com/Cold-Development/MoneyPouchDeluxe/issues).

Download: [SpigotMC](https://www.spigotmc.org/resources/moneypouchdeluxe.118795/)

![MoneyPouchDeluxe](https://imgur.com/FRoQbVI.png)

![Version](https://img.shields.io/badge/Version-v1.5.1-blue?color=799aca)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20%2B-green.svg)
![Folia](https://img.shields.io/badge/Folia-supported-green.svg)
![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)

# MoneyPouchDeluxe

**MoneyPouchDeluxe** adds pouches: items that a player right-clicks to win a random amount of a currency (money, points, XP, tokens, anything). The amount is revealed digit by digit in an animated title.

- Works with **any currency** that has a "give" console command. No economy plugin is a hard dependency.
- Pouches can be **free for everyone** or **locked behind a permission** (VIP pouches, rank pouches, ...).
- Optional **[Nexo](https://nexomc.com)** support for custom item models and `<glyph:id>` icons.
- Continuation of the original [MoneyPouch](https://github.com/LMBishop/MoneyPouch) by LMBishop.

![opening](https://github.com/user-attachments/assets/a7889779-bceb-42c8-b573-8d05e9d49070)

---

## Start here

New to the plugin? Read these in order:

1. **[Installation](Installation)**: requirements, first start, the files that are generated.
2. **[Creating a Pouch](Creating-a-Pouch)**: a step-by-step guide, from an empty file to a pouch in your hand.
3. **[Pouch Permissions](Pouch-Permissions)**: pouches that everyone can open vs. pouches that need a permission.
4. **[Commands and Permissions](Commands-and-Permissions)**: every command and every permission node.

## Reference

| Page | What's in it |
|---|---|
| [Pouch Configuration](Configuration-for-Pouches) | Every option of `pouches.yml` (item, name, lore, range, heads, enchantments, ...) |
| [Economies](Custom-Economy-Types) | XP, money (EssentialsX/CMI/...), PlayerPoints, and how to add any other currency |
| [Main Configuration](Configuration) | `config.yml`: sounds, the title animation, error handling, messages |
| [Colors and Formatting](Colors-and-Formatting) | `&a`, hex colors, gradients, rainbow |
| [Nexo Integration](Nexo-Integration) | Nexo items as pouches, `<glyph:id>` icons |
| [Using Pouches with Other Plugins](Using-Pouches-with-Other-Plugins) | Crates, votes, quests, shops: giving pouches from the console |
| [FAQ and Troubleshooting](FAQ-and-Troubleshooting) | "My pouch doesn't show up", "the player didn't get the money", ... |

## How it works, in 30 seconds

```
pouches.yml                      customeconomytype/vault.yml
───────────                      ───────────────────────────
moneypouch:                      transaction-prize-command: "eco give %player% %prize%"
  pricerange: 5000 → 15000            ▲
  economytype: "VAULT"  ──────────────┘
  permission-required: ...   (optional)
```

1. You define a pouch in `pouches.yml`: what it looks like, how much it can give and in which currency.
2. You (or a crate / vote plugin) give it to a player with `/mp <pouch> <player>`.
3. The player right-clicks it. If they are allowed to open it, a random amount between `from` and `to` is rolled, revealed in a title, and paid out by running the currency's command from the console.

## Support

Questions, bugs, suggestions: [join our Discord](https://discord.colddev.dev). Bugs can also be reported on [GitHub Issues](https://github.com/Cold-Development/MoneyPouchDeluxe/issues).

Download: [SpigotMC](https://www.spigotmc.org/resources/moneypouchdeluxe.118795/)

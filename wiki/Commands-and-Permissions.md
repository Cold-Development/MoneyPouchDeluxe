# Commands and Permissions

## Commands

`<>` = required, `[]` = optional.

Main command: `/mp`. Aliases: `/moneypouchdeluxe`, `/moneypouch`, `/cp`, `/mpa`, `/cpa` (they all do the same, so `/mpa reload` and `/mp reload` are the same command).

| Command | What it does | Permission |
|---|---|---|
| `/mp` or `/mp help` | Lists the commands you can use | — |
| `/mp give <pouch>` | Gives 1 pouch to yourself (players only) | `moneypouch.admin` |
| `/mp give <pouch> <player> [amount]` | Gives pouches to an online player | `moneypouch.admin` |
| `/mp give <pouch> * [amount]` | Gives to **every online player** | `moneypouch.admin` **and** `moneypouch.admin.giveall` |
| `/mp <pouch> [player] [amount]` | Short form of `/mp give` (the form older versions used) | same as `/mp give` |
| `/mp list` | Lists every **loaded** pouch: id, range, economy, permission | `moneypouch.admin` |
| `/mp economies` | Lists every loaded economy (currency), its id and how it pays | `moneypouch.admin` |
| `/mp reload` | Reloads `config.yml`, the messages, `pouches.yml` and `customeconomytype/` | `moneypouch.admin` |

### Giving pouches

- `<pouch>` is the pouch id from `pouches.yml` (not case-sensitive). Press Tab to see them.
- The player must be **online**, and their name written **in full** (Tab completes it).
- The amount goes from 1 to 2304 (a full inventory). Large amounts are split into normal stacks.
- If the inventory is full, whatever doesn't fit is **dropped at the player's feet**. Both you and the player are told.
- Works from the **console** too, which is how crates, vote plugins and shops give pouches: `mp give moneypouch %player%` (or `mp moneypouch %player%`). See [Using Pouches with Other Plugins](Using-Pouches-with-Other-Plugins).
- Every pouch given is written to the [transaction log](Configuration#transaction-log).

> 💡 `/mp list` is your best debugging tool: a pouch missing from it wasn't loaded, and the console says why.

### Renaming commands

`plugins/MoneyPouchDeluxe/commands/moneypouchdeluxe.yml` is generated on the first start. In it you can change the main command's name and aliases, and the name, aliases and `enabled` state of each subcommand (`give`, `list`, `economies`, `help`; `reload` can't be disabled). Run `/mp reload` after editing.

## Permissions

### Plugin permissions

| Permission | Gives access to | Default |
|---|---|---|
| `moneypouch.admin` | `/mp give`, `/mp list`, `/mp economies`, `/mp reload` | OP |
| `moneypouch.admin.giveall` | `/mp give <pouch> *` (give to everyone online). Includes `moneypouch.admin`. | OP |
| `moneypouchdeluxe.updates` | A message on join when a new version is released on GitHub (checked when the server starts). `colddev.updates` does the same for every Cold Development plugin. | OP |

**Regular players don't need any permission** to receive pouches or open public ones. Don't give them `moneypouch.admin`: it lets them give themselves unlimited pouches.

### Pouch permissions

Each pouch can require its own permission to be **opened**, set with `options.permission-required` in `pouches.yml`. There is no fixed list: the node is whatever you write there. The convention is `moneypouch.pouches.<pouch id>`.

| Permission | Gives access to | Default |
|---|---|---|
| *(your node, e.g.)* `moneypouch.pouches.vippouch` | Opening that pouch | OP |
| `moneypouch.pouches.*` | Opening every pouch that follows the convention (LuckPerms wildcard) | — |

Pouches without `permission-required` can be opened by anyone, no permission needed.

Full guide: **[Pouch Permissions](Pouch-Permissions)**.

### Example LuckPerms setup

```
# Staff that hand out pouches
/lp group admin permission set moneypouch.admin true
/lp group admin permission set moneypouch.admin.giveall true

# VIPs can open VIP pouches
/lp group vip permission set moneypouch.pouches.vippouch true

# Default players: nothing to do, public pouches work for everyone
```

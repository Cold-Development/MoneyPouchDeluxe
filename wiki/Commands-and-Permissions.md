# Commands and Permissions

## Commands

`<>` = required, `[]` = optional.

### `/mp` — give pouches

Aliases: `/moneypouch`, `/cp`

| Command | What it does | Permission |
|---|---|---|
| `/mp` | Shows the help | `moneypouch.admin` |
| `/mp <pouch>` | Gives 1 pouch to yourself (players only) | `moneypouch.admin` |
| `/mp <pouch> <player>` | Gives 1 pouch to an online player | `moneypouch.admin` |
| `/mp <pouch> <player> <amount>` | Gives several | `moneypouch.admin` |
| `/mp <pouch> * [amount]` | Gives to **every online player** | `moneypouch.admin` **and** `moneypouch.admin.giveall` |

- `<pouch>` is the pouch id from `pouches.yml` (not case-sensitive). Press Tab to see them.
- The player must be **online**.
- Amounts above 64 work, but show a warning: they are split into several stacks.
- If the inventory is full, whatever doesn't fit is **dropped at the player's feet**. Both you and the player are told.
- Works from the **console** too, which is how crates, vote plugins and shops give pouches: `mp moneypouch %player%`. See [Using Pouches with Other Plugins](Using-Pouches-with-Other-Plugins).

### `/mpa` — admin

Aliases: `/moneypouchadmin`, `/cpa`

| Command | What it does | Permission |
|---|---|---|
| `/mpa` | Shows the help | `moneypouch.admin` |
| `/mpa list` | Lists every **loaded** pouch: id, range, economy, prefix/suffix | `moneypouch.admin` |
| `/mpa economies` | Lists every loaded economy (currency) and its id | `moneypouch.admin` |
| `/mpa reload` | Reloads `config.yml`, `pouches.yml` and `customeconomytype/` | `moneypouch.admin` |

> 💡 `/mpa list` is your best debugging tool: a pouch missing from it wasn't loaded, and the console says why.

## Permissions

### Plugin permissions

| Permission | Gives access to | Default |
|---|---|---|
| `moneypouch.admin` | `/mp` and `/mpa` (giving pouches, list, economies, reload) | OP |
| `moneypouch.admin.giveall` | `/mp <pouch> *` (give to everyone online). Needs `moneypouch.admin` too. | OP |

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

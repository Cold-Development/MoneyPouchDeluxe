# Pouch Permissions

Every pouch is either:

- **Public**: anyone who has it can open it. This is the default.
- **Restricted**: only players with a specific permission can open it. Everyone else gets the `no-permission` message and **keeps the pouch** (it isn't used up).

> ⚠️ The permission only controls **opening** a pouch. Players can still hold, trade, drop or sell a restricted pouch. Giving pouches is controlled by `moneypouch.admin` (see [Commands and Permissions](Commands-and-Permissions)).

---

## Public pouch (no permission)

Don't write a `permission-required` line at all:

```yaml
moneypouch:
  name: "&6&lMoney Pouch"
  item: "CHEST"
  pricerange:
    from: 5000
    to: 15000
  options:
    economytype: "VAULT"
  lore:
    - "&7Anyone can open this!"
```

These are also public, if you prefer to write it explicitly:

```yaml
  options:
    economytype: "VAULT"
    permission-required: false
```

```yaml
  options:
    economytype: "VAULT"
    permission-required: ""
```

## Restricted pouch (permission required)

Write the **full permission node** you want after `permission-required`:

```yaml
vippouch:
  name: "&b&lVIP Pouch"
  item: "DIAMOND_BLOCK"
  pricerange:
    from: 20000
    to: 50000
  options:
    economytype: "VAULT"
    permission-required: moneypouch.pouches.vippouch
  lore:
    - "&7Only &bVIPs &7can open this!"
```

Then run `/mpa reload` and give the permission (see [below](#giving-the-permission)).

### Which node should I use?

**Any node you want.** The plugin checks exactly the text you write. The recommended convention is:

```
moneypouch.pouches.<pouch id>
```

but you can also reuse a permission that your ranks already have, so you don't need to add anything to your permission plugin:

```yaml
    permission-required: group.vip          # LuckPerms: everyone in the "vip" group has this automatically
    permission-required: essentials.fly     # anyone who can /fly
```

Several pouches can share the same node, e.g. all VIP pouches use `moneypouch.vip`.

### A permission for each pouch, step by step

The plugin **doesn't create the node for you**: for every pouch you want to restrict, you write it yourself. Using the pouch id keeps it easy to remember.

**1. Find the pouch id.** It's the name of the block in `pouches.yml` (the line with no indentation), and it's what `/mpa list` shows at the start of each line:

```yaml
pointspouch:        # ← the pouch id is "pointspouch"
  name: "&6&lPoints Pouch"
  ...
```

**2. Build the node:** `moneypouch.pouches.` + the pouch id, **written exactly the same**.

| Pouch id in `pouches.yml` | Node to write in `permission-required` |
|---|---|
| `moneypouch` | `moneypouch.pouches.moneypouch` |
| `pointspouch` | `moneypouch.pouches.pointspouch` |
| `xppouch` | `moneypouch.pouches.xppouch` |
| `vip-pouch` | `moneypouch.pouches.vip-pouch` |

**3. Write it under that pouch's `options:`**, replacing any existing `permission-required` line (there must be only one):

```yaml
moneypouch:
  ...
  options:
    economytype: "VAULT"
    permission-required: moneypouch.pouches.moneypouch

pointspouch:
  ...
  options:
    economytype: "PlayerPoints"
    permission-required: moneypouch.pouches.pointspouch   # was "false" in the default file

xppouch:
  ...
  options:
    economytype: "XP"
    permission-required: moneypouch.pouches.xppouch
```

**4. `/mpa reload`.**

**5. Give each node to whoever should open that pouch** (the same node as in step 3):

```
/lp group default permission set moneypouch.pouches.moneypouch true
/lp group vip     permission set moneypouch.pouches.pointspouch true
/lp group mvp     permission set moneypouch.pouches.xppouch true
```

**6. Test with a non-OP account**: a player without the node gets `no-permission` and keeps the pouch.

> 💡 The node in `pouches.yml` and the one in your permission plugin must match **character for character**. A typo (`moneypouch.pouch.xppouch`, `moneypouches.pouches.xppouch`) means nobody (except OPs) can open the pouch.

> 💡 If you rename a pouch id, the node doesn't change by itself. Update `permission-required` and the permission you gave, or keep the old node: any node works, it just has to match on both sides.

### ❌ Don't write `true`

```yaml
    permission-required: true     # WRONG
```

`true` is **not** "generate a permission for me". The plugin will look for a permission literally called `true`, which nobody has, so only OPs will be able to open the pouch. Always write the full node.

## Giving the permission

### LuckPerms

```
# To a group (recommended)
/lp group vip permission set moneypouch.pouches.vippouch true

# To one player
/lp user Steve permission set moneypouch.pouches.vippouch true

# Temporarily (e.g. 7 days)
/lp user Steve permission settemp moneypouch.pouches.vippouch true 7d

# Every pouch that follows the moneypouch.pouches.<id> convention
/lp group admin permission set moneypouch.pouches.* true

# Take it away
/lp group vip permission unset moneypouch.pouches.vippouch
```

Or use the web editor: `/lp editor`.

> The `moneypouch.pouches.*` wildcard works with LuckPerms (and most modern permission plugins), because they resolve wildcards themselves.

### Other permission plugins

Use the "add permission" command of your plugin. For example, with GroupManager: `/mangaddp vip moneypouch.pouches.vippouch`.

### Denying a permission

If a group inherits a permission you don't want, set it to `false`:

```
/lp group default permission set moneypouch.pouches.vippouch false
```

## OPs have every permission

OPs can open **every** restricted pouch, even if you never gave them the permission. This is normal Bukkit behavior for permissions that aren't given to anyone explicitly.

**Always test restricted pouches with a non-OP account**, or `/deop` yourself while testing.

## Checking a permission

```
/lp user Steve permission check moneypouch.pouches.vippouch
```

and `/mpa list` to confirm the pouch is loaded.

## Common setups

### Rank pouches

One pouch per rank, each with its own permission:

```yaml
vip-pouch:
  ...
  options:
    economytype: "VAULT"
    permission-required: moneypouch.pouches.vip-pouch

mvp-pouch:
  ...
  options:
    economytype: "VAULT"
    permission-required: moneypouch.pouches.mvp-pouch
```

```
/lp group vip permission set moneypouch.pouches.vip-pouch true
/lp group mvp permission set moneypouch.pouches.mvp-pouch true
/lp group mvp parent add vip        # MVPs can also open VIP pouches
```

### Pouches from crates or votes that anyone can open

Leave `permission-required` out. Anyone who receives the pouch can open it.

### "Earn it, then unlock it"

The pouch can be found by anyone (drops, trades, auctions), but only opened by players with a certain rank or progress. Use a restricted pouch with a node your rank-up / quest plugin grants.

## Troubleshooting

| Problem | Cause |
|---|---|
| Everyone can open the "VIP" pouch | The line is missing, is `false`/empty, or is not under `options:` (bad indentation). Check `/mpa list` after `/mpa reload`. |
| Nobody (except OPs) can open it, even with the permission | You wrote `permission-required: true`, or a typo in the node. The node in the config and the one you gave must match exactly. |
| It works for me but not for players | You are OP. Test without OP. |
| I changed the permission but nothing changed | Run `/mpa reload`. Pouches already given out use the new permission after a reload. |

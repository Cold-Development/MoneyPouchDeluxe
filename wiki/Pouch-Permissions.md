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

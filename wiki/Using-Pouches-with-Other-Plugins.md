# Using Pouches with Other Plugins

Pouches are usually handed out by other plugins: crates, vote rewards, quests, shops, mob drops. All of them can do it with the same **console command**:

```
mp give <pouch id> <player> [amount]
```

or the short form `mp <pouch id> <player> [amount]`, which older versions used: both work. (No `/` in most plugins' command rewards.)

The only thing that changes between plugins is the **placeholder for the player's name**: `%player%`, `%player_name%`, `{player}`, ... Check your plugin's documentation.

---

## Examples

### Crates (CrazyCrates, ExcellentCrates, ...)

Add a reward whose **command** is:

```
mp moneypouch %player%
```

For the reward's display item in the crate preview, you can copy the pouch's look (name, item, lore).

### Votes (VotingPlugin, NuVotifier + a reward plugin, ...)

```
mp moneypouch %player% 1
```

### Shops / menus (DeluxeMenus, ShopGUI+, ...)

Sell a pouch by taking the money with the shop and running the give command from the console. DeluxeMenus example:

```yaml
  click_commands:
    - "[console] mp moneypouch %player_name%"
  click_requirement:
    requirements:
      money:
        type: has money
        amount: 10000
  # take the money with your economy, e.g. "[takemoney] 10000"
```

### Quests, battle passes, playtime rewards

Any plugin that can run a console command as a reward:

```
mp xppouch %player% 3
```

### Mob drops (MythicMobs, ...)

Run the command as a skill when the mob dies, e.g. in MythicMobs:

```yaml
  Skills:
    - command{c="mp moneypouch <trigger.name>";asCaster=false} @trigger ~onDeath
```

### Events for everyone online

```
mp moneypouch * 1
```

Needs `moneypouch.admin.giveall` when run by a player (the console has every permission).

## Can I put the pouch item directly into a crate instead?

Yes. Pouches are recognised by an invisible tag that contains the pouch id, not by their name or lore, so a pouch item **copied** into a crate (e.g. "add item in hand") still opens normally.

But a copied item keeps the look it had when it was copied. With the command, players always receive the current look from `pouches.yml`. **The command is recommended.**

## Reminders

- The player must be **online** when the command runs.
- A full inventory never loses the pouch: it's dropped at the player's feet.
- If the pouch requires a permission to open, the player needs it too. Pouches given by crates and votes are usually public: leave `permission-required` out. See [Pouch Permissions](Pouch-Permissions).
- Want to hide the "You have received ..." message when a plugin gives a pouch? Set `receive-item: ''` in your [locale file](Messages-and-Languages).
- Every pouch given is written to the [transaction log](Configuration#transaction-log), with who gave it.
- Writing a plugin? Use the [Developer API](Developer-API) (`givePouch`, events) instead of commands.

# Economies (currencies)

An **economy** is the currency a pouch pays out in. Each pouch picks one with `options.economytype`.

- **XP** is built in.
- **Every other currency is a small file** in `plugins/MoneyPouchDeluxe/customeconomytype/`. The file says which console command gives the currency to a player. This is how MoneyPouchDeluxe works with any economy plugin without depending on it.

```
/mpa economies        ← lists every loaded economy and its id
```

---

## XP (built in)

```yaml
  options:
    economytype: "XP"
```

- Gives **experience points** (not levels) with the vanilla mechanic.
- The player must be online when the prize is paid.
- Its name, prefix and suffix are set in `config.yml`:
  ```yaml
  economy:
    xp:
      name: "XP"
      prefix: ""
      suffix: " XP"
  ```

## Custom economies (every other currency)

### The file

```yaml
# plugins/MoneyPouchDeluxe/customeconomytype/vault.yml
transaction-prize-command: "eco give %player% %prize%"
name: "money"
prefix: "&a$"
suffix: ""
```

| Key | Required | Description |
|---|---|---|
| `transaction-prize-command` | ✅ | The command that gives the prize. Runs **from the console**, **without** `/`. |
| `name` | ❌ | Display name, shown by `%economy%` in messages. Defaults to the file name. |
| `prefix` | ❌ | Shown **before** the amount in the title and in messages (`%prefix%`). Colors allowed. |
| `suffix` | ❌ | Shown **after** the amount (`%suffix%`). Colors allowed. |

Placeholders in the command:

| Placeholder | Replaced with |
|---|---|
| `%player%` | The name of the player who opened the pouch |
| `%prize%` | The amount won, as a plain number (`12345`, no separators) |

### The id = the file name

The file name without `.yml` is the economy id you put in `economytype`:

| File | `economytype` |
|---|---|
| `vault.yml` | `"VAULT"` |
| `playerpoints.yml` | `"PlayerPoints"` |
| `tokens.yml` | `"tokens"` |

- Not case-sensitive.
- The file name must contain **only letters and numbers**. `mob_coins.yml` or `mob-coins.yml` are **rejected** (console: `Invalid economy ID`). Use `mobcoins.yml`.
- Only `.yml` files are read.

### Adding a new currency, step by step

1. Find the console command your currency plugin uses to give currency to a player. Test it in the console first, with a real player name, e.g. `tokens give Steve 10`.
2. Create `customeconomytype/tokens.yml`:
   ```yaml
   transaction-prize-command: "tokens give %player% %prize%"
   name: "tokens"
   prefix: ""
   suffix: " Tokens"
   ```
3. `/mpa reload`
4. `/mpa economies` must show `tokens`.
5. Use it in a pouch: `economytype: "tokens"`, then `/mpa reload` again.

### Ready-to-use files

Copy the one you need into `customeconomytype/`. **Always check the command against your plugin's documentation**, commands can change between versions.

<details open>
<summary><b>Money: EssentialsX, or any plugin with <code>/eco give</code></b> → <code>vault.yml</code></summary>

```yaml
transaction-prize-command: "eco give %player% %prize%"
name: "money"
prefix: "&a$"
suffix: ""
```
</details>

<details>
<summary><b>Money: CMI</b> → <code>vault.yml</code></summary>

```yaml
transaction-prize-command: "cmi money give %player% %prize%"
name: "money"
prefix: "&a$"
suffix: ""
```
</details>

<details>
<summary><b>PlayerPoints</b> → <code>playerpoints.yml</code></summary>

```yaml
transaction-prize-command: "points give %player% %prize%"
name: "points"
prefix: ""
suffix: " Points"
```
</details>

<details>
<summary><b>TokenManager</b> → <code>tokenmanager.yml</code></summary>

```yaml
transaction-prize-command: "tm add %player% %prize%"
name: "tokens"
prefix: ""
suffix: " Tokens"
```
</details>

<details>
<summary><b>Vanilla items</b> (diamonds, emeralds, ...) → <code>diamonds.yml</code></summary>

```yaml
transaction-prize-command: "give %player% diamond %prize%"
name: "diamonds"
prefix: ""
suffix: " Diamonds"
```
If the inventory is full, vanilla `/give` drops the rest on the ground.
</details>

<details>
<summary><b>Any other plugin</b></summary>

As long as it has a console command that takes a player name and an amount, it works:

```yaml
transaction-prize-command: "<command> %player% %prize%"
```
</details>

### Prefix and suffix

They're shown around the amount:

- in the **title**: `prefix` + `12,345` + `suffix`, colored by `prefix-colour` / `suffix-colour` in `config.yml`;
- in the **messages** `prize-message` and `reward-error`, through `%prefix%%prize%%suffix%`.

```
prefix: "&a$"   suffix: ""         →  $12,345
prefix: ""      suffix: " Points"  →  12,345 Points
prefix: "⛃ "    suffix: " coins"   →  ⛃ 12,345 coins
```

If the file has no `prefix` / `suffix` / `name`, the plugin uses `economy.<id>.prefix` / `suffix` / `name` from `config.yml`, if present.

### What if the command fails?

If the command **doesn't exist** (the plugin was removed, a typo in the command name):

- the console logs: `Custom economy command failed, Steve did not receive 12345. Command: /eco give Steve 12345`
- the player gets the `reward-error` message.

You can then give the prize by hand using the command from the log.

> ⚠️ The plugin can only detect a command that doesn't exist. If the command exists but **refuses** (wrong arguments, the target plugin has an error), MoneyPouchDeluxe can't know. That's why you should **test the command in the console** before using it.

Also see `error-handling` on [Main Configuration](Configuration#error-handling).

### If a file is missing

If an economy file is deleted (or the whole `customeconomytype/` folder), the pouches using it are **skipped** with a warning, the other pouches keep working. Pouches of that type that players already have show the `invalid-pouch` message until the file is back.

The default files (`vault.yml`, `playerpoints.yml`, `examplecustomeconomy.yml`) are only generated when the `customeconomytype/` folder **doesn't exist**. To get them back, rename or delete the folder and restart, or copy them from [here](https://github.com/Cold-Development/MoneyPouchDeluxe/tree/master/src/main/resources/customeconomytype).

---

## For developers: registering an economy from your plugin

If a command isn't enough, your plugin can register its own economy type.

1. Add `MoneyPouchDeluxe` to `softdepend` in your `plugin.yml`.
2. Extend `dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType`:

```java
import dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType;
import dev.padrewin.moneypouchdeluxe.Exception.PaymentFailedException;
import org.bukkit.entity.Player;

public class GemsEconomyType extends EconomyType {

    public GemsEconomyType() {
        super("gems", "", " Gems"); // name (%economy%), prefix, suffix
    }

    @Override
    public void processPayment(Player player, long amount) {
        if (!MyGemsApi.add(player.getUniqueId(), amount)) {
            // logged by MoneyPouchDeluxe, and the player gets 'reward-error'
            throw new PaymentFailedException("Could not add gems");
        }
    }

    @Override
    public String toString() {
        return "Gems"; // shown in /mpa list and /mpa economies
    }
}
```

3. Register it in `onEnable`:

```java
MoneyPouchDeluxe mpd = (MoneyPouchDeluxe) Bukkit.getPluginManager().getPlugin("MoneyPouchDeluxe");
if (mpd != null) {
    mpd.registerEconomyType("gems", new GemsEconomyType()); // "gems" = the economytype in pouches.yml
}
```

Notes:
- Ids are alphanumeric and stored lowercase. A conflicting id is ignored with a warning.
- Pouches are loaded one tick after the server finishes enabling plugins, so registering in `onEnable` is early enough.
- `/mpa reload` clears registered economies (only XP and the files are reloaded). If your plugin must survive a reload, register again when needed, or prefer a command-based file.

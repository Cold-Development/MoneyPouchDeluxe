# Economies (currencies)

An **economy** is the currency a pouch pays out in. Each pouch picks one with `options.economytype`.

- **XP** is built in.
- **Every other currency is a small file** in `plugins/MoneyPouchDeluxe/customeconomytype/`. The file says how the currency is given to a player:
  - **through a hook**: Vault or PlayerPoints, paid through their API, which tells whether the payment worked;
  - **through a console command**: any plugin with a "give" command, without MoneyPouchDeluxe depending on it.

```
/mp economies        ← lists every loaded economy, its id and how it pays
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

## The economy files

```yaml
# plugins/MoneyPouchDeluxe/customeconomytype/vault.yml
hook: vault
transaction-prize-command: "eco give %player% %prize%"
name: "money"
prefix: "&a$"
suffix: ""
```

| Key | Required | Description |
|---|---|---|
| `hook` | ❌ | `vault`, `playerpoints` or `command` (default). See [below](#hooks-vault-and-playerpoints). |
| `transaction-prize-command` | for `hook: command` | The command that gives the prize. Runs **from the console**, **without** `/`. Also used by a hook when its plugin isn't installed. |
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

## Hooks: Vault and PlayerPoints

| `hook` | Pays through | Needs |
|---|---|---|
| `vault` | Vault, so whichever economy plugin is behind it (EssentialsX, CMI, ...) | Vault + an economy plugin |
| `playerpoints` | The PlayerPoints API | PlayerPoints |
| `command` (or no `hook`) | `transaction-prize-command` | the plugin that owns the command |

A hook knows whether the payment worked: if the economy refuses it, the player gets `reward-error` and the failure is logged. If the hooked plugin isn't installed, `transaction-prize-command` is used instead (when the file has one).

`vault.yml` and `playerpoints.yml` are generated with their hook. **`VAULT` and `PlayerPoints` also work without these files**, as long as Vault / PlayerPoints is installed: the name, prefix and suffix then come from `economy.vault` / `economy.playerpoints` in `config.yml` (default `&a$` and ` Points`).

## Adding a new currency, step by step

1. Find the console command your currency plugin uses to give currency to a player. Test it in the console first, with a real player name, e.g. `tokens give Steve 10`.
2. Create `customeconomytype/tokens.yml`:
   ```yaml
   transaction-prize-command: "tokens give %player% %prize%"
   name: "tokens"
   prefix: ""
   suffix: " Tokens"
   ```
3. `/mp reload`
4. `/mp economies` must show `tokens`.
5. Use it in a pouch: `economytype: "tokens"`, then `/mp reload` again.

### Ready-to-use files

Copy the one you need into `customeconomytype/`. **Always check the command against your plugin's documentation**, commands can change between versions.

<details open>
<summary><b>Money through Vault (EssentialsX, CMI, ...)</b> → <code>vault.yml</code></summary>

```yaml
hook: vault
transaction-prize-command: "eco give %player% %prize%"   # only used if Vault is missing
name: "money"
prefix: "&a$"
suffix: ""
```
</details>

<details>
<summary><b>PlayerPoints</b> → <code>playerpoints.yml</code></summary>

```yaml
hook: playerpoints
transaction-prize-command: "points give %player% %prize%"   # only used if PlayerPoints is missing
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

## Prefix and suffix

They're shown around the amount:

- in the **title**: `prefix` + `12,345` + `suffix`, colored by `prefix-colour` / `suffix-colour` in `config.yml`;
- in the **messages** `prize-message`, `prize-message-stack` and `reward-error`, through `%prefix%%prize%%suffix%`;
- in `/mp list`.

```
prefix: "&a$"   suffix: ""         →  $12,345
prefix: ""      suffix: " Points"  →  12,345 Points
prefix: "⛃ "    suffix: " coins"   →  ⛃ 12,345 coins
```

If the file has no `prefix` / `suffix` / `name`, the plugin uses `economy.<id>.prefix` / `suffix` / `name` from `config.yml`, if present.

## What if a payment fails?

In every case the player gets `reward-error` **instead of** the prize message, the failure is logged in the console (`error-handling.log-failed-transactions`) and in the [transaction log](Configuration#transaction-log), and the pouch is refunded if `error-handling.refund-pouch` is `true`.

What can be detected depends on how the economy pays:

| Economy | Detected failures |
|---|---|
| `hook: vault` | No economy plugin behind Vault, the economy refused the deposit, an error in the economy plugin |
| `hook: playerpoints` | PlayerPoints refused, an amount too large for points |
| `hook: command` | The command doesn't exist (its plugin was removed, a typo), or it threw an error |
| `XP` | The player was offline, an amount too large |

Example console line:

```
Failed to process payment from pouch 'moneypouch' for player 'Steve' (069a79f4-...) amount 12345 Command (/eco give %player% %prize%): unknown command: /eco give Steve 12345
```

> ⚠️ With `hook: command`, the plugin can only detect a command that doesn't exist. If the command exists but **refuses** (wrong arguments, the target plugin has an error), MoneyPouchDeluxe can't know. That's why you should **test the command in the console** before using it, and prefer the Vault / PlayerPoints hooks when you can.

## If a file is missing

If an economy file is deleted (or the whole `customeconomytype/` folder), the pouches using it are **skipped** with a warning, the other pouches keep working. Pouches of that type that players already have show the `invalid-pouch` message until the file is back. Exception: `VAULT` and `PlayerPoints` keep working without their file while their plugin is installed.

The default files (`vault.yml`, `playerpoints.yml`, `examplecustomeconomy.yml`) are only generated when the `customeconomytype/` folder **doesn't exist**. To get them back, rename or delete the folder and restart, or copy them from [here](https://github.com/Cold-Development/MoneyPouchDeluxe/tree/master/src/main/resources/customeconomytype).

## For developers

A plugin can register its own economy through the API: see [Developer API](Developer-API#your-own-economy).

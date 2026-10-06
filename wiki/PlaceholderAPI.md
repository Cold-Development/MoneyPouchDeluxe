# PlaceholderAPI

With [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) installed, MoneyPouchDeluxe adds placeholders with each player's statistics, for scoreboards, tab lists, holograms, menus, leaderboards...

No eCloud download is needed: the placeholders are registered by the plugin itself.

## Placeholders

| Placeholder | Value |
|---|---|
| `%moneypouch_opened%` | Pouches the player opened |
| `%moneypouch_opened_<pouch>%` | Pouches of one kind they opened, e.g. `%moneypouch_opened_moneypouch%` |
| `%moneypouch_won_<economy>%` | Total amount they won in an economy, e.g. `%moneypouch_won_vault%`, `%moneypouch_won_xp%` |
| `%moneypouch_won_pouch_<pouch>%` | Total amount they won from one kind of pouch, e.g. `%moneypouch_won_pouch_vippouch%` |

Add **`_formatted`** at the end of any of them to get the number with separators (`format.separator` in `config.yml`):

```
%moneypouch_won_vault%             → 1250000
%moneypouch_won_vault_formatted%   → 1,250,000
```

- `<pouch>` is the pouch id from `pouches.yml`, `<economy>` the economy id from `/mp economies`. Not case-sensitive.
- Amounts are per economy because adding money, XP and points together wouldn't mean anything.
- Only **successful** payments are counted. Pouches opened with sneak + right click count one by one (a stack of 16 = 16 opened).
- Statistics start counting from the moment you install 2.0.

Test one with: `/papi parse me %moneypouch_opened%`

## Where statistics are saved

In a SQLite file, `plugins/MoneyPouchDeluxe/moneypouchdeluxe.db`, or in MySQL with `mysql-settings` in `config.yml` (see [Database](Configuration#database)).

Placeholders never wait for the database: statistics are loaded in the background when a player joins, and new results are saved every few seconds and when the server stops. For a player whose statistics aren't loaded yet (e.g. an offline player on a leaderboard), a placeholder shows `0` for a moment, then the real value.

## Placeholders in messages

The other way around also works: any PlaceholderAPI placeholder can be used in the [messages](Messages-and-Languages), for the player who receives the message:

```yaml
prize-message: '&fYou have received %prefix%%prize%%suffix%&f! Balance: &a%vault_eco_balance_formatted%'
```

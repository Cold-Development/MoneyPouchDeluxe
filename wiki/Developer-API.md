# Developer API

MoneyPouchDeluxe has events and an API class for other plugins: boosters, region restrictions, quests, Discord logs, your own currency...

## Setup

Add MoneyPouchDeluxe to your `plugin.yml`:

```yaml
softdepend: [MoneyPouchDeluxe]   # or depend, if your plugin needs it
```

and compile against the plugin jar (e.g. as a `provided` / `compileOnly` dependency from a local file).

Everything is in `dev.padrewin.moneypouchdeluxe.api`.

---

## Events

### `PouchOpenEvent`

Called when a player opens pouches, **before** they're taken from their hand and before the reveal. Cancellable; the amount can be changed.

```java
@EventHandler
public void onPouchOpen(PouchOpenEvent event) {
    Player player = event.getPlayer();

    // Boosters: double every pouch for players with a permission
    if (player.hasPermission("vip.booster")) {
        event.setAmount(event.getAmount() * 2);
    }

    // Restrictions: no pouches in the arena (the pouch stays in the player's hand)
    if (player.getWorld().getName().equals("arena")) {
        event.setCancelled(true);
        player.sendMessage("You can't open pouches here.");
    }
}
```

| Method | |
|---|---|
| `getPlayer()` | The player opening the pouches |
| `getPouch()` | The kind of pouch (`getId()`, `getMinRange()`, `getMaxRange()`, `getEconomyType()`, ...) |
| `getCount()` | How many pouches are opened at once: 1, or the stack size with sneak + right click |
| `getAmount()` / `setAmount(long)` | The **total** the player will receive for all `getCount()` pouches, in the pouch's economy |
| `isCancelled()` / `setCancelled(boolean)` | Cancelled: nothing is paid and the pouches stay in the player's hand |

The changed amount is the one revealed in the title, paid, counted in the statistics and written to the transaction log.

### `PouchRewardEvent`

Called once the reward has been paid, or has failed to be paid. Read-only.

```java
@EventHandler
public void onPouchReward(PouchRewardEvent event) {
    if (event.isSuccessful()) {
        discord.send(event.getPlayer().getName() + " won " + event.getAmount() + " from " + event.getPouch().getId());
    } else {
        getLogger().warning("Payment failed: " + event.getFailureReason());
    }
}
```

| Method | |
|---|---|
| `getPlayer()` | The player (may have left during the reveal) |
| `getPouch()`, `getCount()`, `getAmount()` | As above |
| `isSuccessful()` | `true` if the player received the reward |
| `getFailureReason()` | Why the payment failed, or `null` |

Some economies finish their payment off the main thread (e.g. a console command on Folia, or your own economy). In that case the event is **asynchronous** (`event.isAsynchronous()`): don't touch the world or inventories from the listener without scheduling back.

---

## `MoneyPouchDeluxeAPI`

```java
MoneyPouchDeluxeAPI api = MoneyPouchDeluxeAPI.get();
```

| Method | |
|---|---|
| `getPouches()` | Every loaded pouch |
| `getPouch(String id)` | A pouch by id (case-insensitive), or `null` |
| `createItem(Pouch, int amount)` | A new item stack of the pouch (up to the item's stack size) |
| `givePouch(Player, Pouch, int amount)` | Gives pouches; what doesn't fit is dropped at the player's feet. Safe from any thread (Folia included). Written to the transaction log as `GIVE API`. |
| `isPouch(ItemStack)` / `getPouchId(ItemStack)` | Whether an item is a pouch, and which one |
| `registerEconomyType(String id, EconomyType)` | Adds your own currency (below) |
| `getEconomyType(String id)` | An economy by id, or `null` |

Pouches are loaded **one tick after the server starts** (so every economy plugin is ready), and again on every `/mp reload`. Calling `getPouches()` in your `onEnable` returns an empty list: wait a tick, or use them when needed.

---

## Your own economy

When a [command-based economy](Custom-Economy-Types) isn't enough, register an economy from your plugin. Extend `dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType`:

```java
import dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType;
import dev.padrewin.moneypouchdeluxe.Exception.PaymentFailedException;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class GemsEconomyType extends EconomyType {

    public GemsEconomyType() {
        super("gems", "", " Gems"); // name (%economy%), prefix, suffix
    }

    @Override
    public CompletableFuture<Void> processPayment(Player player, long amount) {
        if (!MyGemsApi.add(player.getUniqueId(), amount)) {
            // The player gets 'reward-error', the failure is logged (and refunded if enabled)
            return CompletableFuture.failedFuture(new PaymentFailedException("Could not add gems"));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String toString() {
        return "Gems"; // shown in /mp economies
    }
}
```

- `processPayment` is called on the player's thread (the main thread on Paper/Spigot). Return a future that completes when the reward is given, or completes exceptionally if it wasn't. It can complete later, e.g. after an asynchronous database call: the player is told the outcome only then.
- `PaymentFailedException(String)` is the expected failure; any other exception is logged with its stack trace.

Register it in `onEnable`:

```java
MoneyPouchDeluxeAPI.get().registerEconomyType("gems", new GemsEconomyType()); // economytype: "gems" in pouches.yml
```

- Ids are stored lowercase. An id already used by another economy is refused (`false`).
- Registering in `onEnable` is early enough: pouches load a tick later. The economy is kept across `/mp reload`.

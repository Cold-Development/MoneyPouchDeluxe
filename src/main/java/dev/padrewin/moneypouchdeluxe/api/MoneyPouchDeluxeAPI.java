package dev.padrewin.moneypouchdeluxe.api;

import dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Pouch;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Entry point for other plugins: {@code MoneyPouchDeluxeAPI.get()}.
 * <p>
 * Events: {@link dev.padrewin.moneypouchdeluxe.api.event.PouchOpenEvent} (cancellable, amount can be
 * changed) and {@link dev.padrewin.moneypouchdeluxe.api.event.PouchRewardEvent}.
 */
public final class MoneyPouchDeluxeAPI {

    private static MoneyPouchDeluxeAPI instance;

    private final MoneyPouchDeluxe plugin;

    private MoneyPouchDeluxeAPI(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    /**
     * @return the API (MoneyPouchDeluxe must be enabled)
     */
    @NotNull
    public static MoneyPouchDeluxeAPI get() {
        MoneyPouchDeluxe plugin = MoneyPouchDeluxe.getInstance();
        if (plugin == null) {
            throw new IllegalStateException("MoneyPouchDeluxe is not enabled");
        }
        // A new plugin instance after a server /reload gets a new API
        if (instance == null || instance.plugin != plugin) {
            instance = new MoneyPouchDeluxeAPI(plugin);
        }
        return instance;
    }

    /**
     * @return every loaded pouch (pouches.yml is loaded one tick after the server starts)
     */
    @NotNull
    public List<Pouch> getPouches() {
        return Collections.unmodifiableList(this.plugin.getPouches());
    }

    /**
     * @return the pouch with this id (as in pouches.yml, case-insensitive), or null
     */
    @Nullable
    public Pouch getPouch(@NotNull String id) {
        return this.plugin.getPouch(id);
    }

    /**
     * @return a new item stack of the pouch (amount up to the item's stack size)
     */
    @NotNull
    public ItemStack createItem(@NotNull Pouch pouch, int amount) {
        ItemStack item = pouch.getItemStack().clone();
        item.setAmount(Math.max(1, Math.min(amount, item.getMaxStackSize())));
        return item;
    }

    /**
     * @return the id of the pouch this item is, or null if it isn't a pouch
     */
    @Nullable
    public String getPouchId(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        return meta == null ? null : meta.getPersistentDataContainer().get(Pouch.getIdKey(), PersistentDataType.STRING);
    }

    public boolean isPouch(@Nullable ItemStack item) {
        return this.getPouchId(item) != null;
    }

    /**
     * Gives pouches to a player, dropping at their feet what doesn't fit. Safe to call from any
     * thread: the inventory is changed on the player's own thread (Folia).
     */
    public void givePouch(@NotNull Player player, @NotNull Pouch pouch, int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be at least 1");
        }
        this.plugin.runAtPlayer(player, () -> {
            if (player.isOnline()) {
                this.plugin.giveOrDrop(player, pouch.getItemStack(), amount);
            }
        });
        this.plugin.getTransactionLog().logGive("API", player.getName(), player.getUniqueId(), pouch.getId(), amount);
    }

    /**
     * Adds an economy that pouches can use with {@code economytype: <id>} in pouches.yml. Register it in
     * your plugin's onEnable: pouches are loaded one tick after the server starts. It is kept across
     * /mp reload.
     *
     * @return false if the id is already used by another economy
     */
    public boolean registerEconomyType(@NotNull String id, @NotNull EconomyType economyType) {
        return this.plugin.registerExternalEconomyType(id, economyType);
    }

    /**
     * @return the economy registered under this id (case-insensitive), or null
     */
    @Nullable
    public EconomyType getEconomyType(@NotNull String id) {
        return this.plugin.getEconomyType(id);
    }

}

package dev.padrewin.moneypouchdeluxe;

import dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class Pouch {

    private final String id;
    private final long minRange;
    private final long maxRange;
    private final ItemStack itemStack;
    private final EconomyType economyType;
    private final String permission;

    /**
     * @param permission permission needed to open the pouch, or null if anyone can open it
     */
    public Pouch(String id, long minRange, long maxRange, ItemStack itemStack, EconomyType economyType, String permission) {
        this.id = id;
        // Both ends are inclusive; from == to makes a fixed-amount pouch
        this.minRange = Math.min(minRange, maxRange);
        this.maxRange = Math.max(minRange, maxRange);
        this.itemStack = itemStack;
        this.economyType = economyType;
        this.permission = permission;
        applyPouchId();
    }

    /**
     * The pouch's id goes in the item's persistent data: that's how a pouch is recognised when
     * it's used, whatever its name, lore or material.
     */
    private void applyPouchId() {
        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(getIdKey(), PersistentDataType.STRING, id);
            this.itemStack.setItemMeta(meta);
        }
    }

    public static NamespacedKey getIdKey() {
        if (MoneyPouchDeluxe.getInstance() == null) {
            throw new IllegalStateException("MoneyPouchDeluxe instance is not initialized.");
        }
        return new NamespacedKey(MoneyPouchDeluxe.getInstance(), "pouch-id");
    }

    public String getId() {
        return id;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isPermissionRequired() {
        return permission != null;
    }

    public long getMinRange() {
        return minRange;
    }

    public long getMaxRange() {
        return maxRange;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public EconomyType getEconomyType() {
        return economyType;
    }
}

package dev.padrewin.moneypouchdeluxe;

import dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class Pouch {

    private final String id;
    private final long minRange;
    private final long maxRange;
    private final ItemStack itemStack;
    private final EconomyType economyType;
    private final boolean permissionRequired;
    private UUID uuid;
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
        this.permissionRequired = permission != null;
        this.permission = permission;
        applyUUIDToItemStack(id);
    }

    public String getId() {
        return id;
    }

    public void initializeUUID() {
        this.uuid = UUID.randomUUID();
        applyUUIDToItemStack(id);
    }

    public String getPermission() {
        return permission;
    }

    private void applyUUIDToItemStack(String pouchId) {
        if (MoneyPouchDeluxe.getInstance() == null) {
            throw new IllegalStateException("MoneyPouchDeluxe instance is not initialized.");
        }

        ItemMeta meta = this.itemStack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(MoneyPouchDeluxe.getInstance(), "pouch-id"), PersistentDataType.STRING, pouchId);
            this.itemStack.setItemMeta(meta);
        }
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isPermissionRequired() {
        return permissionRequired;
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
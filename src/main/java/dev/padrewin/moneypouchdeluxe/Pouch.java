package dev.padrewin.moneypouchdeluxe;

import dev.padrewin.moneypouchdeluxe.EconomyType.EconomyType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class Pouch {

    private final String id; // acest id este moneypouch, pointspouch etc.
    private final long minRange;
    private final long maxRange;
    private final ItemStack itemStack;
    private final EconomyType economyType;
    private final boolean purchasable;
    private final EconomyType purchaseCurrency;
    private final long purchasePrice;
    private final ItemStack shopItemStack;
    private final boolean permissionRequired;
    private UUID uuid;

    public Pouch(String id, long minRange, long maxRange, ItemStack itemStack, EconomyType economyType, boolean permissionRequired, String pouchId) {
        this.id = id;
        this.minRange = minRange >= maxRange ? maxRange - 1 : minRange;
        this.maxRange = maxRange;
        this.itemStack = itemStack;
        this.economyType = economyType;
        this.permissionRequired = permissionRequired;
        this.purchasable = false;
        this.purchaseCurrency = null;
        this.purchasePrice = 0;
        this.shopItemStack = null;
        applyUUIDToItemStack(pouchId);
    }

    public Pouch(String id, long minRange, long maxRange, ItemStack itemStack, EconomyType economyType, boolean permissionRequired,
                 boolean purchasable, EconomyType purchaseCurrency, long purchasePrice, ItemStack shopItemStack, String pouchId) {
        this.id = id;
        this.minRange = minRange >= maxRange ? maxRange - 1 : minRange;
        this.maxRange = maxRange;
        this.itemStack = itemStack;
        this.economyType = economyType;
        this.permissionRequired = permissionRequired;
        this.purchasable = purchasable;
        this.purchaseCurrency = purchaseCurrency;
        this.purchasePrice = purchasePrice;
        this.shopItemStack = shopItemStack;
        applyUUIDToItemStack(pouchId);
    }

    public String getId() {
        return id;
    }

    public void initializeUUID() {
        this.uuid = UUID.randomUUID();
        applyUUIDToItemStack(id);
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

    public boolean isPurchasable() {
        return purchasable;
    }

    public EconomyType getPurchaseCurrency() {
        return purchaseCurrency;
    }

    public long getPurchasePrice() {
        return purchasePrice;
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

    public ItemStack getShopItemStack() {
        return shopItemStack;
    }

    public EconomyType getEconomyType() {
        return economyType;
    }
}
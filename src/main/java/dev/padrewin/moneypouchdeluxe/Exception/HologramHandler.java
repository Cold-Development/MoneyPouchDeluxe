package dev.padrewin.moneypouchdeluxe.Exception;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HologramHandler implements Listener {

    private final JavaPlugin plugin;

    public HologramHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        cleanUpHologramsOnServerStart();
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Item droppedItem = event.getItemDrop();
        createHologram(droppedItem, plugin);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        Item pickedItem = event.getItem();
        NamespacedKey hologramKey = new NamespacedKey(plugin, "is_hologram");

        for (ArmorStand armorStand : pickedItem.getWorld().getEntitiesByClass(ArmorStand.class)) {
            if (armorStand.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE)) {
                if (armorStand.getLocation().distance(pickedItem.getLocation()) < 1.5) {
                    armorStand.remove();
                }
            }
        }
    }

    public static void createHologram(Item droppedItem, JavaPlugin plugin) {
        ItemStack itemStack = droppedItem.getItemStack();

        if (itemStack.hasItemMeta() && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "pouch-id"), PersistentDataType.STRING)) {
            ArmorStand hologram = droppedItem.getWorld().spawn(droppedItem.getLocation().add(0, 1, 0), ArmorStand.class, stand -> {
                stand.setCustomName(ChatColor.translateAlternateColorCodes('&', itemStack.getItemMeta().getDisplayName()));
                stand.setCustomNameVisible(true);
                stand.setGravity(false);
                stand.setInvisible(true);
                stand.setMarker(true);

                NamespacedKey hologramKey = new NamespacedKey(plugin, "is_hologram");
                stand.getPersistentDataContainer().set(hologramKey, PersistentDataType.BYTE, (byte) 1);
            });

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (droppedItem.isValid() && !droppedItem.isDead()) {
                        hologram.teleport(droppedItem.getLocation().add(0, 1, 0));
                    } else {
                        hologram.remove();
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    private void cleanUpHologramsOnServerStart() {
        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
                if (armorStand.getPersistentDataContainer().has(new NamespacedKey(plugin, "is_hologram"), PersistentDataType.BYTE)) {
                    boolean foundNearbyItem = false;
                    for (Item item : world.getEntitiesByClass(Item.class)) {
                        if (item.getLocation().distance(armorStand.getLocation()) < 1.5) {
                            foundNearbyItem = true;
                            break;
                        }
                    }

                    if (!foundNearbyItem) {
                        armorStand.remove();
                    }
                }
            }
        }
    }
}

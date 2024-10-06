package dev.padrewin.moneypouchdeluxe.Exception;

import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class HologramHandler implements Listener {

    private static final Map<Item, ArmorStand> hologramMap = new HashMap<>();

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

        ArmorStand hologram = hologramMap.get(pickedItem);
        if (hologram != null) {
            hologram.remove();
            hologramMap.remove(pickedItem);
        }
    }

    public static void createHologram(Item droppedItem, JavaPlugin plugin) {
        if (!(plugin instanceof MoneyPouchDeluxe) || !((MoneyPouchDeluxe) plugin).areHologramsEnabled()) {
            return;
        }

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

            hologramMap.put(droppedItem, hologram);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (droppedItem.isValid() && !droppedItem.isDead()) {
                        hologram.teleport(droppedItem.getLocation().add(0, 1, 0));
                    } else {
                        hologram.remove();
                        hologramMap.remove(droppedItem);
                        this.cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    private void cleanUpHologramsOnServerStart() {
        for (World world : Bukkit.getWorlds()) {
            for (Item item : world.getEntitiesByClass(Item.class)) {
                ArmorStand hologram = hologramMap.get(item);
                if (hologram != null && item.getLocation().distance(hologram.getLocation()) >= 1.5) {
                    hologram.remove();
                    hologramMap.remove(item);
                }
            }
        }
    }

    public void killAllPouchHolograms() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if (livingEntity.getPersistentDataContainer().has(new NamespacedKey(plugin, "is_hologram"), PersistentDataType.BYTE)) {
                        livingEntity.remove();
                    }
                }
            }
        }
        hologramMap.clear();
    }

}

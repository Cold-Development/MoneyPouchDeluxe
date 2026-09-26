package dev.padrewin.moneypouchdeluxe.Listener;

import dev.padrewin.moneypouchdeluxe.utils.Text;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Pouch;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class UseListenerLatest extends UseListener implements Listener {

    public UseListenerLatest(MoneyPouchDeluxe plugin) {
        super(plugin);
    }

    @EventHandler
    @Override
    public void onPlayerUse(PlayerInteractEvent event) {
        //plugin.getLogger().info("PlayerInteractEvent triggered by " + event.getPlayer().getName());
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            //plugin.getLogger().info("Event action not right-click, ignoring.");
            return;
        }

        if (event.getHand() == EquipmentSlot.HAND) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                return;
            }

            // Obține ID-ul pouch-ului din item
            String pouchId = getPouchId(itemInHand);
            if (pouchId == null) {
                //plugin.getLogger().info("No pouch ID found for item in hand.");
                return;
            }

            // Iterează prin pouch-uri și verifică permisiunea
            for (Pouch pouch : plugin.getPouches()) {
                if (pouch.getId().equalsIgnoreCase(pouchId)) {
                    event.setCancelled(true);
                    if (!canOpen(player, pouch)) {
                        return;
                    }

                    // Dacă are permisiunea, continuă utilizarea pouch-ului
                    usePouch(player, pouch);

                    // Elimină pouch-ul din inventar
                    ItemStack itemToRemove = player.getInventory().getItemInMainHand();
                    if (itemToRemove.getAmount() > 1) {
                        itemToRemove.setAmount(itemToRemove.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInMainHand(null);
                    }
                    player.updateInventory(); // Actualizează inventarul

                    //plugin.getLogger().info("Pouch " + pouch.getId() + " removed from " + player.getName() + "'s inventory.");
                    return;
                }
            }

            // A pouch whose tier was removed from pouches.yml: don't let it be placed or used as a normal item
            event.setCancelled(true);
            Text.send(player, plugin.getMessage(MoneyPouchDeluxe.Message.INVALID_POUCH));
        }
    }

    @Override
    protected String getPouchId(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(
                new NamespacedKey(MoneyPouchDeluxe.getInstance(), "pouch-id"), PersistentDataType.STRING)) {
            return item.getItemMeta().getPersistentDataContainer().get(
                    new NamespacedKey(MoneyPouchDeluxe.getInstance(), "pouch-id"), PersistentDataType.STRING);
        }
        return null;
    }
}

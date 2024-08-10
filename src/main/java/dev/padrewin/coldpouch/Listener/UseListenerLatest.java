package dev.padrewin.coldpouch.Listener;

import dev.padrewin.coldpouch.ColdPouch;
import dev.padrewin.coldpouch.Pouch;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class UseListenerLatest extends UseListener implements Listener {

    public UseListenerLatest(ColdPouch plugin) {
        super(plugin);
    }

    @EventHandler
    @Override
    public void onPlayerUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (event.getHand() == EquipmentSlot.HAND) {
            onRightClickInMainHand(player, event);
        } else {
            ItemStack offHandItem = player.getInventory().getItemInOffHand();
            if (offHandItem != null && offHandItem.getType() == Material.PLAYER_HEAD && offHandItem.hasItemMeta()) {
                ItemMeta offHandMeta = offHandItem.getItemMeta();
                if (offHandMeta instanceof SkullMeta) {
                    SkullMeta offHandSkullMeta = (SkullMeta) offHandMeta;
                    String itemName = offHandSkullMeta.getDisplayName();
                    List<String> itemLore = offHandSkullMeta.getLore();

                    for (Pouch p : super.plugin.getPouches()) {
                        ItemStack pouchItem = p.getItemStack();
                        if (pouchItem != null && pouchItem.getType() == Material.PLAYER_HEAD && pouchItem.hasItemMeta()) {
                            ItemMeta pouchMeta = pouchItem.getItemMeta();
                            if (pouchMeta instanceof SkullMeta) {
                                SkullMeta pouchSkullMeta = (SkullMeta) pouchMeta;
                                String pouchName = pouchSkullMeta.getDisplayName();
                                List<String> pouchLore = pouchSkullMeta.getLore();

                                if (itemName != null && itemName.equals(pouchName) &&
                                        (itemLore != null && itemLore.equals(pouchLore))) {
                                    event.setCancelled(true);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

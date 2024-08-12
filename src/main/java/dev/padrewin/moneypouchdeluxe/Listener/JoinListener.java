package dev.padrewin.moneypouchdeluxe.Listener;

import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class JoinListener implements Listener {

    private final MoneyPouchDeluxe plugin;

    public JoinListener(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getUpdater().checkForUpdate().thenAccept(updateAvailable -> {
            if (updateAvailable) {
                String currentVersion = plugin.getDescription().getVersion().trim();
                String latestVersion = plugin.getUpdater().getReturnedVersion().trim();
                String updateLink = plugin.getUpdater().getUpdateLink();

                String updateMessage = Objects.requireNonNull(plugin.getConfig().getString("messages.update_notification"))
                        .replace("%latest_version%", latestVersion)
                        .replace("%current_version%", currentVersion)
                        .replace("%update_link%", updateLink);

                String finalUpdateMessage = ChatColor.translateAlternateColorCodes('&', updateMessage);
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                    event.getPlayer().sendMessage(finalUpdateMessage);
                }, 50L);
            }
        });
    }
}

package dev.padrewin.moneypouchdeluxe.Listener;

import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class JoinListener implements Listener {
    private final MoneyPouchDeluxe plugin;

    public JoinListener(MoneyPouchDeluxe plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        if (this.plugin.getUpdater().isUpdateReady()) {
            String currentVersion = this.plugin.getDescription().getVersion().trim().toLowerCase();
            String latestVersion = this.plugin.getUpdater().getReturnedVersion().trim().toLowerCase();

            if (!currentVersion.startsWith("v")) {
                currentVersion = "v" + currentVersion;
            }

            if (!latestVersion.startsWith("v")) {
                latestVersion = "v" + latestVersion;
            }

            if (!currentVersion.equals(latestVersion)) {
                String updateMessage = this.plugin.getConfig().getString("messages.update_notification");

                updateMessage = updateMessage.replace("%new_version%", this.plugin.getUpdater().getReturnedVersion())
                        .replace("%current_version%", currentVersion)
                        .replace("%update_link%", this.plugin.getUpdater().getUpdateLink());

                String finalUpdateMessage = updateMessage;
                Bukkit.getScheduler().runTaskLater((Plugin) this.plugin, () -> {
                    event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', finalUpdateMessage));
                    plugin.getLogger().info("Update message sent to " + event.getPlayer().getName());
                }, 50L);
            } else {
                plugin.getLogger().info("");
                plugin.getLogger().info("No new update");
                plugin.getLogger().info("");
            }
        }
    }
}
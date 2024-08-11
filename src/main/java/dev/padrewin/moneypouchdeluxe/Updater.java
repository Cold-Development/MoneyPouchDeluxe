package dev.padrewin.moneypouchdeluxe;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Updater {

    private final JavaPlugin plugin;
    private final int resourceId;
    private String latestVersion;
    private String returnedVersion;

    public Updater(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.latestVersion = plugin.getDescription().getVersion();
    }

    public void checkForUpdate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoOutput(true);
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    try (Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream()))) {
                        if (scanner.hasNext()) {
                            latestVersion = scanner.next().trim();

                            if (!latestVersion.startsWith("v")) {
                                latestVersion = "v" + latestVersion;
                            }

                            String currentVersion = plugin.getDescription().getVersion().trim();

                            if (!currentVersion.startsWith("v")) {
                                currentVersion = "v" + currentVersion;
                            }

                            if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                                Bukkit.getLogger().info("");
                                Bukkit.getLogger().info("[MoneyPouchDeluxe] A new update is available: " + latestVersion + " (You are running version " + currentVersion + ")");
                                Bukkit.getLogger().info("");
                            }
                        }
                    }
                } catch (IOException e) {
                    Bukkit.getLogger().severe("[MoneyPouchDeluxe] Update check failed: " + e.getMessage());
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L * 60 * 60);
    }

    public boolean isUpdateReady() {
        String currentVersion = plugin.getDescription().getVersion().trim().toLowerCase();
        String latestVersion = getReturnedVersion().trim().toLowerCase();

        if (!currentVersion.startsWith("v")) {
            currentVersion = "v" + currentVersion;
        }

        if (!latestVersion.startsWith("v")) {
            latestVersion = "v" + latestVersion;
        }

        plugin.getLogger().info("");
        plugin.getLogger().info("Current Version: '" + currentVersion + "', Latest Version: '" + latestVersion + "'");
        plugin.getLogger().info("Update the plugin here " + "https://www.spigotmc.org/resources/" + resourceId + "/");
        plugin.getLogger().info("");

        return !currentVersion.equals(latestVersion);
    }

    public String getReturnedVersion() {
        return latestVersion;
    }

    public String getUpdateLink() {
        return "https://www.spigotmc.org/resources/" + resourceId + "/";
    }
}

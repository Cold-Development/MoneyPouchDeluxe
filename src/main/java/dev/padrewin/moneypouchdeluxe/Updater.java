package dev.padrewin.moneypouchdeluxe;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Updater {

    private final JavaPlugin plugin;
    private final int resourceId;
    private String latestVersion;
    private boolean updateAvailable;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_LIGHT_BLUE = "\u001B[1;34m";
    private static final String ANSI_LIGHT_RED = "\u001B[1;31m";

    public Updater(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.latestVersion = plugin.getDescription().getVersion();
        this.updateAvailable = false;
    }

    public void checkForUpdateAndLog() {
        CompletableFuture.runAsync(() -> {
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

                        updateAvailable = !currentVersion.equalsIgnoreCase(latestVersion);

                        if (updateAvailable) {
                            Bukkit.getLogger().info("");
                            Bukkit.getLogger().info(ANSI_YELLOW + "[MoneyPouchDeluxe] " + ANSI_LIGHT_BLUE + "Checking for updates.." + ANSI_RESET);
                            Bukkit.getLogger().info(ANSI_YELLOW + "[MoneyPouchDeluxe] " + ANSI_RED + "Update available: " + ANSI_LIGHT_RED + latestVersion + " (You are running version " + currentVersion + ")" + ANSI_RESET);
                            Bukkit.getLogger().info(ANSI_YELLOW + "[MoneyPouchDeluxe] " + ANSI_GREEN + "You can download the latest version from here: " + getUpdateLink() + ANSI_RESET);
                            Bukkit.getLogger().info("");
                        } else {
                            Bukkit.getLogger().info("");
                            Bukkit.getLogger().info(ANSI_YELLOW + "[MoneyPouchDeluxe] " + ANSI_LIGHT_BLUE + "Checking for updates.." + ANSI_RESET);
                            Bukkit.getLogger().info(ANSI_YELLOW + "[MoneyPouchDeluxe] " + ANSI_GREEN + "No update available" + ANSI_RESET);
                            Bukkit.getLogger().info("");
                        }
                    }
                }
            } catch (IOException e) {
                Bukkit.getLogger().severe("[MoneyPouchDeluxe] Update check failed: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<Boolean> checkForUpdate() {
        return CompletableFuture.supplyAsync(() -> {
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

                        updateAvailable = !currentVersion.equalsIgnoreCase(latestVersion);
                    }
                }
            } catch (IOException e) {
                Bukkit.getLogger().severe("[MoneyPouchDeluxe] Update check failed: " + e.getMessage());
            }
            return updateAvailable;
        });
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getReturnedVersion() {
        return latestVersion;
    }

    public String getUpdateLink() {
        return "https://www.spigotmc.org/resources/" + resourceId + "/";
    }
}

package dev.padrewin.moneypouchdeluxe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CustomHeadManager {

    private static final boolean IS_LEGACY = isLegacyVersion();

    public static ItemStack getCustomSkull(String textureURL) {
        Bukkit.getLogger().info("[DEBUG] getCustomSkull() called with texture: " + textureURL);

        if (IS_LEGACY) {
            Bukkit.getLogger().info("[DEBUG] Using legacy version (1.13 - 1.19)");
            return CustomHeadLegacy.getCustomSkull(textureURL);
        } else {
            Bukkit.getLogger().info("[DEBUG] Using modern version (1.20+)");
            return CustomHeadPaper.getCustomSkull(textureURL);
        }
    }

    private static boolean isLegacyVersion() {
        String version = Bukkit.getBukkitVersion().split("-")[0]; // Ex: "1.16.5"
        String[] split = version.split("\\.");

        if (split.length >= 2) {
            int major = Integer.parseInt(split[1]); // Ex: "16" în "1.16.5"
            return major < 20; // Dacă este sub 1.20, folosește metoda legacy
        }
        return false;
    }
}

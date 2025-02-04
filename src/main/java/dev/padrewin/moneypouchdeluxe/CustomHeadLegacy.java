package dev.padrewin.moneypouchdeluxe;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class CustomHeadLegacy {

    public static ItemStack getCustomSkull(String textureURL) {
        Bukkit.getLogger().info("[DEBUG] Using Legacy method for custom head");

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        if (skullMeta == null) {
            Bukkit.getLogger().warning("[DEBUG] SkullMeta is null!");
            return skull;
        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", textureURL));

        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("[DEBUG] Error applying texture to skull!");
        }

        skull.setItemMeta(skullMeta);

        Bukkit.getLogger().info("[DEBUG] Custom head created successfully!");

        return skull;
    }
}

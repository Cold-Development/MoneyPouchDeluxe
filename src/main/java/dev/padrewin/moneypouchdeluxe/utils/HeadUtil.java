package dev.padrewin.moneypouchdeluxe.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Base64;
import java.util.UUID;

public class HeadUtil {

    public static ItemStack createCustomHead(String base64Texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", base64Texture));

        setProfile(skullMeta, profile);

        head.setItemMeta(skullMeta);
        return head;
    }

    private static void setProfile(SkullMeta skullMeta, GameProfile profile) {
        try {
            var profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String encodeTexture(String textureURL) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureURL + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes());
    }
}

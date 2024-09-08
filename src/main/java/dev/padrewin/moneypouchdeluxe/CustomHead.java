package dev.padrewin.moneypouchdeluxe;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public class CustomHead {

    public static ItemStack getCustomSkull(String textureURL) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        if (skullMeta == null) {
            return skull; // fallback in case of error
        }

        String shortName = UUID.randomUUID().toString().substring(0, 8);
        GameProfile profile = new GameProfile(UUID.randomUUID(), shortName);
        profile.getProperties().put("textures", new Property("textures", textureURL));

        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        skull.setItemMeta(skullMeta);
        return skull;
    }

    public static String getTextureValue(SkullMeta skullMeta) {
        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            GameProfile profile = (GameProfile) profileField.get(skullMeta);

            if (profile != null && profile.getProperties() != null) {
                for (Property property : profile.getProperties().get("textures")) {
                    Field valueField = Property.class.getDeclaredField("value");
                    valueField.setAccessible(true);
                    return (String) valueField.get(property);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}

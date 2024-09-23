package dev.padrewin.moneypouchdeluxe;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomHead {

    private static final Map<UUID, PlayerProfile> profileCache = new HashMap<>();

    public static ItemStack getCustomSkull(String textureURL) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        if (skullMeta == null) {
            return skull;
        }

        UUID uuid = UUID.randomUUID();
        PlayerProfile profile = profileCache.getOrDefault(uuid, Bukkit.createProfile(null, null));

        profile.setProperty(new ProfileProperty("textures", textureURL));
        profile.update();

        profileCache.put(uuid, profile);

        skullMeta.setPlayerProfile(profile);
        skull.setItemMeta(skullMeta);

        return skull;
    }

    public static String getTextureValue(SkullMeta skullMeta) {
        PlayerProfile profile = skullMeta.getPlayerProfile();
        if (profile != null) {
            for (ProfileProperty property : profile.getProperties()) {
                if ("textures".equals(property.getName())) {
                    return property.getValue();
                }
            }
        }
        return null;
    }
}

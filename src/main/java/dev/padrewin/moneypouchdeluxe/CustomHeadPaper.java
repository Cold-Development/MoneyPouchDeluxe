package dev.padrewin.moneypouchdeluxe;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class CustomHeadPaper {

    public static ItemStack getCustomSkull(String textureURL) {
        //Bukkit.getLogger().info("[DEBUG] Using Paper method for custom head");

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

        if (skullMeta == null) {
            //Bukkit.getLogger().warning("[DEBUG] SkullMeta is null!");
            return skull;
        }

        UUID uuid = UUID.randomUUID();
        PlayerProfile profile = Bukkit.createProfile(uuid, null);
        profile.setProperty(new ProfileProperty("textures", textureURL));

        skullMeta.setPlayerProfile(profile);
        skull.setItemMeta(skullMeta);

        //Bukkit.getLogger().info("[DEBUG] Custom head created successfully!");

        return skull;
    }
}

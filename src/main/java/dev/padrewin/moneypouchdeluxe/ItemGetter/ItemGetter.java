package dev.padrewin.moneypouchdeluxe.ItemGetter;

import dev.padrewin.moneypouchdeluxe.hook.NexoHook;
import dev.padrewin.moneypouchdeluxe.utils.Heads;
import dev.padrewin.moneypouchdeluxe.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Builds a pouch's item from its section in pouches.yml.
 * <p>
 * Supports: item (a material or {@code nexo:<id>}), name, lore, owner-username / owner-uuid /
 * owner-base64 (player heads), custommodeldata, unbreakable, itemflags and enchantments
 * ({@code namespace:name:level}).
 */
public class ItemGetter {

    public ItemStack getItem(String path, FileConfiguration config, JavaPlugin plugin) {
        String cName = config.getString(path + ".name", path + ".name");
        String cType = config.getString(path + ".item", "CHEST");

        // material, or a Nexo custom item written as "nexo:<id>"
        ItemStack is = null;
        if (cType.regionMatches(true, 0, "nexo:", 0, 5)) {
            is = NexoHook.buildItem(cType.substring(5));
            if (is == null) {
                plugin.getLogger().warning("Unrecognised Nexo item: " + cType + " (is Nexo installed and loaded?)");
            }
        }
        if (is == null) {
            Material type = Material.matchMaterial(cType);
            if (type == null || !type.isItem()) {
                if (!cType.regionMatches(true, 0, "nexo:", 0, 5)) {
                    plugin.getLogger().warning("Unrecognised material: " + cType);
                }
                type = Material.STONE;
            }
            is = new ItemStack(type);
        }
        ItemMeta ism = is.getItemMeta();

        // skull
        if (ism instanceof SkullMeta) {
            SkullMeta sm = (SkullMeta) ism;
            String cOwnerBase64 = config.getString(path + ".owner-base64");
            String cOwnerUsername = config.getString(path + ".owner-username");
            String cOwnerUuid = config.getString(path + ".owner-uuid");
            if (cOwnerUsername != null) {
                sm.setOwner(cOwnerUsername);
            } else if (cOwnerUuid != null) {
                try {
                    sm.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(cOwnerUuid)));
                } catch (IllegalArgumentException ignored) { }
            } else if (cOwnerBase64 != null && !Heads.applyTexture(sm, cOwnerBase64)) {
                plugin.getLogger().warning("Invalid owner-base64 head texture for " + path);
            }
        }

        Text.setLore(ism, config.getStringList(path + ".lore"));
        Text.setDisplayName(ism, cName);

        // custom model data
        if (config.contains(path + ".custommodeldata")) {
            ism.setCustomModelData(config.getInt(path + ".custommodeldata"));
        }

        // item flags
        for (String flag : config.getStringList(path + ".itemflags")) {
            try {
                ism.addItemFlags(ItemFlag.valueOf(flag.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unrecognised item flag: " + flag);
            }
        }

        // unbreakable
        ism.setUnbreakable(config.getBoolean(path + ".unbreakable", false));

        // enchantments
        for (String key : config.getStringList(path + ".enchantments")) {
            String[] split = key.split(":");
            if (split.length < 2) {
                plugin.getLogger().warning("Enchantment does not follow format {namespace}:{name}:{level} : " + key);
                continue;
            }

            Enchantment enchantment;
            try {
                enchantment = Enchantment.getByKey(new NamespacedKey(split[0].toLowerCase(Locale.ROOT), split[1].toLowerCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                enchantment = null;
            }
            if (enchantment == null) {
                plugin.getLogger().warning("Unrecognised enchantment: " + split[0] + ":" + split[1]);
                continue;
            }

            int level = 1;
            if (split.length >= 3) {
                try {
                    level = Integer.parseInt(split[2]);
                } catch (NumberFormatException ignored) { }
            }

            ism.addEnchant(enchantment, level, true);
        }

        is.setItemMeta(ism);
        return is;
    }
}

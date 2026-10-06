package dev.padrewin.moneypouchdeluxe.manager;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.config.CommentedFileConfiguration;
import dev.padrewin.colddev.locale.YamlFileLocale;
import dev.padrewin.colddev.manager.AbstractLocaleManager;
import dev.padrewin.moneypouchdeluxe.utils.ConfigFiles;
import dev.padrewin.moneypouchdeluxe.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.minecart.CommandMinecart;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Messages come from locale/&lt;locale&gt;.yml (en_US and ro_RO ship with the plugin). Messages added
 * in an update are written into those files automatically, without touching the ones an owner edited.
 */
public class LocaleManager extends AbstractLocaleManager {

    /**
     * Messages that used to be in config.yml under {@code messages:} and have another name now.
     * Every other key keeps its name.
     */
    private static final Map<String, String> RENAMED_CONFIG_MESSAGES = Map.ofEntries(
            Map.entry("no-permission", "pouch-no-permission"),
            Map.entry("no-permission-command", "no-permission"),
            Map.entry("full-inv", "command-give-full-inventory"),
            Map.entry("give-item", "command-give-success"),
            Map.entry("give-all", "command-give-all-success"),
            Map.entry("reloaded", "command-reload-reloaded"),
            Map.entry("list-header", "command-list-header"),
            Map.entry("list-entry", "command-list-entry"),
            Map.entry("list-empty", "command-list-empty"),
            Map.entry("economies-header", "command-economies-header"),
            Map.entry("economies-entry", "command-economies-entry"));

    public LocaleManager(ColdPlugin coldPlugin) {
        super(coldPlugin);
    }

    @Override
    public void reload() {
        super.reload();
        this.migrateConfigMessages();
    }

    /**
     * Older versions kept the messages in config.yml. The first time this version runs, they're moved
     * into the active locale file (so customised messages and the prefix are kept) and the section is
     * removed from config.yml, with a backup of the old config next to it.
     */
    private void migrateConfigMessages() {
        File configFile = new File(this.coldPlugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }
        ConfigurationSection messages = YamlConfiguration.loadConfiguration(configFile).getConfigurationSection("messages");
        if (messages == null) {
            return;
        }

        String localeName = this.coldPlugin.getColdConfig().get(SettingKey.LOCALE);
        File localeFile = new File(this.localeDirectory, localeName + ".yml");
        if (!localeFile.exists()) {
            localeFile = new File(this.localeDirectory, "en_US.yml");
        }

        try {
            CommentedFileConfiguration locale = CommentedFileConfiguration.loadConfiguration(localeFile);
            int moved = 0;
            for (String key : messages.getKeys(false)) {
                Object value = messages.get(key);
                if (!(value instanceof String)) {
                    continue; // the old help menu (a list) is now the help command
                }
                locale.set(RENAMED_CONFIG_MESSAGES.getOrDefault(key, key), value);
                moved++;
            }
            locale.save(localeFile);
            this.loadedLocale = new YamlFileLocale(localeFile);

            ConfigFiles.backup(configFile, ".before-locale");
            ConfigFiles.removeSection(configFile, "messages",
                    "The messages are in the locale folder now (see 'locale:' above).");
            this.coldPlugin.reloadConfig();

            this.coldPlugin.getLogger().info("Moved " + moved + " messages from config.yml to locale/" + localeFile.getName()
                    + " (old config saved as config.yml.before-locale).");
        } catch (IOException e) {
            this.coldPlugin.getLogger().severe("Failed to move the messages from config.yml to the locale folder: " + e.getMessage());
        }
    }

    /**
     * Sent through {@link Text#send} so Nexo glyphs render. Command blocks and minecarts must be
     * messaged from their own region thread on Folia.
     */
    @Override
    protected void handleMessage(CommandSender sender, String message) {
        if (sender instanceof BlockCommandSender && !Bukkit.isPrimaryThread()) {
            Location location = ((BlockCommandSender) sender).getBlock().getLocation();
            this.coldPlugin.getScheduler().runTaskAtLocation(location, () -> Text.send(sender, message));
        } else if (sender instanceof CommandMinecart && !Bukkit.isPrimaryThread()) {
            this.coldPlugin.getScheduler().runTaskAtEntity((CommandMinecart) sender, () -> Text.send(sender, message));
        } else {
            Text.send(sender, message);
        }
    }

}

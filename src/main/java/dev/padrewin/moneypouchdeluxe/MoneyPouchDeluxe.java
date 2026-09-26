package dev.padrewin.moneypouchdeluxe;

import dev.padrewin.colddev.utils.NMSUtil;
import dev.padrewin.moneypouchdeluxe.hook.NexoHook;
import dev.padrewin.moneypouchdeluxe.utils.Text;
import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.manager.Manager;
import dev.padrewin.colddev.manager.PluginUpdateManager;
import dev.padrewin.moneypouchdeluxe.Command.MoneyPouchDeluxeAdminCommand;
import dev.padrewin.moneypouchdeluxe.Command.MoneyPouchDeluxeBaseCommand;
import dev.padrewin.moneypouchdeluxe.EconomyType.*;
import dev.padrewin.moneypouchdeluxe.Listener.UseListenerLatest;
import dev.padrewin.moneypouchdeluxe.ItemGetter.ItemGetter;
import dev.padrewin.moneypouchdeluxe.ItemGetter.ItemGetterLatest;
import dev.padrewin.moneypouchdeluxe.Title.Title;
import dev.padrewin.moneypouchdeluxe.Title.Title_Bukkit;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class MoneyPouchDeluxe extends ColdPlugin {

    private final ArrayList<Pouch> pouches = new ArrayList<>();

    private final Map<String, EconomyType> economyTypes = new HashMap<>();

    private Title titleHandle;
    private ItemGetter itemGetter;
    private static MoneyPouchDeluxe instance;
    private YamlConfiguration pouchesConfig = new YamlConfiguration();

    public MoneyPouchDeluxe() {
        super("Cold-Development", "MoneyPouchDeluxe", 23381, null, null, null);
        instance = this;
        itemGetter = new ItemGetterLatest();
    }

    /**
     * Gets a registered {@link EconomyType} with a specified ID.
     *
     * @param id id of economy type
     * @return   {@link EconomyType} or null
     */
    public EconomyType getEconomyType(String id) {
        if (id == null) {
            return null;
        }
        return economyTypes.get(id.toLowerCase());
    }

    /**
     * Get all registered {@link EconomyType}.
     *
     * @return {@code Map<String, EconomyType>} of economy types - the key is the ID
     */
    public Map<String, EconomyType> getEconomyTypes() {
        return economyTypes;
    }

    /**
     * Registers an {@link EconomyType} with the plugin.
     * If the ID conflicts with an existing type, the registration will be ignored.
     *
     * @param id    id of the economy type
     * @param type  the economy type
     * @return      boolean if registered
     */
    public boolean registerEconomyType(String id, EconomyType type) {
        id = id.toLowerCase();
        if (economyTypes.containsKey(id)) {
            if (economyTypes.get(id).getClass().equals(type.getClass())) {
                return false;
            }
            super.getLogger().warning("Economy type registration " + type.toString() + " ignored due to conflicting ID '" + id + "' with economy type " + economyTypes.get(id).toString());
            return false;
        }
        economyTypes.put(id, type);
        //super.getLogger().info("Economy type '" + id + "' registered successfully: " + type.toString());
        return true;
    }

    public static MoneyPouchDeluxe getInstance() {
        return instance;
    }

    /**
     * Get a list of all pouches loaded
     *
     * @return {@code ArrayList<Pouch>}
     */
    public ArrayList<Pouch> getPouches() {
        return pouches;
    }

    @Override
    public void enable() {
        instance = this;
        saveDefaultConfig();

        setupEconomyTypes();

        getManager(PluginUpdateManager.class);

        String pluginName = getDescription().getName();
        getLogger().info("");
        getLogger().info("  ____ ___  _     ____  ");
        getLogger().info(" / ___/ _ \\| |   |  _ \\ ");
        getLogger().info("| |  | | | | |   | | | |");
        getLogger().info("| |__| |_| | |___| |_| |");
        getLogger().info(" \\____\\___/|_____|____/");
        getLogger().info("    " + pluginName + " v" + getDescription().getVersion());
        getLogger().info("    Author(s): " + getDescription().getAuthors().get(0));
        getLogger().info("    (c) Cold Development ❄");
        getLogger().info("");

        this.executeVersionSpecificActions();

        File directory = new File(String.valueOf(this.getDataFolder()));
        if (!directory.exists() && !directory.isDirectory()) {
            directory.mkdir();
        }

        File config = new File(this.getDataFolder() + File.separator + "config.yml");
        if (!config.exists()) {
            try {
                config.createNewFile();
                try (InputStream in = MoneyPouchDeluxe.class.getClassLoader().getResourceAsStream("config.yml")) {
                    OutputStream out = new FileOutputStream(config);
                    byte[] buffer = new byte[1024];
                    int length = in.read(buffer);
                    while (length != -1) {
                        out.write(buffer, 0, length);
                        length = in.read(buffer);
                    }
                } catch (IOException e) {
                    super.getLogger().severe("Failed to create config.");
                    e.printStackTrace();
                    super.getLogger().severe(ChatColor.RED + "...please delete the MoneyPouchDeluxe directory and try RESTARTING (not reloading).");
                }
            } catch (IOException e) {
                super.getLogger().severe("Failed to create config.");
                e.printStackTrace();
                super.getLogger().severe(ChatColor.RED + "...please delete the MoneyPouchDeluxe directory and try RESTARTING (not reloading).");
            }
        }

        File pouchDirectory = new File(this.getDataFolder() + File.separator + "customeconomytype");
        if (!pouchDirectory.exists() && !pouchDirectory.isDirectory()) {
            pouchDirectory.mkdir();

            ArrayList<String> examples = new ArrayList<>();
            examples.add("examplecustomeconomy.yml");
            examples.add("vault.yml");
            examples.add("playerpoints.yml");
            examples.add("README.txt");

            for (String name : examples) {
                File file = new File(this.getDataFolder() + File.separator + "customeconomytype" + File.separator + name);
                try {
                    file.createNewFile();
                    try (InputStream in = this.getResource("customeconomytype/" + name)) {
                        OutputStream out = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        assert in != null;
                        int lenght = in.read(buffer);
                        while (lenght != -1) {
                            out.write(buffer, 0, lenght);
                            lenght = in.read(buffer);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Objects.requireNonNull(getServer().getPluginCommand("moneypouch")).setExecutor(new MoneyPouchDeluxeBaseCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("moneypouchadmin")).setExecutor(new MoneyPouchDeluxeAdminCommand(this));

        NexoHook.registerItemsLoadedListener(this);

        // Defer the configuration-dependent load until all plugins have
        // completed enable(), so economy hooks can be discovered reliably.
        this.getScheduler().runTask(() -> this.reload());

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();
        reloadConfig();
    }

    /**
     * The only built-in economy is XP, since it's vanilla. Every other currency is a custom economy
     * from the customeconomytype folder, so the plugin never depends on an economy plugin.
     */
    private void setupEconomyTypes() {
        if (!economyTypes.containsKey("xp")) {
            registerEconomyType("xp", new XPEconomyType(
                    this.getConfig().getString("economy.xp.name", "XP"),
                    this.getConfig().getString("economy.xp.prefix", ""),
                    this.getConfig().getString("economy.xp.suffix", " XP")));
        }
    }

    @Override
    public void disable() {
        getLogger().info("MoneyPouchDeluxe has been disabled.");
    }

    @Override
    protected @NotNull List<Class<? extends Manager>> getManagerLoadPriority() {
        return List.of();
    }

    /**
     * A configured message with messages.prefix in front of it. A message set to "" is disabled:
     * it comes back empty (without the prefix) and {@link Text#send} skips it.
     */
    public String getMessage(Message message) {
        String text = this.getConfig().getString("messages." + message.getId(), message.getDef());
        if (text == null || text.isEmpty()) {
            return "";
        }
        return Text.color(this.getConfig().getString("messages.prefix", "") + text);
    }

    /**
     * The pouch's configured name for use in messages ({@code %item%}), coloured but with any
     * {@code <glyph:id>} tags kept, so they still render when the message is sent.
     */
    public String getPouchName(Pouch pouch) {
        String name = pouchesConfig.getString(pouch.getId() + ".name");
        if (name == null || name.isEmpty()) {
            return pouch.getItemStack().getItemMeta().getDisplayName();
        }
        return Text.color(name);
    }

    /**
     * Gives the item to the player. Whatever doesn't fit in their inventory is dropped at their feet,
     * so a pouch is never lost.
     *
     * @return true if at least part of it had to be dropped
     */
    public boolean giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack rest : leftover.values()) {
            Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), rest);
            dropped.setPickupDelay(40);
        }
        return !leftover.isEmpty();
    }

    public String getMessage(Message message, String playerName) {
        String msg = getMessage(message);
        if (playerName != null) {
            msg = msg.replace("%player%", playerName);
        }
        return msg;
    }

    public Title getTitleHandle() {
        return titleHandle;
    }


    private void executeVersionSpecificActions() {
        String version;
        itemGetter = new ItemGetterLatest();
        titleHandle = new Title_Bukkit();
        super.getServer().getPluginManager().registerEvents(new UseListenerLatest(this), this);

    }

    /**
     * pouches.yml: every pouch tier, keyed by its id (the part of config.yml that used to be pouches.tier).
     */
    public FileConfiguration getPouchesConfig() {
        return pouchesConfig;
    }

    private void loadPouchesConfig() {
        File file = new File(getDataFolder(), "pouches.yml");
        if (!file.exists()) {
            ConfigurationSection legacyTiers = getConfig().getConfigurationSection("pouches.tier");
            if (legacyTiers != null) {
                migratePouchesConfig(legacyTiers, file);
            } else {
                saveResource("pouches.yml", false);
            }
        }
        pouchesConfig = YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Pouches used to live in config.yml under pouches.tier: move them into pouches.yml, keeping
     * a copy of the old config.yml next to it just in case.
     */
    private void migratePouchesConfig(ConfigurationSection legacyTiers, File file) {
        YamlConfiguration migrated = new YamlConfiguration();
        copySection(legacyTiers, migrated);
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            Files.copy(configFile.toPath(), new File(getDataFolder(), "config.yml.before-pouches-yml").toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            migrated.save(file);

            getConfig().set("pouches.tier", null);
            saveConfig();
            getLogger().info("Moved " + legacyTiers.getKeys(false).size() + " pouches from config.yml to pouches.yml"
                    + " (old config saved as config.yml.before-pouches-yml).");
        } catch (IOException e) {
            getLogger().severe("Failed to move pouches from config.yml to pouches.yml: " + e.getMessage());
        }
    }

    private static void copySection(ConfigurationSection from, ConfigurationSection to) {
        for (String key : from.getKeys(false)) {
            if (from.isConfigurationSection(key)) {
                copySection(from.getConfigurationSection(key), to.createSection(key));
            } else {
                to.set(key, from.get(key));
            }
        }
    }

    public void reload() {
        super.reloadConfig();
        loadPouchesConfig();
        economyTypes.clear();
        setupEconomyTypes();

        ArrayList<String> custom = new ArrayList<>();
        for (Map.Entry<String, EconomyType> entry : economyTypes.entrySet()) {
            if (entry.getValue() instanceof CustomEconomyType) {
                custom.add(entry.getKey());
            }
        }
        for (String s : custom) {
            economyTypes.remove(s);
        }

        Path customEconomyPath = Paths.get(this.getDataFolder() + File.separator + "customeconomytype").toAbsolutePath();
        File customEconomyFolder = customEconomyPath.toFile();

        if (!customEconomyFolder.isDirectory()) {
            getLogger().warning("The customeconomytype folder is missing, so no custom economies were loaded."
                    + " Pouches using a custom economy will be skipped.");
        } else {
            try {
                Files.walkFileTree(customEconomyPath, new SimpleFileVisitor<Path>() {
                    final URI economyTypeRoot = customEconomyPath.toUri();

                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) {
                        File economyTypeFile = new File(path.toUri());
                        if (!economyTypeFile.getName().toLowerCase().endsWith(".yml")) return FileVisitResult.CONTINUE;

                        YamlConfiguration config = new YamlConfiguration();
                        try {
                            config.load(economyTypeFile);
                        } catch (Exception ex) {
                            getLogger().warning("Failed to load custom economy file: " + economyTypeFile.getName());
                            return FileVisitResult.CONTINUE;
                        }

                        String id = economyTypeFile.getName().replace(".yml", "");
                        if (!id.matches("[A-Za-z0-9]+")) {
                            getLogger().warning("Invalid economy ID: " + id + " (must be alphanumeric)");
                            return FileVisitResult.CONTINUE;
                        }

                        String command = config.getString("transaction-prize-command");
                        if (command == null) {
                            getLogger().warning("Missing 'transaction-prize-command' in file: " + economyTypeFile.getName());
                            return FileVisitResult.CONTINUE;
                        }

                        CustomEconomyType customEconomyType = new CustomEconomyType(
                                config.getString("name", getConfig().getString("economy." + id + ".name", id)),
                                config.getString("prefix", getConfig().getString("economy." + id + ".prefix", "")),
                                config.getString("suffix", getConfig().getString("economy." + id + ".suffix", "")),
                                command);

                        registerEconomyType(id, customEconomyType);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // A pouch whose economy isn't available (plugin missing, custom economy file deleted...) is
        // skipped on its own below; the others still load.
        pouches.clear();

        for (String pouchName : pouchesConfig.getKeys(false)) {
            String path = pouchName;

            String itemName = pouchesConfig.getString(path + ".name", "Unnamed Pouch");
            String itemType = pouchesConfig.getString(path + ".item", "CHEST");
            String textureURL = pouchesConfig.getString(path + ".texture-url", "");
            long priceMin = pouchesConfig.getLong(path + ".pricerange.from", 0);
            long priceMax = pouchesConfig.getLong(path + ".pricerange.to", 0);
            String economyTypeId = pouchesConfig.getString(path + ".options.economytype", "VAULT");
            List<String> lore = pouchesConfig.getStringList(path + ".lore");

            EconomyType economyType = getEconomyType(economyTypeId);
            if (economyType == null) {
                getLogger().warning("Skipping pouch '" + pouchName + "': economy type '" + economyTypeId + "' is missing"
                        + " (is its plugin installed, or does customeconomytype/" + economyTypeId.toLowerCase() + ".yml exist?)");
                continue;
            }

            ItemStack itemStack = getItemStack(path, pouchesConfig, itemName, lore);

            String permission = pouchesConfig.getString(path + ".options.permission-required", null);
            if (permission != null && (permission.isEmpty() || permission.equalsIgnoreCase("false"))) {
                permission = null; // "permission-required: false" means no permission
            }
            Pouch pouch = new Pouch(pouchName, priceMin, priceMax, itemStack, economyType, permission);
            pouch.initializeUUID();
            pouches.add(pouch);

        }

    }


    public ItemStack getItemStack(String path, FileConfiguration config, String itemName, List<String> lore) {
        ItemStack itemStack = itemGetter.getItem(path, config, this);

        if (itemStack != null && itemStack.getType() != Material.AIR) {
            ItemMeta meta = itemStack.getItemMeta();

            if (itemName != null && !itemName.isEmpty()) {
                Text.setDisplayName(meta, itemName);
            }

            long rangeFrom = config.getLong(path + ".pricerange.from");
            long rangeTo = config.getLong(path + ".pricerange.to");

            if (lore != null && !lore.isEmpty()) {
                List<String> rangedLore = new ArrayList<>();
                for (String line : lore) {
                    rangedLore.add(applyPriceRange(line, rangeFrom, rangeTo));
                }
                Text.setLore(meta, rangedLore);
            }

            itemStack.setItemMeta(meta);

            if (itemStack.getType() == Material.PLAYER_HEAD && config.contains(path + ".texture-url")) {
                String textureURL = config.getString(path + ".texture-url");
                //Bukkit.getLogger().info("[DEBUG] Applying texture for item at path: " + path + " | Texture: " + textureURL);

                ItemStack skull = CustomHeadManager.getCustomSkull(textureURL);

                if (skull.hasItemMeta() && skull.getItemMeta() instanceof SkullMeta) {
                    SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                    if (meta != null) {
                        Text.copyNameAndLore(meta, skullMeta);
                        skull.setItemMeta(skullMeta);
                    }
                }

                return skull;
            }
        }

        return itemStack;
    }

    private static String applyPriceRange(String line, long from, long to) {
        return line
                .replace("%pricerange_from%", String.format("%,d", from))
                .replace("%pricerange_to%", String.format("%,d", to));
    }

    public <T extends Manager> T getSpecificManager(Class<T> managerClass) {
        return getManager(managerClass);
    }

    public enum Message {

        FULL_INV("full-inv", "&c%player%'s inventory is full!"),
        PLAYER_FULL_INV("player-full-inv", "&cYour inventory is full. A pouch was dropped near you. Make sure to pick it up."),
        GIVE_ITEM("give-item", "&6Given &e%player% %item%&6."),
        RECEIVE_ITEM("receive-item", "&6You have been given %item%&6."),
        PRIZE_MESSAGE("prize-message", "&6You have received &c%prefix%%prize%%suffix%&6!"),
        ALREADY_OPENING("already-opening", "&cPlease wait for your current pouch opening to complete first!"),
        INVALID_POUCH("invalid-pouch", "&cThis pouch is invalid and cannot be opened."),
        REWARD_ERROR("reward-error", "&cYour reward of %prefix%%prize%%suffix% has failed to process. Contact an admin, this has been logged."),
        NO_PERMISSION("no-permission", "&cYou cannot open this pouch."),
        RELOADED("reloaded", "&fMoneyPouchDeluxe has been reloaded.");

        private String id;
        private String def; // (default message if undefined)

        Message(String id, String def) {
            this.id = id;
            this.def = def;
        }

        public String getId() {
            return id;
        }

        public String getDef() {
            return def;
        }
    }
}

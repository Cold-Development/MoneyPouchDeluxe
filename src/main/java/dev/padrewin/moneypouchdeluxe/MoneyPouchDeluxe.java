package dev.padrewin.moneypouchdeluxe;

import dev.padrewin.moneypouchdeluxe.hook.NexoHook;
import dev.padrewin.moneypouchdeluxe.utils.Heads;
import dev.padrewin.moneypouchdeluxe.utils.Text;
import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.manager.Manager;
import dev.padrewin.colddev.manager.PluginUpdateManager;
import dev.padrewin.moneypouchdeluxe.manager.CommandManager;
import dev.padrewin.moneypouchdeluxe.manager.DataManager;
import dev.padrewin.moneypouchdeluxe.hook.PouchPlaceholderExpansion;
import dev.padrewin.moneypouchdeluxe.manager.LocaleManager;
import dev.padrewin.moneypouchdeluxe.utils.ConfigFiles;
import dev.padrewin.colddev.config.ConfigUpdater;
import dev.padrewin.moneypouchdeluxe.EconomyType.*;
import dev.padrewin.moneypouchdeluxe.Listener.UseListener;
import dev.padrewin.moneypouchdeluxe.ItemGetter.ItemGetter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
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

    /**
     * Copied into customeconomytype/ the first time the folder is created.
     */
    private static final List<String> DEFAULT_ECONOMY_FILES = List.of(
            "examplecustomeconomy.yml", "vault.yml", "playerpoints.yml", "README.txt");

    private final ArrayList<Pouch> pouches = new ArrayList<>();

    private final Map<String, EconomyType> economyTypes = new HashMap<>();
    private final Map<String, EconomyType> externalEconomyTypes = new HashMap<>();

    private final ItemGetter itemGetter = new ItemGetter();
    private static MoneyPouchDeluxe instance;
    private TransactionLog transactionLog;
    private YamlConfiguration pouchesConfig = new YamlConfiguration();

    public MoneyPouchDeluxe() {
        super("Cold-Development", "MoneyPouchDeluxe", 23381, DataManager.class, LocaleManager.class, CommandManager.class);
        instance = this;
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

    /**
     * An economy added by another plugin through the API. Unlike the others it isn't read from a
     * file, so it's kept here and registered again on every reload.
     *
     * @return false if the id is already used by another economy
     */
    public boolean registerExternalEconomyType(String id, EconomyType type) {
        id = id.toLowerCase();
        EconomyType existing = externalEconomyTypes.get(id);
        if (existing != null && existing != type) {
            return false;
        }
        externalEconomyTypes.put(id, type);
        return registerEconomyType(id, type) || economyTypes.get(id) == type;
    }

    public TransactionLog getTransactionLog() {
        return transactionLog;
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

    /**
     * config.yml has to exist and be up to date before ColdDev loads it in onEnable (it adds the
     * 'locale' setting when missing): otherwise a first install would get a config.yml with only that
     * setting in it. Settings added by an update are written into the owner's files here, keeping
     * everything they changed (see {@link ConfigUpdater}).
     */
    @Override
    public void onLoad() {
        super.onLoad();
        saveDefaultConfig();
        updateFile("config.yml");

        // The economies that ship with the plugin (only if the owner still has them)
        File economyFolder = new File(getDataFolder(), "customeconomytype");
        if (economyFolder.isDirectory()) {
            for (String name : List.of("vault.yml", "playerpoints.yml")) {
                if (new File(economyFolder, name).exists()) {
                    updateFile("customeconomytype/" + name);
                }
            }
            if (new File(economyFolder, "README.txt").exists()) {
                saveResource("customeconomytype/README.txt", true);
            }
        }
    }

    /**
     * Adds the settings that are new in this version to one of the owner's files.
     *
     * @param path the file's path in the data folder, which is also its path in the jar
     */
    private void updateFile(String path) {
        try (InputStream defaults = getResource(path)) {
            if (defaults == null) {
                return;
            }
            List<String> added = ConfigUpdater.update(new File(getDataFolder(), path), defaults);
            if (!added.isEmpty()) {
                getLogger().info(path + ": added " + added.size() + " new setting(s): " + String.join(", ", added));
            }
        } catch (IOException e) {
            getLogger().warning("Could not add the new settings to " + path + ": " + e.getMessage());
        }
    }

    @Override
    public void enable() {
        instance = this;
        transactionLog = new TransactionLog(this);
        saveDefaultConfig();
        saveDefaultEconomyFiles();

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

        getServer().getPluginManager().registerEvents(new UseListener(this), this);

        NexoHook.registerItemsLoadedListener(this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PouchPlaceholderExpansion(this).register();
        }

        // Defer the configuration-dependent load until all plugins have
        // completed enable(), so economy hooks can be discovered reliably.
        this.getScheduler().runTask(this::reloadPouches);
    }

    /**
     * The example economies are only copied when the customeconomytype folder doesn't exist yet,
     * so files an owner deleted on purpose don't come back.
     */
    private void saveDefaultEconomyFiles() {
        if (new File(getDataFolder(), "customeconomytype").isDirectory()) {
            return;
        }
        for (String name : DEFAULT_ECONOMY_FILES) {
            saveResource("customeconomytype/" + name, false);
        }
    }

    /**
     * The only built-in economy is XP, since it's vanilla. Every other currency is a custom economy
     * from the customeconomytype folder (or registered by another plugin through the API), so the
     * plugin never depends on an economy plugin.
     */
    private void setupEconomyTypes() {
        if (!economyTypes.containsKey("xp")) {
            registerEconomyType("xp", new XPEconomyType(
                    this.getConfig().getString("economy.xp.name", "XP"),
                    this.getConfig().getString("economy.xp.prefix", ""),
                    this.getConfig().getString("economy.xp.suffix", " XP")));
        }
        externalEconomyTypes.forEach(this::registerEconomyType);
    }

    @Override
    public void disable() {
        if (transactionLog != null) {
            transactionLog.close();
        }
        getLogger().info("MoneyPouchDeluxe has been disabled.");
    }

    @Override
    protected @NotNull List<Class<? extends Manager>> getManagerLoadPriority() {
        return List.of();
    }

    /**
     * @return the loaded pouch with this id (case-insensitive), or null
     */
    public Pouch getPouch(String id) {
        for (Pouch pouch : pouches) {
            if (pouch.getId().equalsIgnoreCase(id)) {
                return pouch;
            }
        }
        return null;
    }

    /**
     * @return the id an economy is registered under (as used in pouches.yml)
     */
    public String getEconomyId(EconomyType economy) {
        for (Map.Entry<String, EconomyType> entry : economyTypes.entrySet()) {
            if (entry.getValue() == economy) {
                return entry.getKey();
            }
        }
        return economy.toString();
    }

    /**
     * Runs on the player's thread: right away if already on it, otherwise scheduled there. On Folia
     * a player's inventory may only be touched from their own region thread.
     */
    public void runAtPlayer(Player player, Runnable action) {
        if (getScheduler().isEntityThread(player)) {
            action.run();
        } else {
            getScheduler().runTaskAtEntity(player, action);
        }
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
     * Gives the item to the player, split into normal stacks so any amount ends up as regular items.
     * Whatever doesn't fit in their inventory is dropped at their feet, so a pouch is never lost.
     * Must run on the player's thread (see {@link #runAtPlayer}).
     *
     * @param amount how many of the item to give (the item's own amount is ignored)
     * @return true if at least part of it had to be dropped
     */
    public boolean giveOrDrop(Player player, ItemStack item, int amount) {
        int maxStack = Math.max(1, item.getMaxStackSize());
        List<ItemStack> stacks = new ArrayList<>();
        for (int remaining = amount; remaining > 0; remaining -= maxStack) {
            ItemStack stack = item.clone();
            stack.setAmount(Math.min(maxStack, remaining));
            stacks.add(stack);
        }

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(stacks.toArray(new ItemStack[0]));
        for (ItemStack rest : leftover.values()) {
            Item dropped = player.getWorld().dropItemNaturally(player.getLocation(), rest);
            dropped.setPickupDelay(40);
        }
        return !leftover.isEmpty();
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
     * a copy of the old config.yml next to it just in case. Only the server's own pouches end up in
     * pouches.yml, never the example ones. pouches.tier is removed from config.yml line by line, so
     * the rest of the file (comments included) stays as it was.
     */
    private void migratePouchesConfig(ConfigurationSection legacyTiers, File file) {
        YamlConfiguration migrated = new YamlConfiguration();
        copySection(legacyTiers, migrated);
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            ConfigFiles.backup(configFile, ".before-pouches-yml");
            migrated.save(file);

            ConfigFiles.removeSection(configFile, "pouches.tier", "The pouches are in pouches.yml now.");
            reloadConfig();
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

    /**
     * Reloads everything: config.yml, the locale files and commands (ColdDev managers), then the
     * economies and pouches.
     */
    @Override
    public void reload() {
        reload(this::reloadPouches);
    }

    /**
     * Reloads config.yml, pouches.yml and the custom economies, and rebuilds every pouch.
     */
    public void reloadPouches() {
        super.reloadConfig();
        loadPouchesConfig();
        transactionLog.configure(getConfig().getBoolean("transaction-log.enabled", true),
                getConfig().getInt("transaction-log.keep-days", 30));
        economyTypes.clear();
        setupEconomyTypes();

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

                        String name = config.getString("name", getConfig().getString("economy." + id + ".name", id));
                        String prefix = config.getString("prefix", getConfig().getString("economy." + id + ".prefix", ""));
                        String suffix = config.getString("suffix", getConfig().getString("economy." + id + ".suffix", ""));
                        String command = config.getString("transaction-prize-command", "");
                        String hook = config.getString("hook", "");

                        EconomyType economyType = loadEconomyType(economyTypeFile.getName(), hook, command, name, prefix, suffix);
                        if (economyType != null) {
                            registerEconomyType(id, economyType);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        registerBuiltInHook("vault", "&a$", "");
        registerBuiltInHook("playerpoints", "", " Points");

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
            pouches.add(pouch);

        }

    }


    /**
     * Vault and PlayerPoints work without a customeconomytype file, as long as their plugin is
     * installed: older versions had them built in, so servers updating from those have pouches
     * using "VAULT" but no vault.yml. A file with the same name always takes precedence; without
     * one, name/prefix/suffix come from economy.&lt;id&gt; in config.yml.
     */
    private void registerBuiltInHook(String id, String defaultPrefix, String defaultSuffix) {
        if (economyTypes.containsKey(id)) {
            return;
        }
        EconomyType hooked = EconomyHooks.create(id,
                getConfig().getString("economy." + id + ".name", ""),
                getConfig().getString("economy." + id + ".prefix", defaultPrefix),
                getConfig().getString("economy." + id + ".suffix", defaultSuffix));
        if (hooked != null) {
            registerEconomyType(id, hooked);
        }
    }

    /**
     * A customeconomytype file is paid through a hook ({@code hook: vault} / {@code playerpoints}),
     * which knows whether the transaction went through, or through {@code transaction-prize-command}.
     * The command is also the fallback when the hooked plugin isn't installed.
     *
     * @return the economy, or null (with a warning) if the file can't be used
     */
    private EconomyType loadEconomyType(String fileName, String hook, String command, String name, String prefix, String suffix) {
        hook = hook == null ? "" : hook.trim();
        boolean hasCommand = command != null && !command.isBlank();

        if (!hook.isEmpty() && !hook.equalsIgnoreCase("command")) {
            String requiredPlugin = EconomyHooks.requiredPlugin(hook);
            if (requiredPlugin == null) {
                getLogger().warning("Unknown hook '" + hook + "' in " + fileName + " (use vault, playerpoints or command).");
            } else {
                EconomyType hooked = EconomyHooks.create(hook, name, prefix, suffix);
                if (hooked != null) {
                    if (hook.equalsIgnoreCase("vault") && EconomyHooks.isVaultMissingEconomy()) {
                        getLogger().warning(fileName + ": Vault is installed but no economy plugin is registered with it,"
                                + " so payments will fail until one is.");
                    }
                    return hooked;
                }
                getLogger().warning(fileName + " hooks into " + requiredPlugin + ", which isn't installed"
                        + (hasCommand ? "; using transaction-prize-command instead." : "."));
            }
        }

        if (!hasCommand) {
            getLogger().warning("Skipping " + fileName + ": it has no working hook and no 'transaction-prize-command'.");
            return null;
        }
        return new CustomEconomyType(name, prefix, suffix, command);
    }

    public ItemStack getItemStack(String path, FileConfiguration config, String itemName, List<String> lore) {
        ItemStack itemStack = itemGetter.getItem(path, config, this);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return itemStack;
        }

        if (itemName != null && !itemName.isEmpty()) {
            Text.setDisplayName(meta, itemName);
        }

        if (lore != null && !lore.isEmpty()) {
            long rangeFrom = config.getLong(path + ".pricerange.from");
            long rangeTo = config.getLong(path + ".pricerange.to");
            List<String> rangedLore = new ArrayList<>();
            for (String line : lore) {
                rangedLore.add(applyPriceRange(line, rangeFrom, rangeTo));
            }
            Text.setLore(meta, rangedLore);
        }

        // Applied on the item's own meta, so custom model data, enchantments and flags are kept
        String texture = config.getString(path + ".texture-url", "");
        if (meta instanceof SkullMeta && texture != null && !texture.isBlank()
                && !Heads.applyTexture((SkullMeta) meta, texture)) {
            getLogger().warning("Invalid texture-url for pouch '" + path + "'. Use the Value (Base64), the texture URL"
                    + " or the texture hash from minecraft-heads.com.");
        }

        itemStack.setItemMeta(meta);
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
}

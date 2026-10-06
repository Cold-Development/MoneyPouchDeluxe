package dev.padrewin.moneypouchdeluxe;

import dev.padrewin.moneypouchdeluxe.hook.NexoHook;
import dev.padrewin.moneypouchdeluxe.utils.Heads;
import dev.padrewin.moneypouchdeluxe.utils.Text;
import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.manager.Manager;
import dev.padrewin.colddev.manager.PluginUpdateManager;
import dev.padrewin.moneypouchdeluxe.Command.MoneyPouchDeluxeAdminCommand;
import dev.padrewin.moneypouchdeluxe.Command.MoneyPouchDeluxeBaseCommand;
import dev.padrewin.moneypouchdeluxe.EconomyType.*;
import dev.padrewin.moneypouchdeluxe.Listener.UseListener;
import dev.padrewin.moneypouchdeluxe.ItemGetter.ItemGetter;
import org.bukkit.command.CommandSender;
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

    private final ItemGetter itemGetter = new ItemGetter();
    private static MoneyPouchDeluxe instance;
    private YamlConfiguration pouchesConfig = new YamlConfiguration();

    public MoneyPouchDeluxe() {
        super("Cold-Development", "MoneyPouchDeluxe", 23381, null, null, null);
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

        Objects.requireNonNull(getServer().getPluginCommand("moneypouch")).setExecutor(new MoneyPouchDeluxeBaseCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("moneypouchadmin")).setExecutor(new MoneyPouchDeluxeAdminCommand(this));

        NexoHook.registerItemsLoadedListener(this);

        // Defer the configuration-dependent load until all plugins have
        // completed enable(), so economy hooks can be discovered reliably.
        this.getScheduler().runTask(this::reload);
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
        String prefix = message.isPrefixed() ? this.getConfig().getString("messages.prefix", "") : "";
        return Text.color(prefix + text);
    }

    /**
     * {@link #getMessage(Message)} with placeholders filled in, given as pairs:
     * {@code getMessage(Message.GIVE_ITEM, "%player%", name, "%item%", item)}.
     */
    public String getMessage(Message message, String... placeholders) {
        String text = getMessage(message);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace(placeholders[i], placeholders[i + 1]);
        }
        return text;
    }

    /**
     * A multi-line message (a list in the config), coloured, without the prefix. Placeholders as pairs.
     */
    public List<String> getMessageList(String id, List<String> def, String... placeholders) {
        List<String> lines = this.getConfig().isList("messages." + id)
                ? this.getConfig().getStringList("messages." + id) : def;
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            for (int i = 0; i + 1 < placeholders.length; i += 2) {
                line = line.replace(placeholders[i], placeholders[i + 1]);
            }
            result.add(Text.color(line));
        }
        return result;
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

    public void sendHelp(CommandSender sender) {
        for (String line : getMessageList("help", DEFAULT_HELP, "%version%", getDescription().getVersion())) {
            Text.send(sender, line.isEmpty() ? " " : line);
        }
    }

    private static final List<String> DEFAULT_HELP = List.of(
            "&6&lMoneyPouchDeluxe &7v%version%",
            "&7<> = required, [] = optional",
            "&e/mp <pouch> [player|*] [amount] &8» &7give a pouch to a player, everyone (*) or yourself",
            "&e/mpa list &8» &7list all pouches",
            "&e/mpa economies &8» &7list all economies",
            "&e/mpa reload &8» &7reload the config");

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

    public enum Message {

        FULL_INV("full-inv", "&6%player%'s &finventory is &cfull&f. The pouch was dropped near the player."),
        PLAYER_FULL_INV("player-full-inv", "&fYour inventory is &cfull&f. A pouch was dropped near you. Make sure to pick it up."),
        GIVE_ITEM("give-item", "&fYou have given &6%player%&f %item%&f."),
        GIVE_ALL("give-all", "&fYou have given &6everyone&f %item%&f."),
        RECEIVE_ITEM("receive-item", "&fYou have received &6%item%&f."),
        PRIZE_MESSAGE("prize-message", "&fYou have received %prefix%%prize%%suffix%&f!"),
        ALREADY_OPENING("already-opening", "&fPlease wait until you open the first pouch!"),
        INVALID_POUCH("invalid-pouch", "&fThis pouch no longer exists! &7(contact an administrator)"),
        REWARD_ERROR("reward-error", "&fThe reward %prefix%%prize%%suffix% &fhas failed. &7(contact an administrator)"),
        NO_PERMISSION("no-permission", "&fYou do not have permission to open this pouch!"),
        NO_PERMISSION_COMMAND("no-permission-command", "&fYou do not have permission to do that!"),
        POUCH_NOT_FOUND("pouch-not-found", "&fThe pouch &c%pouch% &fdoes not exist."),
        PLAYER_NOT_FOUND("player-not-found", "&fThe player &c%player% &fis not online."),
        PLAYER_REQUIRED("player-required", "&fFrom the console you have to specify a player: &c/mp <pouch> <player> [amount]"),
        INVALID_AMOUNT("invalid-amount", "&c%amount% &fis not a valid amount. Use a whole number from &c1 &fto &c%max%&f."),
        RELOADED("reloaded", "&fMoneyPouchDeluxe has been reloaded."),
        LIST_HEADER("list-header", "&6&lPouches &7(%count%)", false),
        LIST_ENTRY("list-entry", " &8» &6%pouch% &7%min% - %max% &8| &7economy: &f%economy%%permission%", false),
        LIST_EMPTY("list-empty", " &8» &7No pouches are loaded. Check pouches.yml and the console.", false),
        ECONOMIES_HEADER("economies-header", "&6&lEconomies &7(%count%)", false),
        ECONOMIES_ENTRY("economies-entry", " &8» &6%id% &7%type% &8| &f%prefix%&7123&f%suffix%", false);

        private final String id;
        private final String def; // (default message if undefined)
        private final boolean prefixed;

        Message(String id, String def) {
            this(id, def, true);
        }

        Message(String id, String def, boolean prefixed) {
            this.id = id;
            this.def = def;
            this.prefixed = prefixed;
        }

        public String getId() {
            return id;
        }

        public String getDef() {
            return def;
        }

        /**
         * Whether messages.prefix goes in front. Lines of a list (pouches, economies) don't get it.
         */
        public boolean isPrefixed() {
            return prefixed;
        }
    }
}

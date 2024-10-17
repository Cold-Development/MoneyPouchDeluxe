package dev.padrewin.moneypouchdeluxe;

import dev.padrewin.colddev.ColdPlugin;
import dev.padrewin.colddev.manager.Manager;
import dev.padrewin.colddev.manager.PluginUpdateManager;
import dev.padrewin.moneypouchdeluxe.Command.MoneyPouchDeluxeAdminCommand;
import dev.padrewin.moneypouchdeluxe.Command.MoneyPouchDeluxeBaseCommand;
import dev.padrewin.moneypouchdeluxe.Command.MoneyPouchDeluxeShopCommand;
import dev.padrewin.moneypouchdeluxe.EconomyType.*;
import dev.padrewin.moneypouchdeluxe.Exception.HologramHandler;
import dev.padrewin.moneypouchdeluxe.Listener.ServerLoadListener;
import dev.padrewin.moneypouchdeluxe.Listener.UseListenerLatest;
import dev.padrewin.moneypouchdeluxe.Gui.MenuController;
import dev.padrewin.moneypouchdeluxe.ItemGetter.ItemGetter;
import dev.padrewin.moneypouchdeluxe.ItemGetter.ItemGetterLatest;
import dev.padrewin.moneypouchdeluxe.Title.Title;
import dev.padrewin.moneypouchdeluxe.Title.Title_Bukkit;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class MoneyPouchDeluxe extends ColdPlugin {

    private final ArrayList<Pouch> pouches = new ArrayList<>();

    private final Map<String, EconomyType> economyTypes = new HashMap<>();

    private Title titleHandle;
    private ItemGetter itemGetter;
    private MenuController menuController;
    private PlayerPointsAPI playerPointsAPI;
    private static MoneyPouchDeluxe instance;
    private boolean pointsSetupDone = false;
    private boolean pointsHooked = false;
    private boolean isVaultHooked = false;
    private boolean vaultHookLogged = false;

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

        setupEconomy();
        setupPointsEconomy();
        setupEconomyTypes();
        boolean hologramsEnabled = areHologramsEnabled();
        setHologramsEnabled(hologramsEnabled);

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

        boolean isStackerPluginPresent = isStackerPluginDetected();

        if (!isStackerPluginPresent) {
            new HologramHandler(this);

            NamespacedKey hologramKey = new NamespacedKey(this, "is_hologram");

            for (World world : Bukkit.getWorlds()) {
                for (ArmorStand armorStand : world.getEntitiesByClass(ArmorStand.class)) {
                    if (armorStand.getPersistentDataContainer().has(hologramKey, PersistentDataType.BYTE)) {
                        armorStand.remove();
                    }
                }
            }
        } else {
            getLogger().info("Stacker plugin detected. Holograms will not be created to avoid double holo");
        }

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

        menuController = new MenuController(this);

        HologramHandler hologramHandler = new HologramHandler(this);
        Objects.requireNonNull(getServer().getPluginCommand("moneypouch")).setExecutor(new MoneyPouchDeluxeBaseCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("moneypouchshop")).setExecutor(new MoneyPouchDeluxeShopCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("moneypouchadmin")).setExecutor(new MoneyPouchDeluxeAdminCommand(this, hologramHandler));

        super.getServer().getPluginManager().registerEvents(menuController, this);
        Bukkit.getScheduler().runTask(this, this::reload);

        setupPointsEconomy();

        getServer().getPluginManager().registerEvents(new ServerLoadListener(this), this);
        getServer().getScheduler().runTask(this, this::reload);
    }

    private void setupPointsEconomy() {
        if (pointsSetupDone) {
            return;
        }

        pointsHooked = false;

        if (Bukkit.getServer().getPluginManager().getPlugin("PremiumPoints") != null && Bukkit.getServer().getPluginManager().getPlugin("PremiumPoints").isEnabled()) {
            if (getEconomyType("playerpoints") == null) {
                try {
                    Class<?> premiumPointsClass = Class.forName("dev.padrewin.premiumpoints.PremiumPoints");
                    Object premiumPointsInstance = premiumPointsClass.getMethod("getInstance").invoke(null);
                    Object api = premiumPointsClass.getMethod("getAPI").invoke(premiumPointsInstance);

                    playerPointsAPI = (PlayerPointsAPI) api;
                    registerEconomyType("playerpoints", new PlayerPointsEconomyType(this,
                            this.getConfig().getString("economy.premiumpoints.prefix", ""),
                            this.getConfig().getString("economy.premiumpoints.suffix", " Points"))
                    );
                    getLogger().info("PremiumPoints hook successfully!");
                    pointsHooked = true;
                } catch (Exception e) {
                    //getLogger().severe("Failed to hook into PremiumPoints: " + e.getMessage());
                }
            }
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("ColdBits") != null && Bukkit.getServer().getPluginManager().getPlugin("ColdBits").isEnabled()) {
            if (getEconomyType("coldbits") == null) {
                try {
                    Class<?> coldBitsClass = Class.forName("dev.padrewin.coldbits.ColdBits");
                    Object coldBitsInstance = coldBitsClass.getMethod("getInstance").invoke(null);
                    Object api = coldBitsClass.getMethod("getAPI").invoke(coldBitsInstance);

                    playerPointsAPI = (PlayerPointsAPI) api;
                    registerEconomyType("coldbits", new PlayerPointsEconomyType(this,
                            this.getConfig().getString("economy.coldbits.prefix", ""),
                            this.getConfig().getString("economy.coldbits.suffix", " Bits"))
                    );
                    getLogger().info("ColdBits hook successfully!");
                    pointsHooked = true;
                } catch (Exception e) {
                    //getLogger().severe("Failed to hook into PremiumPoints: " + e.getMessage());
                }
            }
        }

        if (!pointsHooked && Bukkit.getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            if (getEconomyType("playerpoints") == null) {
                try {
                    Class<?> playerPointsClass = Class.forName("org.black_ixx.playerpoints.PlayerPoints");
                    Object playerPointsInstance = playerPointsClass.getMethod("getInstance").invoke(null);
                    Object api = playerPointsClass.getMethod("getAPI").invoke(playerPointsInstance);

                    playerPointsAPI = (PlayerPointsAPI) api;
                    registerEconomyType("playerpoints", new PlayerPointsEconomyType(this,
                            this.getConfig().getString("economy.playerpoints.prefix", ""),
                            this.getConfig().getString("economy.playerpoints.suffix", " Points"))
                    );
                    getLogger().info("PlayerPoints hook successfully!");
                    pointsHooked = true;
                } catch (Exception e) {
                    //getLogger().severe("Failed to hook into PlayerPoints: " + e.getMessage());
                }
            }
        }

        if (!pointsHooked) {
            getLogger().warning("Points support will be disabled.");
        }

        pointsSetupDone = true;
    }

    private void setupEconomyTypes() {
        if (!economyTypes.containsKey("invalid")) {
            registerEconomyType("invalid", new InvalidEconomyType());
        }

        if (!economyTypes.containsKey("xp")) {
            registerEconomyType("xp", new XPEconomyType(
                    this.getConfig().getString("economy.xp.prefix", this.getConfig().getString("economy.prefixes.xp", "")),
                    this.getConfig().getString("economy.xp.suffix", this.getConfig().getString("economy.suffixes.xp", " XP"))));
        }

        // Asigură-te că Vault este hook-uit înainte de a-l înregistra
        if (isVaultHooked && !economyTypes.containsKey("vault")) {
            registerEconomyType("vault", new VaultEconomyType(this,
                    this.getConfig().getString("economy.vault.prefix", this.getConfig().getString("economy.prefixes.vault", "$")),
                    this.getConfig().getString("economy.vault.suffix", this.getConfig().getString("economy.suffixes.vault", ""))));
        } else if (!isVaultHooked) {
            getLogger().warning("Vault plugin not hooked. Vault economy type will not be registered.");
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("LemonMobCoins") != null && !economyTypes.containsKey("lemonmobcoins")) {
            registerEconomyType("lemonmobcoins", new LemonMobCoinsEconomyType(this,
                    this.getConfig().getString("economy.lemonmobcoins.prefix", ""),
                    this.getConfig().getString("economy.lemonmobcoins.suffix", " Mob Coins")));
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

    private Economy econ = null;

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault plugin not found. Vault economy type will not be registered.");
            isVaultHooked = false; // Actualizează variabila de control
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().warning("No economy provider found for Vault.");
            isVaultHooked = false; // Actualizează variabila de control
            return false;
        }
        econ = rsp.getProvider();
        if (econ == null) {
            getLogger().warning("Vault economy provider is not available.");
            isVaultHooked = false; // Actualizează variabila de control
            return false;
        }

        // Logăm mesajul de hook doar dacă este hook-uit pentru prima dată
        if (!vaultHookLogged) {
            getLogger().info("Vault hook successfully!");
            vaultHookLogged = true;
        }

        // Setăm variabila de control doar dacă hook-ul a fost cu succes
        isVaultHooked = true;
        return true;
    }

    private boolean isStackerPluginDetected() {
        String[] stackerPlugins = {
                "RoseStacker",
                "WildStacker",
                "EpicStacker",
                "LagAssist",
                "SimpleStack",
                "StackMob",
                "Stacker"
        };

        for (String pluginName : stackerPlugins) {
            if (Bukkit.getPluginManager().getPlugin(pluginName) != null) {
                return true;
            }
        }
        return false;
    }

    public PlayerPointsAPI getPlayerPointsAPI() {
        return playerPointsAPI;
    }


    public String getMessage(Message message) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages."
                + message.getId(), message.getDef()));

    }

    public String getMessage(Message message, String playerName) {
        String msg = getMessage(message);
        if (playerName != null) {
            msg = msg.replace("%player%", playerName);
        }
        return msg;
    }

    public boolean areHologramsEnabled() {
        return getConfig().getBoolean("holograms.enabled", false);
    }

    public void setHologramsEnabled(boolean enabled) {
        getConfig().set("holograms.enabled", enabled);
        saveConfig();
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

    public MenuController getMenuController() {
        return menuController;
    }

    public void reload() {
        super.reloadConfig();

        boolean isEconomySetup = setupEconomy();
        setupEconomyTypes();
        setupPointsEconomy();


        if (!isVaultHooked && !economyTypes.containsKey("vault")) {
            getLogger().warning("Vault economy type not available. Pouches using 'vault' economy type will be ignored.");
        }

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

        if (!customEconomyFolder.exists() || !customEconomyFolder.isDirectory()) {
            return;
        }

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
                    if (!StringUtils.isAlphanumeric(id)) {
                        getLogger().warning("Invalid economy ID: " + id + " (must be alphanumeric)");
                        return FileVisitResult.CONTINUE;
                    }

                    String command = config.getString("transaction-prize-command");
                    if (command == null) {
                        getLogger().warning("Missing 'transaction-prize-command' in file: " + economyTypeFile.getName());
                        return FileVisitResult.CONTINUE;
                    }

                    CustomEconomyType customEconomyType = new CustomEconomyType(
                            getConfig().getString("economy." + id + ".prefix", ""),
                            getConfig().getString("economy." + id + ".suffix", ""),
                            command);

                    registerEconomyType(id, customEconomyType);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Verifică disponibilitatea economiei înainte de a încarca pouches-urile
        if (!isEconomySetup) {
            getLogger().warning("Skipping loading of pouches due to missing valid economy setup.");
            return;
        }

        // Încarcă pouches-urile numai după ce economia este setată corect
        pouches.clear();

        for (String pouchName : this.getConfig().getConfigurationSection("pouches.tier").getKeys(false)) {
            String path = "pouches.tier." + pouchName;

            String itemName = this.getConfig().getString(path + ".name", "Unnamed Pouch");
            String itemType = this.getConfig().getString(path + ".item", "CHEST");
            String textureURL = this.getConfig().getString(path + ".texture-url", "");
            long priceMin = this.getConfig().getLong(path + ".pricerange.from", 0);
            long priceMax = this.getConfig().getLong(path + ".pricerange.to", 0);
            String economyTypeId = this.getConfig().getString(path + ".options.economytype", "VAULT");
            boolean permissionRequired = this.getConfig().getBoolean(path + ".options.permission-required", false);
            List<String> lore = this.getConfig().getStringList(path + ".lore");

            EconomyType economyType = getEconomyType(economyTypeId);
            if (economyType == null) {
                getLogger().info("Ignoring pouch with ID " + pouchName + " due to invalid economy type '" + economyTypeId + "'.");
                continue;
            }

            ItemStack itemStack = getItemStack(path, this.getConfig(), itemName, lore);

            boolean purchasable = this.getConfig().contains("shop.purchasable-items." + pouchName);
            long price = 0;
            EconomyType purchaseEconomy = null;
            ItemStack shopIs = null;

            if (purchasable) {
                price = this.getConfig().getLong("shop.purchasable-items." + pouchName + ".price", 0);
                String purchaseEconomyId = this.getConfig().getString("shop.purchasable-items." + pouchName + ".currency", "VAULT");
                purchaseEconomy = getEconomyType(purchaseEconomyId);

                if (purchaseEconomy == null) {
                    purchaseEconomy = getEconomyType("invalid");
                    getLogger().warning("Pouch with ID " + pouchName + " tried to use an invalid currency (for /mpshop) economy type '" + purchaseEconomyId + "'.");
                }

                shopIs = itemStack.clone();
                ItemMeta shopIsm = shopIs.getItemMeta();
                List<String> shopIsLore = new ArrayList<>(lore);
                for (String shopLore : this.getConfig().getStringList("shop.append-to-lore")) {
                    shopIsLore.add(ChatColor.translateAlternateColorCodes('&', shopLore)
                            .replace("%price%", String.valueOf(price))
                            .replace("%prefix%", purchaseEconomy.getPrefix())
                            .replace("%suffix%", purchaseEconomy.getSuffix()));
                }
                shopIsm.setLore(shopIsLore);
                shopIs.setItemMeta(shopIsm);
            }

            Pouch pouch = new Pouch(pouchName, priceMin, priceMax, itemStack, economyType, permissionRequired, purchasable, purchaseEconomy, price, shopIs, pouchName);
            pouch.initializeUUID();
            pouches.add(pouch);
        }
    }


    public ItemStack getItemStack(String path, FileConfiguration config, String itemName, List<String> lore) {
        ItemStack itemStack = itemGetter.getItem(path, config, this);

        if (itemStack != null && itemStack.getType() != Material.AIR) {
            ItemMeta meta = itemStack.getItemMeta();

            if (itemName != null && !itemName.isEmpty()) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));
            }

            long rangeFrom = config.getLong(path + ".pricerange.from");
            long rangeTo = config.getLong(path + ".pricerange.to");

            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    line = line.replace("%pricerange_from%", String.format("%,d", rangeFrom));
                    line = line.replace("%pricerange_to%", String.format("%,d", rangeTo));
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }

            itemStack.setItemMeta(meta);

            if (itemStack.getType() == Material.PLAYER_HEAD && config.contains(path + ".texture-url")) {
                String textureURL = config.getString(path + ".texture-url");
                itemStack = CustomHead.getCustomSkull(textureURL);
                if (itemStack.hasItemMeta()) {
                    SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                    if (meta != null) {
                        skullMeta.setDisplayName(meta.getDisplayName());
                        skullMeta.setLore(meta.getLore());
                        itemStack.setItemMeta(skullMeta);
                    }
                }
            }
        }

        return itemStack;
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
        INVENTORY_FULL("inventory-full", "&cYour inventory is full."),
        REWARD_ERROR("reward-error", "&cYour reward of %prefix%%prize%%suffix% has failed to process. Contact an admin, this has been logged."),
        PURCHASE_SUCCESS("purchase-success", "&6You have purchased %item%&6 for &c%prefix%%price%%suffix%&6."),
        PURCHASE_FAIL("purchase-fail", "&cYou do not have &c%prefix%%price%%suffix%&6."),
        PURCHASE_ERROR("purchase-ERROR", "&cCould not complete transaction for %item%&c."),
        SHOP_DISABLED("shop-disabled", "&cThe pouch shop is disabled."),
        NO_PERMISSION("no-permission", "&cYou cannot open this pouch."),
        KILL_HOLO("kill-holo", "Pouch hologram removed.");

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
package dev.padrewin.moneypouchdeluxe;

import dev.padrewin.coldplugin.ColdPlugin;
import dev.padrewin.coldplugin.manager.Manager;
import dev.padrewin.coldplugin.manager.PluginUpdateManager;
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
import dev.padrewin.premiumpoints.PremiumPoints;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
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
        if (economyTypes.containsKey(id)) {
            super.getLogger().warning("Economy type registration " + type.toString() + " ignored due to conflicting ID '" + id + "' with economy type " + economyTypes.get(id).toString());
            return false;
        }
        economyTypes.put(id, type);
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
        setupEconomy();
        setupPointsEconomy();
        getManager(PluginUpdateManager.class);
        instance = this;
        menuController = new MenuController(this);
        super.getServer().getPluginManager().registerEvents(menuController, this);
        Objects.requireNonNull(this.getCommand("moneypouchadmin")).setExecutor(new MoneyPouchDeluxeAdminCommand(this));
        getServer().getPluginManager().registerEvents(new ServerLoadListener(this), this);

        if (getEconomyType("vault") == null && setupEconomy()) {
            registerEconomyType("vault", new VaultEconomyType(this,
                    this.getConfig().getString("economy.vault.prefix", "$"),
                    this.getConfig().getString("economy.vault.suffix", ""))
            );
            getLogger().info("Vault hook successfully!");
        } else if (getEconomyType("vault") == null) {
            getLogger().warning("Vault economy not found. Vault support will be disabled.");
        }

        Objects.requireNonNull(getServer().getPluginCommand("moneypouch")).setExecutor(new MoneyPouchDeluxeBaseCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("moneypouchshop")).setExecutor(new MoneyPouchDeluxeShopCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("moneypouchadmin")).setExecutor(new MoneyPouchDeluxeAdminCommand(this));

        String pluginName = getDescription().getName();
        getLogger().info("");
        getLogger().info("  ____ ___  _     ____  ");
        getLogger().info(" / ___/ _ \\| |   |  _ \\ ");
        getLogger().info("| |  | | | | |   | | | |");
        getLogger().info("| |__| |_| | |___| |_| |");
        getLogger().info(" \\____\\___/|_____|____/ ");
        getLogger().info("    " + pluginName + " v" + getDescription().getVersion());
        getLogger().info("    Author(s): " + getDescription().getAuthors().get(0));
        getLogger().info("    (c) Cold Development. All rights reserved.");
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

        // Setăm flag-ul pentru a preveni reapelarea setup-ului
        pointsSetupDone = true;
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
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
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
        setupPointsEconomy();

        // Verificăm și înregistrăm doar dacă nu există deja economia respectivă
        if (getEconomyType("invalid") == null) {
            registerEconomyType("invalid", new InvalidEconomyType());
        }

        if (getEconomyType("xp") == null) {
            registerEconomyType("xp", new XPEconomyType(
                    this.getConfig().getString("economy.xp.prefix", this.getConfig().getString("economy.prefixes.xp", "")),
                    this.getConfig().getString("economy.xp.suffix", this.getConfig().getString("economy.suffixes.xp", " XP"))
            ));
        }

        if (getEconomyType("lemonmobcoins") == null && Bukkit.getServer().getPluginManager().getPlugin("LemonMobCoins") != null) {
            registerEconomyType("lemonmobcoins", new LemonMobCoinsEconomyType(this,
                    this.getConfig().getString("economy.lemonmobcoins.prefix", ""),
                    this.getConfig().getString("economy.lemonmobcoins.suffix", " Mob Coins"))
            );
        }

        if (getEconomyType("vault") == null) {
            if (setupEconomy()) {
                registerEconomyType("vault", new VaultEconomyType(this,
                        this.getConfig().getString("economy.vault.prefix", "$"),
                        this.getConfig().getString("economy.vault.suffix", "")));
                getLogger().info("Vault hook successfully!");
            } else {
                getLogger().warning("Vault economy not found. Vault support will be disabled.");
            }
        }

        // Pouches reload
        pouches.clear();

        Set<String> invalidPouchesLogged = new HashSet<>();

        for (String pouchName : this.getConfig().getConfigurationSection("pouches.tier").getKeys(false)) {
            String path = "pouches.tier." + pouchName;

            String itemName = this.getConfig().getString(path + ".name", "Unnamed Pouch");
            String itemType = this.getConfig().getString(path + ".item", "CHEST");
            String textureURL = this.getConfig().getString(path + ".texture-url", "");
            long priceMin = this.getConfig().getLong(path + ".pricerange.from", 0);
            long priceMax = this.getConfig().getLong(path + ".pricerange.to", 0);
            String economyTypeId = this.getConfig().getString(path + ".options.economytype", "vault");
            boolean permissionRequired = this.getConfig().getBoolean(path + ".options.permission-required", false);
            List<String> lore = this.getConfig().getStringList(path + ".lore");

            String pouchId = pouchName;

            ItemStack itemStack = getItemStack(path, this.getConfig(), itemName, lore);

            EconomyType economyType = getEconomyType(economyTypeId.toLowerCase());
            if (economyType == null) {
                if (!invalidPouchesLogged.contains(pouchName)) {
                    economyType = getEconomyType("invalid");
                    super.getLogger().warning("Pouch with ID " + pouchName + " tried to use an invalid economy type '" + economyTypeId + "'.");
                    invalidPouchesLogged.add(pouchName);
                }
                continue;
            }

            boolean purchasable = this.getConfig().contains("shop.purchasable-items." + pouchName);
            long price = 0;
            EconomyType purchaseEconomy = null;
            ItemStack shopIs = null;

            if (purchasable) {
                price = this.getConfig().getLong("shop.purchasable-items." + pouchName + ".price", 0);
                String purchaseEconomyId = this.getConfig().getString("shop.purchasable-items." + pouchName + ".currency", "vault");
                purchaseEconomy = getEconomyType(purchaseEconomyId.toLowerCase());

                if (purchaseEconomy == null) {
                    purchaseEconomy = getEconomyType("invalid");
                    super.getLogger().warning("Pouch with ID " + pouchName + " tried to use an invalid currency economy type '" + purchaseEconomyId + "'.");
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

            Pouch pouch = new Pouch(pouchId, priceMin, priceMax, itemStack, economyType, permissionRequired, purchasable, purchaseEconomy, price, shopIs, pouchId);
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
        return getManager(managerClass);  // Apelăm metoda moștenită din ColdPlugin
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
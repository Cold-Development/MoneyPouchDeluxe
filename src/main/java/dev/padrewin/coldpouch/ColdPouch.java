package dev.padrewin.coldpouch;

import dev.padrewin.coldpouch.Command.ColdPouchAdminCommand;
import dev.padrewin.coldpouch.Command.ColdPouchBaseCommand;
import dev.padrewin.coldpouch.Command.ColdPouchShopCommand;
import dev.padrewin.coldpouch.EconomyType.*;
import dev.padrewin.coldpouch.Listener.UseListenerLatest;
import dev.padrewin.coldpouch.Gui.MenuController;
import dev.padrewin.coldpouch.ItemGetter.ItemGetter;
import dev.padrewin.coldpouch.ItemGetter.ItemGetterLatest;
import dev.padrewin.coldpouch.Title.Title;
import dev.padrewin.coldpouch.Title.Title_Bukkit;
import org.apache.commons.lang.StringUtils;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColdPouch extends JavaPlugin {
    private final ArrayList<Pouch> pouches = new ArrayList<>();

    private final Map<String, EconomyType> economyTypes = new HashMap<>();

    private Title titleHandle;
    private ItemGetter itemGetter;
    private MenuController menuController;
    private PlayerPointsAPI playerPointsAPI;

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

    /**
     * Get a list of all pouches loaded
     *
     * @return {@code ArrayList<Pouch>}
     */
    public ArrayList<Pouch> getPouches() {
        return pouches;
    }

    @Override
    public void onEnable() {

        String pluginName = getDescription().getName();
        getLogger().info("");
        getLogger().info("  ____ ___  _     ____  ");
        getLogger().info(" / ___/ _ \\| |   |  _ \\ ");
        getLogger().info("| |  | | | | |   | | | |");
        getLogger().info("| |__| |_| | |___| |_| |");
        getLogger().info(" \\____\\___/|_____|____/");
        getLogger().info("    " + pluginName + " v" + getDescription().getVersion());
        getLogger().info("    Author(s): " + getDescription().getAuthors().get(0));
        getLogger().info("    (c) Cold Development. All rights reserved.");
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
                try (InputStream in = ColdPouch.class.getClassLoader().getResourceAsStream("config.yml")) {
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
                    super.getLogger().severe(ChatColor.RED + "...please delete the ColdPouch directory and try RESTARTING (not reloading).");
                }
            } catch (IOException e) {
                super.getLogger().severe("Failed to create config.");
                e.printStackTrace();
                super.getLogger().severe(ChatColor.RED + "...please delete the ColdPouch directory and try RESTARTING (not reloading).");
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

        registerEconomyType("invalid", new InvalidEconomyType());
        registerEconomyType("xp", new XPEconomyType(           // vv for legacy purposes
                this.getConfig().getString("economy.xp.prefix", this.getConfig().getString("economy.prefixes.xp", "")),
                this.getConfig().getString("economy.xp.suffix", this.getConfig().getString("economy.suffixes.xp", " XP"))));


        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            registerEconomyType("vault", new VaultEconomyType(this,
                    this.getConfig().getString("economy.vault.prefix", this.getConfig().getString("economy.prefixes.vault", "$")),
                    this.getConfig().getString("economy.vault.suffix", this.getConfig().getString("economy.suffixes.vault", ""))));
        }

        if (Bukkit.getServer().getPluginManager().getPlugin("LemonMobCoins") != null) {
            registerEconomyType("lemonmobcoins", new LemonMobCoinsEconomyType(this,
                    this.getConfig().getString("economy.lemonmobcoins.prefix", ""),
                    this.getConfig().getString("economy.lemonmobcoins.suffix", " Mob Coins")));
        }

        super.getServer().getPluginCommand("ColdPouch").setExecutor(new ColdPouchBaseCommand(this));
        super.getServer().getPluginCommand("ColdPouchShop").setExecutor(new ColdPouchShopCommand(this));
        super.getServer().getPluginCommand("ColdPouchAdmin").setExecutor(new ColdPouchAdminCommand(this));

        super.getServer().getPluginManager().registerEvents(menuController, this);
        Bukkit.getScheduler().runTask(this, this::reload);

        if (getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            playerPointsAPI = PlayerPoints.getInstance().getAPI();
            registerEconomyType("playerpoints", new PlayerPointsEconomyType(this,
                    this.getConfig().getString("economy.playerpoints.prefix", ""),
                    this.getConfig().getString("economy.playerpoints.suffix", " Points")));
            getLogger().info("PlayerPoints found and hooked!");
        } else {
            getLogger().warning("PlayerPoints not found. PlayerPoints support will be disabled.");
        }

    }

    public PlayerPointsAPI getPlayerPointsAPI() {
        return playerPointsAPI;
    }


    public String getMessage(Message message) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages."
                + message.getId(), message.getDef()));
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

        ArrayList<String> custom = new ArrayList<>();
        for (Map.Entry<String, EconomyType> entry : economyTypes.entrySet()) {
            if (entry.getValue() instanceof CustomEconomyType) {
                custom.add(entry.getKey());
            }
        }
        for (String s : custom) {
            economyTypes.remove(s);
        }

        FileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
            final URI economyTypeRoot = Paths.get(ColdPouch.this.getDataFolder() + File.separator + "customeconomytype").toUri();

            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) {
                File economyTypeFile = new File(path.toUri());
                if (!economyTypeFile.getName().toLowerCase().endsWith(".yml")) return FileVisitResult.CONTINUE;

                YamlConfiguration config = new YamlConfiguration();
                // test MP file integrity
                try {
                    config.load(economyTypeFile);
                } catch (Exception ex) {
                    return FileVisitResult.CONTINUE;
                }

                String id = economyTypeFile.getName().replace(".yml", "");

                if (!StringUtils.isAlphanumeric(id)) {
                    return FileVisitResult.CONTINUE;
                }

                String command = config.getString("transaction-prize-command");

                if (command == null) command = "";

                CustomEconomyType customEconomyType = new CustomEconomyType(
                        ColdPouch.this.getConfig().getString("economy." + id + ".prefix", ""),
                        ColdPouch.this.getConfig().getString("economy." + id + ".suffix", ""),
                        command);

                registerEconomyType(id, customEconomyType);
                return FileVisitResult.CONTINUE;
            }
        };

        try {
            Files.walkFileTree(Paths.get(this.getDataFolder() + File.separator + "customeconomytype"), fileVisitor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        pouches.clear();
        for (String s : this.getConfig().getConfigurationSection("pouches.tier").getKeys(false)) {
            String path = "pouches.tier." + s;

            String itemName = this.getConfig().getString(path + ".name", "Unnamed Pouch");
            String itemType = this.getConfig().getString(path + ".item", "CHEST");
            String textureURL = this.getConfig().getString(path + ".texture-url", "");
            long priceMin = this.getConfig().getLong(path + ".pricerange.from", 0);
            long priceMax = this.getConfig().getLong(path + ".pricerange.to", 0);
            String economyTypeId = this.getConfig().getString(path + ".options.economytype", "VAULT");
            boolean permissionRequired = this.getConfig().getBoolean(path + ".options.permission-required", false);
            List<String> lore = this.getConfig().getStringList(path + ".lore");

            ItemStack itemStack;
            if (itemType.equalsIgnoreCase("PLAYER_HEAD") && !textureURL.isEmpty()) {
                itemStack = CustomHead.getCustomSkull(textureURL);
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));
                    List<String> formattedLore = new ArrayList<>();
                    for (String line : lore) {
                        formattedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                    itemMeta.setLore(formattedLore);
                    itemStack.setItemMeta(itemMeta);
                }
            } else {
                itemStack = getItemStack(path, this.getConfig(), itemName, lore);
            }

            EconomyType economyType = getEconomyType(economyTypeId);
            if (economyType == null) {
                economyType = getEconomyType("invalid");
                super.getLogger().warning("Pouch with ID " + s + " tried to use an invalid economy type '" + economyTypeId + "'.");
            }

            boolean purchasable = this.getConfig().contains("shop.purchasable-items." + s);
            if (purchasable) {
                long price = this.getConfig().getLong("shop.purchasable-items." + s + ".price", 0);
                String purchaseEconomyId = this.getConfig().getString("shop.purchasable-items." + s + ".currency", "VAULT");
                EconomyType purchaseEconomy = getEconomyType(purchaseEconomyId);

                if (purchaseEconomy == null) {
                    purchaseEconomy = getEconomyType("invalid");
                    super.getLogger().warning("Pouch with ID " + s + " tried to use an invalid currency (for /mpshop) economy type '" + purchaseEconomyId + "'.");
                }

                ItemStack shopIs = itemStack.clone();
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

                pouches.add(new Pouch(s.replace(" ", "_"), priceMin, priceMax, itemStack, economyType, permissionRequired, purchasable, purchaseEconomy, price, shopIs));
            } else {
                pouches.add(new Pouch(s.replace(" ", "_"), priceMin, priceMax, itemStack, economyType, permissionRequired));
            }
        }
    }

    public ItemStack getItemStack(String path, FileConfiguration config, String itemName, List<String> lore) {
        ItemStack itemStack = itemGetter.getItem(path, config, this);

        if (itemStack != null && itemStack.getType() != Material.AIR) {
            ItemMeta meta = itemStack.getItemMeta();

            if (itemName != null && !itemName.isEmpty()) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemName));
            }

            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
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

    public enum Message {

        FULL_INV("full-inv", "&c%player%'s inventory is full!"),
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
        NO_PERMISSION("no-permission", "&cYou cannot open this pouch.");

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

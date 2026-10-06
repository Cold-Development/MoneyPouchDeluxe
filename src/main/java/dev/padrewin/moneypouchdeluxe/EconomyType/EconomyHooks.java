package dev.padrewin.moneypouchdeluxe.EconomyType;

import org.bukkit.Bukkit;

import java.util.Locale;

/**
 * Creates the economies that hook straight into another plugin, picked with {@code hook:} in a
 * customeconomytype file. The hooked classes are only touched once their plugin is known to be
 * enabled, so MoneyPouchDeluxe runs fine without Vault or PlayerPoints installed.
 */
public final class EconomyHooks {

    private EconomyHooks() {
    }

    /**
     * @return the plugin a hook needs, or null if the hook name isn't known
     */
    public static String requiredPlugin(String hook) {
        switch (hook.toLowerCase(Locale.ROOT)) {
            case "vault":
                return "Vault";
            case "playerpoints":
                return "PlayerPoints";
            default:
                return null;
        }
    }

    public static boolean isAvailable(String hook) {
        String plugin = requiredPlugin(hook);
        return plugin != null && Bukkit.getPluginManager().isPluginEnabled(plugin);
    }

    /**
     * @return the hooked economy, or null if the hook isn't known or its plugin isn't enabled
     */
    public static EconomyType create(String hook, String name, String prefix, String suffix) {
        if (!isAvailable(hook)) {
            return null;
        }
        switch (hook.toLowerCase(Locale.ROOT)) {
            case "vault":
                return new VaultEconomyType(name, prefix, suffix);
            case "playerpoints":
                return new PlayerPointsEconomyType(name, prefix, suffix);
            default:
                return null;
        }
    }

    /**
     * Vault is installed but nothing has registered an economy with it, so deposits would fail.
     */
    public static boolean isVaultMissingEconomy() {
        return isAvailable("vault") && VaultEconomyType.getEconomy() == null;
    }

}

package dev.padrewin.moneypouchdeluxe.EconomyType;

import dev.padrewin.colddev.scheduler.ColdScheduler;
import dev.padrewin.moneypouchdeluxe.MoneyPouchDeluxe;
import dev.padrewin.moneypouchdeluxe.Listener.UseListener;
import dev.padrewin.moneypouchdeluxe.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CustomEconomyType extends EconomyType {

    private final String command;

    public CustomEconomyType(String name, String prefix, String suffix, String command) {
        super(name, prefix, suffix);
        this.command = command;
    }

    @Override
    public void processPayment(Player player, long amount) {
        String resolved = command.replace("%player%", player.getName()).replace("%prize%", String.valueOf(amount));
        MoneyPouchDeluxe plugin = MoneyPouchDeluxe.getInstance();
        // Console commands run on the global region thread on Folia
        ColdScheduler.getInstance(plugin).executeGlobal(() -> {
            boolean ran;
            try {
                ran = Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), resolved);
            } catch (Exception e) {
                ran = false;
            }
            if (!ran) {
                // The command doesn't exist (its plugin missing?) or threw: nothing was given, so leave a trace
                // an admin can use to reward the player by hand
                plugin.getLogger().severe("Custom economy command failed, " + player.getName() + " did not receive "
                        + amount + ". Command: /" + resolved);
                if (player.isOnline()) {
                    Text.send(player, applyPlaceholders(plugin.getMessage(MoneyPouchDeluxe.Message.REWARD_ERROR),
                            UseListener.formatNumber(amount, plugin.getConfig().getString("pouches.title.format.separator", ","))));
                }
            }
        });
    }


    @Override
    public String toString() {
        return "Custom (/" + command + ")";
    }

}

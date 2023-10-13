package me.lockie.coopersmpwinter;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WinterCommand implements CommandExecutor {

    private final CooperSMPWinter plugin;

    public WinterCommand(CooperSMPWinter plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("winter")) {
            String usageMessage = "§cUsage: /winter <reload|removeMainHandItem|giveSnowShovel>";
            String noPermissionMessage = "§cYou do not have permission to use this command!";
            if (args.length == 0) {
                sender.sendMessage(usageMessage);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "reload":
                    if (sender.hasPermission("winter.reload")) {
                        plugin.reloadConfig();
                        sender.sendMessage(String.format("§a%s reloaded", plugin.getName()));
                    } else {
                        sender.sendMessage(noPermissionMessage);
                    }
                    return true;

                case "removemainhanditem":
                    if (sender.hasPermission("winter.removeMainHandItem") && sender instanceof Player player) {
                        player.getInventory().getItemInMainHand().setAmount(0);
                        sender.sendMessage("§aRemoved main hand item");
                    } else {
                        sender.sendMessage(noPermissionMessage);
                    }
                    return true;

                case "givesnowshovel":
                    if (sender.hasPermission("winter.giveSnowShovel") && sender instanceof Player player) {
                        player.getInventory().addItem(CustomItemManager.snowShovel);
                        sender.sendMessage("§aGave snow shovel");
                    } else {
                        sender.sendMessage(noPermissionMessage);
                    }
                    return true;
            }

            sender.sendMessage(usageMessage);
            return true;
        }
        return false;
    }
}

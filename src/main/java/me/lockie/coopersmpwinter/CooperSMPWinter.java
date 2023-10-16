package me.lockie.coopersmpwinter;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import io.papermc.lib.PaperLib;
import me.lockie.coopersmpwinter.items.CustomItem;
import me.lockie.coopersmpwinter.items.CustomItemManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;

public class CooperSMPWinter extends JavaPlugin {

    private CustomItemManager customItemManager;

    private void loadCommands() {
        new CommandAPICommand("winter")
                .executes((sender, args) -> {
                    sender.sendMessage("§cUsage: /winter <reload|removeMainHandItem|giveSnowShovel>");
                })
                .withSubcommands(
                        new CommandAPICommand("reload")
                                .withPermission("winter.reload")
                                .executes((sender, args) -> {
                                    this.reloadConfig();
                                    sender.sendMessage("§aReloaded config");
                                }),
                        new CommandAPICommand("removeMainHandItem")
                                .withPermission("winter.removeMainHandItem")
                                .executesPlayer((player, args) -> {
                                    player.getInventory().setItemInMainHand(null);
                                    player.sendMessage("§aRemoved item in main hand");
                                }),
                        new CommandAPICommand("give")
                                .withPermission("winter.give")
                                .withArguments(new StringArgument("item").replaceSuggestions(ArgumentSuggestions.strings(this.customItemManager.customItems.stream().map(CustomItem::getId).collect(Collectors.toList()))))
                                .executesPlayer((player, args) -> {
                                    final String itemId = (String) args.get(0);
                                    player.getInventory().addItem(this.customItemManager.getCustomItemById(itemId));
                                    player.sendMessage("§aGiven items");
                                })
                )
                .register();
    }

    @Override
    public void onLoad() {
        if (!CommandAPI.isLoaded())
            CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);
        CommandAPI.onEnable();

        this.getLogger().info(String.format("%s Enabled", this.getName()));
        saveDefaultConfig();

        final CustomItemManager customItemManager = new CustomItemManager();

        final PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new WinterEventListener(this), this);

        customItemManager.registerCustomItems();
        this.customItemManager = customItemManager;

        this.loadCommands();
    }

    @Override
    public void onDisable() {
        CommandAPI.unregister("winter");
        this.getLogger().info(String.format("%s Disabled", this.getName()));
    }
}

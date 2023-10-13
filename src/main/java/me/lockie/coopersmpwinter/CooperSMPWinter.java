package me.lockie.coopersmpwinter;

import com.comphenix.protocol.ProtocolLibrary;
import io.papermc.lib.PaperLib;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class CooperSMPWinter extends JavaPlugin {

    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);

        this.getLogger().info(String.format("%s Enabled", this.getName()));
        saveDefaultConfig();

        ProtocolLibrary.getProtocolManager().addPacketListener(new EnchantmentGlowHider(this));

        final CustomItemManager customItemManager = new CustomItemManager(this);

        final PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new SnowballListener(this), this);
        pluginManager.registerEvents(new WinterEventListener(), this);

        PluginCommand command = Objects.requireNonNull(this.getCommand("winter"));
        command.setExecutor(new WinterCommand(this));
        command.setTabCompleter(new WinterTabCompletor());

        customItemManager.registerCustomItems();
    }

    @Override
    public void onDisable() {
        this.getLogger().info(String.format("%s Disabled", this.getName()));
    }
}

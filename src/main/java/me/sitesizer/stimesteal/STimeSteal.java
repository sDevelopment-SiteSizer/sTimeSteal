package me.sitesizer.stimesteal;

import org.bukkit.plugin.java.JavaPlugin;

import me.sitesizer.stimesteal.listeners.DeathListener;
import me.sitesizer.stimesteal.listeners.JoinListener;
import me.sitesizer.stimesteal.tasks.TimeDecrementor;

public class STimeSteal extends JavaPlugin {

    private TimeManager timeManager;
    private ItemManager itemManager; // Keep reference if needed

    @Override
    public void onEnable() {
        // Save Default Config
        saveDefaultConfig();
        saveResource("recipes.yml", false);

        // Initialize Managers
        this.timeManager = new TimeManager(this);
        this.itemManager = new ItemManager(this);

        // Register Commands
        getCommand("withdraw").setExecutor(new me.sitesizer.stimesteal.commands.WithdrawCommand(this, timeManager, itemManager));
        getCommand("stimesteal").setExecutor(new me.sitesizer.stimesteal.commands.AdminTimeCommand(this, timeManager, itemManager));
        getCommand("paytime").setExecutor(new me.sitesizer.stimesteal.commands.PayTimeCommand(this, timeManager));

        // Register Listeners
        getServer().getPluginManager().registerEvents(new JoinListener(timeManager), this);
        getServer().getPluginManager().registerEvents(new DeathListener(timeManager), this);
        getServer().getPluginManager().registerEvents(new me.sitesizer.stimesteal.listeners.VoucherListener(this, timeManager, itemManager), this);
        getServer().getPluginManager().registerEvents(new me.sitesizer.stimesteal.listeners.ReviveBeaconListener(this, timeManager, itemManager), this);

        // Citizens Hook
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getLogger().info("Citizens found! Enabling NPC features.");
            me.sitesizer.stimesteal.gui.ShopGUI shopGUI = new me.sitesizer.stimesteal.gui.ShopGUI(this, timeManager);
            getServer().getPluginManager().registerEvents(new me.sitesizer.stimesteal.listeners.CitizensListener(this, shopGUI), this);
        }

        // Start Tasks
        new TimeDecrementor(timeManager).runTaskTimer(this, 20L, 20L); // Every second (20 ticks)

        getLogger().info("sTimeSteal has been enabled! The clock is ticking...");
    }

    @Override
    public void onDisable() {
        getLogger().info("sTimeSteal has been disabled.");
    }
}

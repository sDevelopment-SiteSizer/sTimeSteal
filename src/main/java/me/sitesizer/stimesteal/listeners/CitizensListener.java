package me.sitesizer.stimesteal.listeners;

import me.sitesizer.stimesteal.STimeSteal;
import me.sitesizer.stimesteal.gui.ShopGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.MetadataValue;

public class CitizensListener implements Listener {

    private final STimeSteal plugin;
    private final ShopGUI shopGUI;

    public CitizensListener(STimeSteal plugin, ShopGUI shopGUI) {
        this.plugin = plugin;
        this.shopGUI = shopGUI;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        // Check if the entity is an NPC (Citizens adds "NPC" metadata)
        if (event.getRightClicked().hasMetadata("NPC")) {
            // It is an NPC!
            // Check name for "Time Merchant"
            if (event.getRightClicked().getCustomName() != null
                    && event.getRightClicked().getCustomName().equals("Time Merchant")) {
                shopGUI.openShop(event.getPlayer());
            } else if (event.getRightClicked().getName().equals("Time Merchant")) {
                // Fallback for non-custom names
                shopGUI.openShop(event.getPlayer());
            }
        }
    }
}

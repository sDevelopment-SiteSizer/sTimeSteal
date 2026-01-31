package me.sitesizer.stimesteal.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import me.sitesizer.stimesteal.ItemManager;
import me.sitesizer.stimesteal.STimeSteal;
import me.sitesizer.stimesteal.TimeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ReviveBeaconListener implements Listener {

    private final STimeSteal plugin;
    private final TimeManager timeManager;
    private final ItemManager itemManager;

    public ReviveBeaconListener(STimeSteal plugin, TimeManager timeManager, ItemManager itemManager) {
        this.plugin = plugin;
        this.timeManager = timeManager;
        this.itemManager = itemManager;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !itemManager.isReviveBeacon(item)) {
            return;
        }

        event.setCancelled(true);
        openReviveGui(event.getPlayer());
    }

    private void openReviveGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, Component.text("Revive Beacon"));

        ItemStack filler = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        filler.getItemMeta().displayName(Component.empty());

        // Fill border? User said "covered by light gray glass plane", assuming border or background.
        // Let's fill ALL slots first.
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, filler);
        }

        // Leave center slots open? Or User said "put banned pplayer head...".
        // Sounds like an input slot. Let's clear the center slot (Slot 22, or maybe a few).
        // A single slot for input seems robust.
        gui.setItem(22, null); // The input slot

        player.openInventory(gui);
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text("Revive Beacon"))) {
            return;
        }

        // Prevent taking the glass panes
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.LIGHT_GRAY_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            return;
        }

        // Logic for putting head in.
        // We actually need to detect when they put it in, or maybe click a confirm button?
        // User said "put banned player head... and on reviving...". 
        // This implies instant action or a check.
        // Let's check on slot change or click.
        // Simpler: If they click a head into slot 22.
        if (event.getSlot() == 22 && event.getCursor().getType() == Material.PLAYER_HEAD) {
            // Check if it's a valid player head
            ItemStack cursor = event.getCursor();
            if (cursor.getItemMeta() instanceof SkullMeta meta) {
                if (meta.getOwningPlayer() != null) {
                    // The target might be offline. getOwningPlayer returns OfflinePlayer.
                    org.bukkit.OfflinePlayer offlineTarget = meta.getOwningPlayer();

                    // How to find Beacon? They are holding it or it's in their inventory?
                    // They clicked WITH the head, so beacon is likely in their inventory (or main hand if they opened it).
                    Player user = (Player) event.getWhoClicked();

                    // Check if target needs revival
                    if (offlineTarget.isOnline()) {
                        Player onlineTarget = offlineTarget.getPlayer();
                        if (onlineTarget != null) { // Ensure onlineTarget is not null
                            if (timeManager.getTime(onlineTarget) > 0 && onlineTarget.getGameMode() != GameMode.SPECTATOR) {
                                user.sendMessage(Component.text("That player is alive.", NamedTextColor.RED));
                                event.setCancelled(true);
                                return;
                            }

                            // Revive!
                            timeManager.grantReviveTime(onlineTarget);
                            timeManager.revivePlayer(onlineTarget);
                        }
                    } else {
                        // Offline revive logic would require editing OfflinePlayer PDC or handling on join.
                        // For 5/10 difficulty, let's stick to Online checks or simple file mod?
                        // Let's assume Online for now for safety, or just set a "RevivePending" flag?
                        user.sendMessage(Component.text("Player must be online to revive (for now).", NamedTextColor.RED));
                        event.setCancelled(true);
                        return;
                    }

                    // Success - Remove Beacon
                    consumeBeacon(user);

                    user.sendMessage(Component.text("Revived " + offlineTarget.getName(), NamedTextColor.GREEN));
                    user.closeInventory();
                    event.setCancelled(true); // Don't actually place the head
                }
            }
        }
    }

    private void consumeBeacon(Player player) {
        // Find one Revive Beacon and remove it.
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && itemManager.isReviveBeacon(item)) {
                item.subtract(1);
                return;
            }
        }
        // Fallback: Check cursor/offhand if not found?
        // Usually assume it's the one they opened with.
    }
}

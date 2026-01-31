package me.sitesizer.stimesteal.gui;

import me.sitesizer.stimesteal.STimeSteal;
import me.sitesizer.stimesteal.TimeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ShopGUI implements Listener {

    private final STimeSteal plugin;
    private final TimeManager timeManager;
    private final Component guiTitle = Component.text("Time Shop", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD);

    public ShopGUI(STimeSteal plugin, TimeManager timeManager) {
        this.plugin = plugin;
        this.timeManager = timeManager;
    }

    public void openShop(Player player) {
        Inventory shop = Bukkit.createInventory(null, 27, guiTitle);

        // Example Item: Buy 1 Hour for 1 Diamond (Placeholder economy logic)
        // For now, let's say you can Trade Hearts for Time or Vice Versa if we had a heart system.
        // Or simply withdraw time items.
        // Let's add a "Withdraw 1 Hour" item
        shop.setItem(11, createGuiItem(Material.CLOCK,
                Component.text("Withdraw 1 Hour", NamedTextColor.GREEN),
                Component.text("Cost: 60 Minutes", NamedTextColor.GRAY)));

        // Let's add a "Buy Heart" (Max Health) item - simplistic implementation
        shop.setItem(15, createGuiItem(Material.RED_DYE,
                Component.text("Buy Heart", NamedTextColor.RED),
                Component.text("Cost: 10 Hours", NamedTextColor.RED),
                Component.text("(Requires 600 Minutes)", NamedTextColor.GRAY)));

        // Fill background
        ItemStack filler = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Component.text(" "));
        for (int i = 0; i < shop.getSize(); i++) {
            if (shop.getItem(i) == null) {
                shop.setItem(i, filler);
            }
        }

        player.openInventory(shop);
    }

    private ItemStack createGuiItem(Material material, Component name, Component... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(guiTitle)) {
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        if (clickedItem.getType() == Material.CLOCK) {
            // Withdraw 1 Hour
            // 60 minutes
            if (timeManager.getTime(player) > 3600) {
                // Logic to withdraw handled by command usually, but here we can just deduct and give item
                // Integrating with existing logic would be best, but for now:
                player.performCommand("withdraw 60");
                player.sendMessage(Component.text("Withdrawn 1 Hour!", NamedTextColor.GREEN));
                player.closeInventory();
            } else {
                player.sendMessage(Component.text("Not enough time!", NamedTextColor.RED));
            }
        } else if (clickedItem.getType() == Material.RED_DYE) {
            // Buy Heart - Cost 600 minutes
            long costSeconds = 600 * 60;
            if (timeManager.getTime(player) >= costSeconds) {
                timeManager.modifyTime(player, -costSeconds);

                // Add Max Health
                double currentMax = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(currentMax + 2.0);

                player.sendMessage(Component.text("You bought a heart!", NamedTextColor.RED));
                player.closeInventory();
            } else {
                player.sendMessage(Component.text("Not enough time!", NamedTextColor.RED));
            }
        }
    }
}

package de.allinone.firma.gui;

import de.allinone.firma.FirmaPlugin;
import de.allinone.firma.data.Firma;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class UpgradeGui {

    public static final String TITLE = ChatColor.GREEN + "" + ChatColor.BOLD + "Firma upgraden";

    public static void open(Player player, FirmaPlugin plugin, Firma firma) {
        Inventory inv = Bukkit.createInventory(player, 27, TITLE);

        inv.setItem(11, namedItem(Material.EMERALD, ChatColor.AQUA + "Ressourcen-Level (" + firma.getResourceLevel() + "/100)", List.of(
                ChatColor.GRAY + "Erhoeht Ertrag pro Mitarbeiter",
                ChatColor.YELLOW + "Kosten: " + FirmaGui.formatMoney(plugin, cost("resource", firma.getResourceLevel())),
                ChatColor.GREEN + "Linksklick = 1 Level",
                ChatColor.GREEN + "Rechtsklick = 15 Level",
                ChatColor.GREEN + "Shift-Rechtsklick = 100 Level"
        )));

        inv.setItem(15, namedItem(Material.VILLAGER_SPAWN_EGG, ChatColor.LIGHT_PURPLE + "Mitarbeiter-Slots (" + firma.getEmployeeSlotLevel() + "/100)", List.of(
                ChatColor.GRAY + "Max. Mitarbeiter: " + firma.getMaxEmployees(),
                ChatColor.YELLOW + "Kosten: " + FirmaGui.formatMoney(plugin, cost("slots", firma.getEmployeeSlotLevel())),
                ChatColor.GREEN + "Linksklick = 1 Level",
                ChatColor.GREEN + "Rechtsklick = 15 Level",
                ChatColor.GREEN + "Shift-Rechtsklick = 100 Level"
        )));

        inv.setItem(22, namedItem(Material.ARROW, ChatColor.YELLOW + "Zurueck", List.of()));

        player.openInventory(inv);
    }

    public static double cost(String type, int currentLevel) {
        double base = type.equals("resource") ? 15000 : 20000;
        return base * Math.pow(1.07, currentLevel);
    }

    private static ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}

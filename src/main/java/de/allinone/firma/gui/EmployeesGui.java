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

public class EmployeesGui {

    public static final String TITLE = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Digitale Mitarbeiter";
    public static final double HIRE_COST = 25000;

    public static void open(Player player, FirmaPlugin plugin, Firma firma) {
        Inventory inv = Bukkit.createInventory(player, 27, TITLE);

        inv.setItem(4, namedItem(Material.NETHER_STAR, ChatColor.AQUA + "Aktiv: " + firma.getDigitalEmployees() + "/" + firma.getMaxEmployees(), List.of(
                ChatColor.GRAY + "Ertrag: " + ChatColor.GREEN + FirmaGui.formatMoney(plugin, firma.getIncomePerCycle()) + " / 60s"
        )));

        inv.setItem(11, namedItem(Material.LIME_DYE, ChatColor.GREEN + "1 Mitarbeiter anstellen", List.of(
                ChatColor.GRAY + "Kosten: " + FirmaGui.formatMoney(plugin, HIRE_COST),
                ChatColor.YELLOW + "Klick zum Anstellen"
        )));

        inv.setItem(13, namedItem(Material.RED_DYE, ChatColor.RED + "1 Mitarbeiter entlassen", List.of(
                ChatColor.YELLOW + "Klick zum Entlassen"
        )));

        inv.setItem(22, namedItem(Material.ARROW, ChatColor.YELLOW + "Zurueck", List.of()));

        player.openInventory(inv);
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

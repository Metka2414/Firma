package de.allinone.firma.gui;

import de.allinone.firma.FirmaPlugin;
import de.allinone.firma.data.Firma;
import de.allinone.firma.data.FirmaRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BankGui {

    public static final String TITLE = ChatColor.GOLD + "" + ChatColor.BOLD + "Firmenbank";

    public static void open(Player player, FirmaPlugin plugin, Firma firma) {
        Inventory inv = Bukkit.createInventory(player, 27, TITLE);

        inv.setItem(4, item(Material.GOLD_BLOCK, ChatColor.GOLD + "Guthaben: " + FirmaGui.formatMoney(plugin, firma.getBankBalance()), List.of()));

        inv.setItem(11, item(Material.LIME_DYE, ChatColor.GREEN + "Einzahlen", List.of(
                ChatColor.GRAY + "Jeder in der Firma kann einzahlen.",
                ChatColor.YELLOW + "Klick, dann Betrag im Chat eingeben"
        )));

        boolean canWithdraw = firma.getRole(player.getUniqueId()) == FirmaRole.OWNER;
        inv.setItem(15, item(Material.RED_DYE,
                canWithdraw ? ChatColor.RED + "Auszahlen" : ChatColor.DARK_GRAY + "Auszahlen (keine Rechte)",
                List.of(
                        canWithdraw ? ChatColor.GRAY + "Nur Eigentuemer/Berechtigte." : ChatColor.RED + "Du hast keine Auszahlungs-Rechte.",
                        canWithdraw ? ChatColor.YELLOW + "Klick, dann Betrag im Chat eingeben" : ""
                )));

        inv.setItem(22, item(Material.ARROW, ChatColor.YELLOW + "Zurueck", List.of()));

        player.openInventory(inv);
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}

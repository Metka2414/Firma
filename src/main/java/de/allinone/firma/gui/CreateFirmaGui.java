package de.allinone.firma.gui;

import de.allinone.firma.data.FirmaType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import java.util.List;

public class CreateFirmaGui {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(player, 27, FirmaGui.CREATE_TITLE);

        inv.setItem(10, item(Material.REDSTONE, ChatColor.RED + "IT-Firma"));
        inv.setItem(11, item(Material.IRON_PICKAXE, ChatColor.GRAY + "Mine-Firma"));
        inv.setItem(12, item(Material.BRICKS, ChatColor.GOLD + "Bau-Firma"));
        inv.setItem(14, item(Material.OAK_DOOR, ChatColor.GREEN + "Immobilien-Firma"));
        inv.setItem(15, item(Material.CHEST, ChatColor.YELLOW + "Sonstige Firma"));

        player.openInventory(inv);
    }

    public static FirmaType typeForSlot(int slot) {
        return switch (slot) {
            case 10 -> FirmaType.IT;
            case 11 -> FirmaType.MINE;
            case 12 -> FirmaType.BAU;
            case 14 -> FirmaType.IMMOBILIEN;
            case 15 -> FirmaType.SONSTIGES;
            default -> null;
        };
    }

    private static ItemStack item(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(List.of(ChatColor.YELLOW + "Klick zum Auswaehlen"));
        item.setItemMeta(meta);
        return item;
    }
}

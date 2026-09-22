package de.allinone.firma.gui;

import de.allinone.firma.FirmaPlugin;
import de.allinone.firma.data.Firma;
import de.allinone.firma.data.FirmaRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MembersGui {

    public static final String TITLE = ChatColor.AQUA + "" + ChatColor.BOLD + "Mitglieder";

    public static void open(Player player, FirmaPlugin plugin, Firma firma) {
        int size = Math.max(27, (((firma.getMembers().size() - 1) / 9) + 1) * 9 + 18);
        Inventory inv = Bukkit.createInventory(player, size, TITLE);

        boolean isOwner = firma.getRole(player.getUniqueId()) == FirmaRole.OWNER;

        int slot = 0;
        for (UUID memberId : firma.getMembers().keySet()) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(memberId);
            FirmaRole memberRole = firma.getMembers().get(memberId);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(offline);
            meta.setDisplayName((memberRole == FirmaRole.OWNER ? ChatColor.GOLD : ChatColor.WHITE) + String.valueOf(offline.getName()));

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Rolle: " + ChatColor.WHITE + (memberRole == FirmaRole.OWNER ? "Eigentuemer" : "Angestellter"));
            if (isOwner && !memberId.equals(player.getUniqueId())) {
                lore.add("");
                lore.add(ChatColor.YELLOW + "Linksklick = Rolle wechseln");
                lore.add(ChatColor.RED + "Rechtsklick = Kicken");
            }
            meta.setLore(lore);
            head.setItemMeta(meta);

            inv.setItem(slot, head);
            slot++;
        }

        int bottomRow = size - 9;
        if (isOwner) {
            inv.setItem(bottomRow + 2, namedItem(Material.EMERALD, ChatColor.GREEN + "Spieler einladen", List.of(
                    ChatColor.YELLOW + "Klick, dann Spielername im Chat eingeben"
            )));
        }
        inv.setItem(bottomRow + 6, namedItem(Material.ARROW, ChatColor.YELLOW + "Zurueck", List.of()));

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

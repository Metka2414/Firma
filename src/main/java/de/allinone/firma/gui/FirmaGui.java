package de.allinone.firma.gui;

import de.allinone.firma.FirmaPlugin;
import de.allinone.firma.data.Firma;
import de.allinone.firma.data.FirmaRole;
import de.allinone.firma.data.FirmaType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class FirmaGui {

    public static final String MAIN_TITLE = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Firma-Menue";
    public static final String CREATE_TITLE = ChatColor.GREEN + "" + ChatColor.BOLD + "Firma erstellen";
    public static final String DASHBOARD_PREFIX = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Firma: ";

    public static void openMain(Player player, FirmaPlugin plugin) {
        Firma firma = plugin.getFirmaManager().getFirmaOf(player.getUniqueId());

        if (firma != null) {
            openDashboard(player, plugin, firma);
            return;
        }

        Inventory inv = Bukkit.createInventory(player, 27, MAIN_TITLE);

        inv.setItem(13, namedItem(Material.ANVIL, ChatColor.GREEN + "" + ChatColor.BOLD + "Firma gruenden", List.of(
                ChatColor.GRAY + "Kosten: " + ChatColor.WHITE + FirmaGui.formatMoney(plugin, 100000),
                ChatColor.YELLOW + "Klick zum Gruenden"
        )));

        player.openInventory(inv);
    }

    public static void openDashboard(Player player, FirmaPlugin plugin, Firma firma) {
        plugin.getFirmaManager().setOpenFirma(player.getUniqueId(), firma.getId());
        Inventory inv = build(player, plugin, firma);
        player.openInventory(inv);
    }

    public static void refreshDashboard(Player player, FirmaPlugin plugin, Firma firma) {
        var openInv = player.getOpenInventory();
        if (openInv == null || !openInv.getTitle().equals(titleFor(firma))) {
            openDashboard(player, plugin, firma);
            return;
        }
        Inventory fresh = build(player, plugin, firma);
        Inventory top = openInv.getTopInventory();
        for (int i = 0; i < fresh.getSize(); i++) {
            top.setItem(i, fresh.getItem(i));
        }
    }

    public static String titleFor(Firma firma) {
        return DASHBOARD_PREFIX + firma.getName();
    }

    private static Inventory build(Player player, FirmaPlugin plugin, Firma firma) {
        Inventory inv = Bukkit.createInventory(player, 45, titleFor(firma));

        FirmaRole role = firma.getRole(player.getUniqueId());
        boolean isOwner = role == FirmaRole.OWNER;

        inv.setItem(4, namedItem(Material.NETHER_STAR, ChatColor.AQUA + "" + ChatColor.BOLD + firma.getName(), List.of(
                ChatColor.GRAY + "Bereich: " + ChatColor.WHITE + firma.getType().name(),
                ChatColor.GRAY + "Mitglieder: " + ChatColor.WHITE + firma.getMembers().size(),
                ChatColor.GRAY + "Bank: " + ChatColor.GOLD + formatMoney(plugin, firma.getBankBalance()),
                ChatColor.GRAY + "Deine Rolle: " + ChatColor.WHITE + (isOwner ? "Eigentuemer" : "Angestellter")
        )));

        inv.setItem(10, namedItem(Material.GOLD_INGOT, ChatColor.GOLD + "Firmenbank", List.of(
                ChatColor.GRAY + "Guthaben: " + ChatColor.GOLD + formatMoney(plugin, firma.getBankBalance()),
                ChatColor.YELLOW + "Klick zum Oeffnen"
        )));

        inv.setItem(12, namedItem(Material.PLAYER_HEAD, ChatColor.AQUA + "Mitglieder", List.of(
                ChatColor.GRAY + "" + firma.getMembers().size() + " Mitglieder",
                ChatColor.YELLOW + "Klick zum Verwalten"
        )));

        inv.setItem(14, namedItem(Material.VILLAGER_SPAWN_EGG, ChatColor.LIGHT_PURPLE + "Digitale Mitarbeiter", List.of(
                ChatColor.GRAY + "Aktiv: " + firma.getDigitalEmployees() + "/" + firma.getMaxEmployees(),
                ChatColor.GRAY + "Ertrag: " + ChatColor.GREEN + formatMoney(plugin, firma.getIncomePerCycle()) + " / 60s",
                ChatColor.YELLOW + "Klick zum Verwalten"
        )));

        inv.setItem(16, namedItem(Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Firma upgraden", List.of(
                ChatColor.GRAY + "Ressourcen-Level: " + firma.getResourceLevel() + "/100",
                ChatColor.GRAY + "Mitarbeiter-Slots-Level: " + firma.getEmployeeSlotLevel() + "/100",
                ChatColor.YELLOW + "Klick zum Verbessern"
        )));

        if (firma.getType() == FirmaType.IMMOBILIEN) {
            inv.setItem(28, namedItem(Material.OAK_DOOR, ChatColor.YELLOW + "Immobilien verwalten", List.of(
                    ChatColor.GRAY + "" + firma.getProperties().size() + " Angebote",
                    ChatColor.YELLOW + "Klick zum Verwalten"
            )));
        }

        if (firma.getType() == FirmaType.BAU) {
            long open = firma.getBuildRequests().stream().filter(r -> !r.isCompleted()).count();
            inv.setItem(28, namedItem(Material.BRICKS, ChatColor.YELLOW + "Bauauftraege", List.of(
                    ChatColor.GRAY + "" + open + " offene Auftraege",
                    ChatColor.YELLOW + "Klick zum Verwalten"
            )));
        }

        inv.setItem(30, namedItem(Material.EMERALD, ChatColor.GREEN + "Firma verkaufen", List.of(
                firma.isForSale()
                        ? ChatColor.GRAY + "Zum Verkauf fuer: " + formatMoney(plugin, firma.getSalePrice())
                        : ChatColor.GRAY + "Aktuell nicht zum Verkauf",
                ChatColor.YELLOW + "Klick zum Einstellen/Bearbeiten"
        )));

        inv.setItem(32, namedItem(Material.NAME_TAG, ChatColor.YELLOW + "Firma umbenennen", List.of(
                ChatColor.YELLOW + "Klick zum Umbenennen"
        )));

        if (isOwner) {
            inv.setItem(34, namedItem(Material.BARRIER, ChatColor.RED + "Firma aufloesen", List.of(
                    ChatColor.RED + "Achtung: unwiderruflich!",
                    ChatColor.YELLOW + "Klick zum Aufloesen (mit Bestaetigung)"
            )));
        }

        inv.setItem(40, namedItem(Material.ARROW, ChatColor.GRAY + "Schliessen", List.of()));

        return inv;
    }

    static ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static String formatMoney(FirmaPlugin plugin, double amount) {
        if (plugin.getEconomy() != null) {
            return plugin.getEconomy().format(amount);
        }
        return String.format("%.2f", amount);
    }
}

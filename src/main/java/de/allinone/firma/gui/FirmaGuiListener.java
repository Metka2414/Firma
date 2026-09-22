package de.allinone.firma.gui;

import de.allinone.firma.FirmaPlugin;
import de.allinone.firma.data.Firma;
import de.allinone.firma.data.FirmaRole;
import de.allinone.firma.data.FirmaType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

public class FirmaGuiListener implements Listener {

    private final FirmaPlugin plugin;

    public FirmaGuiListener(FirmaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        if (title.equals(FirmaGui.MAIN_TITLE)) {
            event.setCancelled(true);
            handleMain(player, event.getSlot());
        } else if (title.equals(FirmaGui.CREATE_TITLE)) {
            event.setCancelled(true);
            handleCreate(player, event.getSlot());
        } else if (title.startsWith(FirmaGui.DASHBOARD_PREFIX)) {
            event.setCancelled(true);
            handleDashboard(player, event.getSlot());
        } else if (title.equals(BankGui.TITLE)) {
            event.setCancelled(true);
            handleBank(player, event.getSlot());
        } else if (title.equals(MembersGui.TITLE)) {
            event.setCancelled(true);
            handleMembers(player, event);
        } else if (title.equals(UpgradeGui.TITLE)) {
            event.setCancelled(true);
            handleUpgrade(player, event);
        } else if (title.equals(EmployeesGui.TITLE)) {
            event.setCancelled(true);
            handleEmployees(player, event.getSlot());
        }
    }

    private Firma currentFirma(Player player) {
        return plugin.getFirmaManager().getOpenFirma(player.getUniqueId());
    }

    private void handleMain(Player player, int slot) {
        if (slot == 13) {
            CreateFirmaGui.open(player);
        }
    }

    private void handleCreate(Player player, int slot) {
        FirmaType type = CreateFirmaGui.typeForSlot(slot);
        if (type == null) return;

        if (plugin.getFirmaManager().getFirmaOf(player.getUniqueId()) != null) {
            player.sendMessage(ChatColor.RED + "Du bist bereits Mitglied einer Firma.");
            player.closeInventory();
            return;
        }

        if (!plugin.getEconomy().has(player, 100000)) {
            player.sendMessage(ChatColor.RED + "Du brauchst 100.000$ um eine Firma zu gruenden.");
            player.closeInventory();
            return;
        }

        player.closeInventory();
        FirmaCreationSession.start(player.getUniqueId(), type);
        player.sendMessage(ChatColor.GREEN + "Gib jetzt den Namen deiner Firma im Chat ein:");
    }

    private void handleDashboard(Player player, int slot) {
        Firma firma = currentFirma(player);
        if (firma == null) return;

        switch (slot) {
            case 10 -> BankGui.open(player, plugin, firma);
            case 12 -> MembersGui.open(player, plugin, firma);
            case 14 -> EmployeesGui.open(player, plugin, firma);
            case 16 -> UpgradeGui.open(player, plugin, firma);
            case 28 -> {
                if (firma.getType() == FirmaType.IMMOBILIEN || firma.getType() == FirmaType.BAU) {
                    player.sendMessage(ChatColor.YELLOW + "Diese Funktion wird in Kuerze ergaenzt.");
                }
            }
            case 30 -> {
                if (firma.getRole(player.getUniqueId()) != FirmaRole.OWNER) {
                    player.sendMessage(ChatColor.RED + "Nur Eigentuemer koennen die Firma verkaufen.");
                    return;
                }
                player.closeInventory();
                PendingInput.start(player.getUniqueId(), PendingInput.Type.SET_SALE_PRICE, firma.getId());
                player.sendMessage(ChatColor.GREEN + "Gib den Verkaufspreis im Chat ein (0 = Verkauf beenden):");
            }
            case 32 -> {
                player.closeInventory();
                PendingInput.start(player.getUniqueId(), PendingInput.Type.RENAME_FIRMA, firma.getId());
                player.sendMessage(ChatColor.GREEN + "Gib den neuen Firmennamen im Chat ein:");
            }
            case 34 -> {
                if (firma.getRole(player.getUniqueId()) != FirmaRole.OWNER) return;
                plugin.getFirmaManager().delete(firma);
                plugin.getFirmaManager().clearOpenFirma(player.getUniqueId());
                player.closeInventory();
                player.sendMessage(ChatColor.RED + "Firma wurde aufgeloest.");
            }
            case 40 -> player.closeInventory();
            default -> {}
        }
    }

    private void handleBank(Player player, int slot) {
        Firma firma = currentFirma(player);
        if (firma == null) return;

        if (slot == 11) {
            player.closeInventory();
            PendingInput.start(player.getUniqueId(), PendingInput.Type.BANK_DEPOSIT, firma.getId());
            player.sendMessage(ChatColor.GREEN + "Gib den Betrag im Chat ein, den du einzahlen willst (0 = abbrechen):");
        } else if (slot == 15) {
            if (firma.getRole(player.getUniqueId()) != FirmaRole.OWNER) {
                player.sendMessage(ChatColor.RED + "Du hast keine Auszahlungs-Rechte.");
                return;
            }
            player.closeInventory();
            PendingInput.start(player.getUniqueId(), PendingInput.Type.BANK_WITHDRAW, firma.getId());
            player.sendMessage(ChatColor.GREEN + "Gib den Betrag im Chat ein, den du auszahlen willst (0 = abbrechen):");
        } else if (slot == 22) {
            FirmaGui.openDashboard(player, plugin, firma);
        }
    }

    private void handleMembers(Player player, InventoryClickEvent event) {
        Firma firma = currentFirma(player);
        if (firma == null) return;

        int slot = event.getSlot();
        var item = event.getCurrentItem();
        int size = event.getInventory().getSize();
        int bottomRow = size - 9;

        if (slot == bottomRow + 6) {
            FirmaGui.openDashboard(player, plugin, firma);
            return;
        }
        if (slot == bottomRow + 2 && firma.getRole(player.getUniqueId()) == FirmaRole.OWNER) {
            player.closeInventory();
            PendingInput.start(player.getUniqueId(), PendingInput.Type.INVITE_PLAYER, firma.getId());
            player.sendMessage(ChatColor.GREEN + "Gib den Spielernamen im Chat ein, den du einladen willst:");
            return;
        }

        if (item == null || item.getItemMeta() == null) return;
        if (firma.getRole(player.getUniqueId()) != FirmaRole.OWNER) return;

        int index = 0;
        for (UUID memberId : firma.getMembers().keySet()) {
            if (index == slot) {
                if (memberId.equals(player.getUniqueId())) return;

                if (event.getClick() == ClickType.RIGHT) {
                    firma.removeMember(memberId);
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(memberId);
                    player.sendMessage(ChatColor.YELLOW + String.valueOf(offline.getName()) + " wurde aus der Firma entfernt.");
                    MembersGui.open(player, plugin, firma);
                } else if (event.getClick() == ClickType.LEFT) {
                    FirmaRole currentRole = firma.getRole(memberId);
                    firma.addMember(memberId, currentRole == FirmaRole.OWNER ? FirmaRole.EMPLOYEE : FirmaRole.OWNER);
                    player.sendMessage(ChatColor.GREEN + "Rolle geaendert!");
                    MembersGui.open(player, plugin, firma);
                }
                return;
            }
            index++;
        }
    }

    private void handleUpgrade(Player player, InventoryClickEvent event) {
        Firma firma = currentFirma(player);
        if (firma == null) return;

        int slot = event.getSlot();
        if (slot == 22) {
            FirmaGui.openDashboard(player, plugin, firma);
            return;
        }

        String type = slot == 11 ? "resource" : slot == 15 ? "slots" : null;
        if (type == null) return;

        int amount = 1;
        if (event.isRightClick() && event.isShiftClick()) amount = 100;
        else if (event.isRightClick()) amount = 15;

        int bought = 0;
        double totalCost = 0;

        for (int i = 0; i < amount; i++) {
            int currentLevel = type.equals("resource") ? firma.getResourceLevel() : firma.getEmployeeSlotLevel();
            if (currentLevel >= Firma.MAX_LEVEL) break;

            double cost = UpgradeGui.cost(type, currentLevel);
            if (!firma.withdrawBank(cost)) break;

            totalCost += cost;
            if (type.equals("resource")) {
                firma.setResourceLevel(currentLevel + 1);
            } else {
                firma.setEmployeeSlotLevel(currentLevel + 1);
            }
            bought++;
        }

        if (bought == 0) {
            player.sendMessage(ChatColor.RED.toString() + "Nicht genug Geld in der Firmenbank fuer auch nur 1 Level!");
        } else {
            player.sendMessage(ChatColor.GREEN.toString() + bought + " Level gekauft fuer " + FirmaGui.formatMoney(plugin, totalCost) + " (aus der Firmenbank)!");
        }

        UpgradeGui.open(player, plugin, firma);
    }

    private void handleEmployees(Player player, int slot) {
        Firma firma = currentFirma(player);
        if (firma == null) return;

        if (slot == 11) {
            if (firma.getDigitalEmployees() >= firma.getMaxEmployees()) {
                player.sendMessage(ChatColor.RED + "Maximale Mitarbeiterzahl erreicht! Upgrade die Slots.");
                return;
            }
            if (!firma.withdrawBank(EmployeesGui.HIRE_COST)) {
                player.sendMessage(ChatColor.RED + "Nicht genug Geld in der Firmenbank!");
                return;
            }
            firma.setDigitalEmployees(firma.getDigitalEmployees() + 1);
            player.sendMessage(ChatColor.GREEN + "Mitarbeiter angestellt!");
            EmployeesGui.open(player, plugin, firma);
        } else if (slot == 13) {
            if (firma.getDigitalEmployees() <= 0) return;
            firma.setDigitalEmployees(firma.getDigitalEmployees() - 1);
            player.sendMessage(ChatColor.YELLOW + "Mitarbeiter entlassen.");
            EmployeesGui.open(player, plugin, firma);
        } else if (slot == 22) {
            FirmaGui.openDashboard(player, plugin, firma);
        }
    }
}

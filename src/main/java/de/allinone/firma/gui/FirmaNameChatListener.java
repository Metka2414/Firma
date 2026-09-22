package de.allinone.firma.gui;

import de.allinone.firma.FirmaPlugin;
import de.allinone.firma.data.Firma;
import de.allinone.firma.data.FirmaRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class FirmaNameChatListener implements Listener {

    private final FirmaPlugin plugin;

    public FirmaNameChatListener(FirmaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = ChatColor.stripColor(event.getMessage()).trim();

        if (FirmaCreationSession.isActive(player.getUniqueId())) {
            event.setCancelled(true);
            handleFirmaCreation(player, message);
            return;
        }

        if (PendingInput.isActive(player.getUniqueId())) {
            event.setCancelled(true);
            handlePendingInput(player, message);
        }
    }

    private void handleFirmaCreation(Player player, String message) {
        FirmaCreationSession session = FirmaCreationSession.get(player.getUniqueId());

        if (message.equalsIgnoreCase("abbrechen")) {
            FirmaCreationSession.clear(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.YELLOW + "Firmengruendung abgebrochen."));
            return;
        }

        if (message.length() < 3 || message.length() > 24) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.RED + "Der Name muss zwischen 3 und 24 Zeichen lang sein. Versuch es erneut:"));
            return;
        }

        if (plugin.getFirmaManager().nameTaken(message)) {
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.RED + "Dieser Firmenname ist bereits vergeben. Versuch einen anderen:"));
            return;
        }

        if (!plugin.getEconomy().has(player, 100000)) {
            FirmaCreationSession.clear(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.sendMessage(ChatColor.RED + "Du hast nicht mehr genug Geld (100.000$ noetig)."));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, 100000);
        Firma firma = plugin.getFirmaManager().create(message, session.getType(), player.getUniqueId());
        FirmaCreationSession.clear(player.getUniqueId());

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Firma gegruendet: " + ChatColor.RESET
                    + ChatColor.GREEN + firma.getName() + ChatColor.GRAY + " (" + firma.getType().name() + ")");
            FirmaGui.openDashboard(player, plugin, firma);
        });
    }

    private void handlePendingInput(Player player, String message) {
        PendingInput input = PendingInput.get(player.getUniqueId());
        Firma firma = plugin.getFirmaManager().get(input.getFirmaId());

        if (firma == null) {
            PendingInput.clear(player.getUniqueId());
            return;
        }

        switch (input.getType()) {
            case BANK_DEPOSIT -> handleDeposit(player, firma, message);
            case BANK_WITHDRAW -> handleWithdraw(player, firma, message);
            case INVITE_PLAYER -> handleInvite(player, firma, message);
            case SET_SALE_PRICE -> handleSalePrice(player, firma, message);
            case RENAME_FIRMA -> handleRename(player, firma, message);
            default -> PendingInput.clear(player.getUniqueId());
        }
    }

    private double parseAmount(String message) {
        String cleaned = message.replace(",", ".").replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) return -1;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void handleDeposit(Player player, Firma firma, String message) {
        double amount = parseAmount(message);
        if (amount < 0) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Ungueltiger Betrag. Versuch es erneut (0 = abbrechen):"));
            return;
        }
        PendingInput.clear(player.getUniqueId());

        if (amount == 0) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.YELLOW + "Einzahlung abgebrochen."));
            return;
        }
        if (!plugin.getEconomy().has(player, amount)) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Du hast nicht genug Geld."));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, amount);
        firma.addBankBalance(amount);

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(ChatColor.GREEN + FirmaGui.formatMoney(plugin, amount) + " in die Firmenbank eingezahlt!");
            BankGui.open(player, plugin, firma);
        });
    }

    private void handleWithdraw(Player player, Firma firma, String message) {
        double amount = parseAmount(message);
        if (amount < 0) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Ungueltiger Betrag. Versuch es erneut (0 = abbrechen):"));
            return;
        }
        PendingInput.clear(player.getUniqueId());

        if (amount == 0) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.YELLOW + "Auszahlung abgebrochen."));
            return;
        }
        if (firma.getRole(player.getUniqueId()) != FirmaRole.OWNER) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Du hast keine Auszahlungs-Rechte."));
            return;
        }
        if (!firma.withdrawBank(amount)) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Nicht genug Geld in der Firmenbank."));
            return;
        }

        plugin.getEconomy().depositPlayer(player, amount);

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(ChatColor.GREEN + FirmaGui.formatMoney(plugin, amount) + " ausgezahlt!");
            BankGui.open(player, plugin, firma);
        });
    }

    private void handleInvite(Player player, Firma firma, String message) {
        PendingInput.clear(player.getUniqueId());

        Bukkit.getScheduler().runTask(plugin, () -> {
            OfflinePlayer resolved = Bukkit.getOfflinePlayer(message);
            if (resolved == null || resolved.getName() == null) {
                player.sendMessage(ChatColor.RED + "Spieler nicht gefunden.");
                return;
            }
            if (firma.isMember(resolved.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Dieser Spieler ist bereits Mitglied.");
                return;
            }
            if (plugin.getFirmaManager().getFirmaOf(resolved.getUniqueId()) != null) {
                player.sendMessage(ChatColor.RED + "Dieser Spieler ist bereits in einer anderen Firma.");
                return;
            }

            firma.addMember(resolved.getUniqueId(), FirmaRole.EMPLOYEE);
            player.sendMessage(ChatColor.GREEN + resolved.getName() + " wurde zur Firma hinzugefuegt!");
            if (resolved.isOnline() && resolved.getPlayer() != null) {
                resolved.getPlayer().sendMessage(ChatColor.GREEN + "Du wurdest der Firma '" + firma.getName() + "' hinzugefuegt!");
            }
            MembersGui.open(player, plugin, firma);
        });
    }

    private void handleSalePrice(Player player, Firma firma, String message) {
        double amount = parseAmount(message);
        if (amount < 0) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Ungueltiger Betrag. Versuch es erneut:"));
            return;
        }
        PendingInput.clear(player.getUniqueId());

        if (amount == 0) {
            firma.setForSale(false);
            firma.setSalePrice(0);
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ChatColor.YELLOW + "Firma ist nicht mehr zum Verkauf.");
                FirmaGui.openDashboard(player, plugin, firma);
            });
            return;
        }

        firma.setForSale(true);
        firma.setSalePrice(amount);
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(ChatColor.GREEN + "Firma ist jetzt fuer " + FirmaGui.formatMoney(plugin, amount) + " zum Verkauf!");
            FirmaGui.openDashboard(player, plugin, firma);
        });
    }

    private void handleRename(Player player, Firma firma, String message) {
        PendingInput.clear(player.getUniqueId());

        if (message.length() < 3 || message.length() > 24) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Ungueltiger Name (3-24 Zeichen)."));
            return;
        }
        if (plugin.getFirmaManager().nameTaken(message)) {
            Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage(ChatColor.RED + "Dieser Name ist bereits vergeben."));
            return;
        }

        firma.setName(message);
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.sendMessage(ChatColor.GREEN + "Firma wurde umbenannt zu: " + message);
            FirmaGui.openDashboard(player, plugin, firma);
        });
    }
}

package de.allinone.firma;

import de.allinone.firma.gui.FirmaGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FirmaCommand implements CommandExecutor {

    private final FirmaPlugin plugin;

    public FirmaCommand(FirmaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl kann nur von Spielern genutzt werden.");
            return true;
        }

        FirmaGui.openMain(player, plugin);
        return true;
    }
}

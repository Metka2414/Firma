package de.allinone.firma;

import de.allinone.firma.data.FirmaManager;
import de.allinone.firma.gui.FirmaGuiListener;
import de.allinone.firma.gui.FirmaNameChatListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class FirmaPlugin extends JavaPlugin {

    private static FirmaPlugin instance;

    private Economy economy;
    private FirmaManager firmaManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault/EssentialsX wurde nicht gefunden! Plugin wird deaktiviert.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        this.firmaManager = new FirmaManager(this);
        firmaManager.loadAll();

        getServer().getPluginManager().registerEvents(new FirmaGuiListener(this), this);
        getServer().getPluginManager().registerEvents(new FirmaNameChatListener(this), this);
        getCommand("firma").setExecutor(new FirmaCommand(this));

        Bukkit.getScheduler().runTaskTimer(this, () -> firmaManager.tickAll(), 20L, 20L);
        Bukkit.getScheduler().runTaskTimer(this, firmaManager::saveAll, 6000L, 6000L);

        getLogger().info("FirmaPlugin wurde aktiviert! (" + firmaManager.getAll().size() + " Firmen geladen)");
    }

    @Override
    public void onDisable() {
        if (firmaManager != null) {
            firmaManager.saveAll();
        }
        getLogger().info("FirmaPlugin wurde deaktiviert!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            return false;
        }
        this.economy = provider.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return economy;
    }

    public FirmaManager getFirmaManager() {
        return firmaManager;
    }

    public static FirmaPlugin getInstance() {
        return instance;
    }
}

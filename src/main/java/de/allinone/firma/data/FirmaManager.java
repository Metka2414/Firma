package de.allinone.firma.data;

import de.allinone.firma.FirmaPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FirmaManager {

    private final FirmaPlugin plugin;
    private final Map<UUID, Firma> firmen = new HashMap<>();
    private final Map<UUID, UUID> openGuiSessions = new HashMap<>();
    private final File file;

    public FirmaManager(FirmaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "firmen.yml");
    }

    public Firma create(String name, FirmaType type, UUID founder) {
        Firma firma = new Firma(UUID.randomUUID(), name, type, founder);
        firmen.put(firma.getId(), firma);
        return firma;
    }

    public void delete(Firma firma) {
        firmen.remove(firma.getId());
    }

    public Firma get(UUID id) {
        return firmen.get(id);
    }

    public List<Firma> getAll() {
        return new ArrayList<>(firmen.values());
    }

    public Firma getFirmaOf(UUID playerId) {
        for (Firma firma : firmen.values()) {
            if (firma.isMember(playerId)) return firma;
        }
        return null;
    }

    public boolean nameTaken(String name) {
        for (Firma firma : firmen.values()) {
            if (firma.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public void setOpenFirma(UUID playerId, UUID firmaId) {
        openGuiSessions.put(playerId, firmaId);
    }

    public Firma getOpenFirma(UUID playerId) {
        UUID firmaId = openGuiSessions.get(playerId);
        return firmaId != null ? firmen.get(firmaId) : null;
    }

    public void clearOpenFirma(UUID playerId) {
        openGuiSessions.remove(playerId);
    }

    public void tickAll() {
        long now = System.currentTimeMillis();
        for (Firma firma : firmen.values()) {
            if (firma.getDigitalEmployees() <= 0) continue;
            long elapsedSeconds = (now - firma.getLastTick()) / 1000L;
            if (elapsedSeconds >= 60) {
                firma.addBankBalance(firma.getIncomePerCycle());
                firma.setLastTick(now);
            }
        }
    }

    public void saveAll() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        YamlConfiguration config = new YamlConfiguration();
        int i = 0;
        for (Firma firma : firmen.values()) {
            String path = "firmen." + i;
            config.set(path + ".id", firma.getId().toString());
            config.set(path + ".name", firma.getName());
            config.set(path + ".type", firma.getType().name());
            config.set(path + ".bank", firma.getBankBalance());
            config.set(path + ".resource-level", firma.getResourceLevel());
            config.set(path + ".employee-slot-level", firma.getEmployeeSlotLevel());
            config.set(path + ".digital-employees", firma.getDigitalEmployees());
            config.set(path + ".plot-id", firma.getPlotId());
            config.set(path + ".for-sale", firma.isForSale());
            config.set(path + ".sale-price", firma.getSalePrice());
            config.set(path + ".last-tick", firma.getLastTick());

            for (Map.Entry<UUID, FirmaRole> entry : firma.getMembers().entrySet()) {
                config.set(path + ".members." + entry.getKey(), entry.getValue().name());
            }

            int p = 0;
            for (PropertyListing listing : firma.getProperties()) {
                String pp = path + ".properties." + p;
                config.set(pp + ".id", listing.getId().toString());
                config.set(pp + ".name", listing.getName());
                config.set(pp + ".price", listing.getPrice());
                config.set(pp + ".sold", listing.isSold());
                if (listing.getBuyer() != null) config.set(pp + ".buyer", listing.getBuyer().toString());
                p++;
            }

            int b = 0;
            for (BuildRequest req : firma.getBuildRequests()) {
                String bp = path + ".requests." + b;
                config.set(bp + ".id", req.getId().toString());
                config.set(bp + ".requester", req.getRequester().toString());
                config.set(bp + ".description", req.getDescription());
                config.set(bp + ".offer", req.getOffer());
                config.set(bp + ".accepted", req.isAccepted());
                config.set(bp + ".completed", req.isCompleted());
                b++;
            }

            i++;
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte firmen.yml nicht speichern: " + e.getMessage());
        }
    }

    public void loadAll() {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.isConfigurationSection("firmen")) return;

        for (String key : config.getConfigurationSection("firmen").getKeys(false)) {
            String path = "firmen." + key;
            try {
                UUID id = UUID.fromString(config.getString(path + ".id"));
                String name = config.getString(path + ".name");
                FirmaType type = FirmaType.valueOf(config.getString(path + ".type"));

                Map<UUID, FirmaRole> memberMap = new HashMap<>();
                ConfigurationSection memberSection = config.getConfigurationSection(path + ".members");
                if (memberSection != null) {
                    for (String memberKey : memberSection.getKeys(false)) {
                        memberMap.put(UUID.fromString(memberKey), FirmaRole.valueOf(memberSection.getString(memberKey)));
                    }
                }

                UUID firstOwner = memberMap.entrySet().stream()
                        .filter(e -> e.getValue() == FirmaRole.OWNER)
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(UUID.randomUUID());

                Firma firma = new Firma(id, name, type, firstOwner);
                firma.getMembers().clear();
                firma.getMembers().putAll(memberMap);

                firma.addBankBalance(config.getDouble(path + ".bank", 0));
                firma.setResourceLevel(config.getInt(path + ".resource-level", 0));
                firma.setEmployeeSlotLevel(config.getInt(path + ".employee-slot-level", 0));
                firma.setDigitalEmployees(config.getInt(path + ".digital-employees", 0));
                firma.setPlotId(config.getString(path + ".plot-id"));
                firma.setForSale(config.getBoolean(path + ".for-sale", false));
                firma.setSalePrice(config.getDouble(path + ".sale-price", 0));
                firma.setLastTick(config.getLong(path + ".last-tick", System.currentTimeMillis()));

                ConfigurationSection propSection = config.getConfigurationSection(path + ".properties");
                if (propSection != null) {
                    for (String propKey : propSection.getKeys(false)) {
                        String pp = path + ".properties." + propKey;
                        PropertyListing listing = new PropertyListing(
                                config.getString(pp + ".name"), config.getDouble(pp + ".price"));
                        listing.setSold(config.getBoolean(pp + ".sold", false));
                        String buyerStr = config.getString(pp + ".buyer");
                        if (buyerStr != null) listing.setBuyer(UUID.fromString(buyerStr));
                        firma.getProperties().add(listing);
                    }
                }

                ConfigurationSection reqSection = config.getConfigurationSection(path + ".requests");
                if (reqSection != null) {
                    for (String reqKey : reqSection.getKeys(false)) {
                        String bp = path + ".requests." + reqKey;
                        BuildRequest req = new BuildRequest(
                                UUID.fromString(config.getString(bp + ".requester")),
                                config.getString(bp + ".description"),
                                config.getDouble(bp + ".offer"));
                        req.setAccepted(config.getBoolean(bp + ".accepted", false));
                        req.setCompleted(config.getBoolean(bp + ".completed", false));
                        firma.getBuildRequests().add(req);
                    }
                }

                firmen.put(id, firma);
            } catch (Exception e) {
                plugin.getLogger().warning("Konnte Firma '" + key + "' nicht laden: " + e.getMessage());
            }
        }
        plugin.getLogger().info(firmen.size() + " Firmen geladen.");
    }
}

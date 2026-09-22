package de.allinone.firma.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Firma {

    public static final int MAX_LEVEL = 100;
    public static final double BASE_EMPLOYEE_INCOME = 500.0;
    public static final int START_MAX_EMPLOYEES = 3;

    private final UUID id;
    private String name;
    private FirmaType type;
    private final Map<UUID, FirmaRole> members = new HashMap<>();

    private double bankBalance = 0;
    private int resourceLevel = 0;
    private int employeeSlotLevel = 0;
    private int digitalEmployees = 0;

    private String plotId;
    private boolean forSale = false;
    private double salePrice = 0;

    private final List<PropertyListing> properties = new ArrayList<>();
    private final List<BuildRequest> buildRequests = new ArrayList<>();

    private long lastTick = System.currentTimeMillis();

    public Firma(UUID id, String name, FirmaType type, UUID founder) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.members.put(founder, FirmaRole.OWNER);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FirmaType getType() {
        return type;
    }

    public void setType(FirmaType type) {
        this.type = type;
    }

    public Map<UUID, FirmaRole> getMembers() {
        return members;
    }

    public void addMember(UUID uuid, FirmaRole role) {
        members.put(uuid, role);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public boolean isOwner(UUID uuid) {
        return members.get(uuid) == FirmaRole.OWNER;
    }

    public FirmaRole getRole(UUID uuid) {
        return members.get(uuid);
    }

    public int getOwnerCount() {
        return (int) members.values().stream().filter(r -> r == FirmaRole.OWNER).count();
    }

    public double getBankBalance() {
        return bankBalance;
    }

    public void addBankBalance(double amount) {
        this.bankBalance += amount;
    }

    public boolean withdrawBank(double amount) {
        if (bankBalance < amount) return false;
        bankBalance -= amount;
        return true;
    }

    public int getResourceLevel() {
        return resourceLevel;
    }

    public void setResourceLevel(int resourceLevel) {
        this.resourceLevel = Math.max(0, Math.min(resourceLevel, MAX_LEVEL));
    }

    public int getEmployeeSlotLevel() {
        return employeeSlotLevel;
    }

    public void setEmployeeSlotLevel(int employeeSlotLevel) {
        this.employeeSlotLevel = Math.max(0, Math.min(employeeSlotLevel, MAX_LEVEL));
    }

    public int getDigitalEmployees() {
        return digitalEmployees;
    }

    public void setDigitalEmployees(int digitalEmployees) {
        this.digitalEmployees = Math.max(0, Math.min(digitalEmployees, getMaxEmployees()));
    }

    public int getMaxEmployees() {
        double progress = employeeSlotLevel / (double) MAX_LEVEL;
        return (int) Math.round(START_MAX_EMPLOYEES + (47 * progress));
    }

    public double getIncomePerCycle() {
        double multiplier = 1.0 + (resourceLevel / 100.0) * 4.0;
        return BASE_EMPLOYEE_INCOME * digitalEmployees * multiplier;
    }

    public String getPlotId() {
        return plotId;
    }

    public void setPlotId(String plotId) {
        this.plotId = plotId;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice = salePrice;
    }

    public List<PropertyListing> getProperties() {
        return properties;
    }

    public List<BuildRequest> getBuildRequests() {
        return buildRequests;
    }

    public long getLastTick() {
        return lastTick;
    }

    public void setLastTick(long lastTick) {
        this.lastTick = lastTick;
    }
}

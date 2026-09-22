package de.allinone.firma.data;

import java.util.UUID;

public class PropertyListing {

    private final UUID id;
    private String name;
    private double price;
    private boolean sold;
    private UUID buyer;

    public PropertyListing(String name, double price) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.price = price;
        this.sold = false;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }

    public UUID getBuyer() {
        return buyer;
    }

    public void setBuyer(UUID buyer) {
        this.buyer = buyer;
    }
}

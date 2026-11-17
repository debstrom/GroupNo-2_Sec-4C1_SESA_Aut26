package com.uits.geoshield.models;

public class HelpPlace {
    private final String name;
    private final double distanceKm;
    private final String phone;

    public HelpPlace(String name, double distanceKm, String phone) {
        this.name = name;
        this.distanceKm = distanceKm;
        this.phone = phone;
    }

    public String getName() { return name; }
    public double getDistanceKm() { return distanceKm; }
    public String getPhone() { return phone; }
}

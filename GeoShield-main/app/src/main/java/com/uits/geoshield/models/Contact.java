package com.uits.geoshield.models;

public class Contact {
    private final String name;
    private final String relation;
    private final String channels;
    private final String phone;
    private boolean verified;

    public Contact(String name, String relation, String channels, String phone, boolean verified) {
        this.name = name;
        this.relation = relation;
        this.channels = channels;
        this.phone = phone;
        this.verified = verified;
    }

    public String getName() { return name; }
    public String getRelation() { return relation; }
    public String getChannels() { return channels; }
    public String getPhone() { return phone; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getInitial() {
        return name == null || name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
    }
}

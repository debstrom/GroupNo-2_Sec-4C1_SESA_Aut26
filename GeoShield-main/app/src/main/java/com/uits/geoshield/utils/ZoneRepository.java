package com.uits.geoshield.utils;

import com.uits.geoshield.models.Zone;

import java.util.ArrayList;
import java.util.List;

/** Sample safety zones. Replace with API data when the backend is ready. */
public final class ZoneRepository {
    private ZoneRepository() {}

    public static List<Zone> getZones() {
        List<Zone> zones = new ArrayList<>();
        zones.add(new Zone("Dhanmondi", "Well-lit roads, regular patrols reported",
                Zone.Level.SAFE, 0.22f, 0.55f, 0.27f));
        zones.add(new Zone("Jigatola", "Busy at night, occasional harassment reports",
                Zone.Level.MODERATE, 0.68f, 0.65f, 0.23f));
        zones.add(new Zone("Mohammadpur", "Frequent harassment, poor lighting reported",
                Zone.Level.RISKY, 0.41f, 0.92f, 0.22f));
        zones.add(new Zone("Demo Area (sample)", "Multiple recent incidents reported, avoid after dark",
                Zone.Level.DANGEROUS, 0.80f, 0.98f, 0.20f));
        return zones;
    }
}

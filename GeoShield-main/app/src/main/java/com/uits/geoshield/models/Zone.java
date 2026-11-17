package com.uits.geoshield.models;

import com.uits.geoshield.R;

public class Zone {

    public enum Level {
        SAFE(R.string.level_safe, R.color.zone_safe),
        MODERATE(R.string.level_moderate, R.color.zone_moderate),
        RISKY(R.string.level_risky, R.color.zone_risky),
        DANGEROUS(R.string.level_dangerous, R.color.zone_dangerous);

        public final int labelRes;
        public final int colorRes;

        Level(int labelRes, int colorRes) {
            this.labelRes = labelRes;
            this.colorRes = colorRes;
        }
    }

    private final String name;
    private final String description;
    private final Level level;
    /** Centre x as a fraction of view width. */
    private final float cx;
    /** Centre y as a fraction of the visible map height. */
    private final float cy;
    /** Radius as a fraction of view width. */
    private final float radius;

    public Zone(String name, String description, Level level, float cx, float cy, float radius) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Level getLevel() { return level; }
    public float getCx() { return cx; }
    public float getCy() { return cy; }
    public float getRadius() { return radius; }
}

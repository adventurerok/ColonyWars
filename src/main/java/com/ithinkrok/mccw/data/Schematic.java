package com.ithinkrok.mccw.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

/**
 * Created by paul on 04/11/15.
 * <p>
 * Stores schematic data for buildings
 */
public class Schematic {

    private String schematicFile;
    private String buildingName;
    private int baseRotation;
    private String transformName;
    private Vector offset;

    public Schematic(String buildingName, ConfigurationSection config) {
        this.buildingName = buildingName;
        this.schematicFile = config.getString("buildings.schematics." + buildingName);
        this.baseRotation = config.getInt("buildings.rotations." + buildingName);

        String base = "buildings.offsets." + buildingName;

        this.offset = new Vector(config.getInt(base + ".x"), config.getInt(base + ".y"), config.getInt(base + ".z"));
        this.transformName = config.getString("buildings.transform." + buildingName, buildingName);
    }


    public String getBuildingName() {
        return buildingName;
    }

    public String getSchematicFile() {
        return schematicFile;
    }

    public int getBaseRotation() {
        return baseRotation;
    }

    public Vector getOffset() {
        return offset;
    }

    public String getTransformName() {
        return transformName;
    }
}

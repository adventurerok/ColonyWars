package com.ithinkrok.mccw.data;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * Created by paul on 04/11/15.
 * <p>
 * Stores schematic data for buildings
 */
public class SchematicData {

    private String schematicFile;
    private String buildingName;
    private int baseRotation;

    public SchematicData(String buildingName, FileConfiguration config) {
        this(buildingName, config.getString("buildings.schematics." + buildingName),
                config.getInt("buildings.rotations." + buildingName));
    }

    public SchematicData(String buildingName, String schematicFile, int baseRotation) {
        this.buildingName = buildingName;
        this.schematicFile = schematicFile;
        this.baseRotation = baseRotation;
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
}

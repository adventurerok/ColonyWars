package com.ithinkrok.mccw.data;

/**
 * Created by paul on 04/11/15.
 *
 * Stores schematic data for buildings
 */
public class SchematicData {

    private String schematicFile;
    private String buildingName;

    public String getBuildingName() {
        return buildingName;
    }

    public String getSchematicFile() {
        return schematicFile;
    }

    public SchematicData(String buildingName, String schematicFile) {
        this.buildingName = buildingName;
        this.schematicFile = schematicFile;
    }
}

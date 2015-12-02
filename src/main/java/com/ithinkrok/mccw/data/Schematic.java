package com.ithinkrok.mccw.data;

import com.flowpowered.nbt.*;
import com.flowpowered.nbt.stream.NBTInputStream;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by paul on 04/11/15.
 * <p>
 * Stores schematic data for buildings
 */
public class Schematic {

    private String buildingName;
    private int baseRotation;
    private String transformName;
    private Vector offset;
    private Vector size;

    private byte[] blocks;
    private byte[] data;

    public Schematic(File dataFolder, String buildingName, ConfigurationSection config) {
        this.buildingName = buildingName;
        this.baseRotation = config.getInt("buildings.rotations." + buildingName);
        String schematicFile = config.getString("buildings.schematics." + buildingName);

        String base = "buildings.offsets." + buildingName;

        Vector baseOffset =
                new Vector(config.getInt(base + ".x"), config.getInt(base + ".y"), config.getInt(base + ".z"));
        this.transformName = config.getString("buildings.transform." + buildingName, buildingName);

        File schemFile = new File(dataFolder, schematicFile);

        try (NBTInputStream in = new NBTInputStream(new FileInputStream(schemFile))) {
            CompoundMap nbt = ((CompoundTag) in.readTag()).getValue();

            short width = ((ShortTag) nbt.get("Width")).getValue();
            short height = ((ShortTag) nbt.get("Height")).getValue();
            short length = ((ShortTag) nbt.get("Length")).getValue();

            int offsetX = ((IntTag) nbt.get("WEOffsetX")).getValue() + baseOffset.getBlockX();
            int offsetY = ((IntTag) nbt.get("WEOffsetY")).getValue() + baseOffset.getBlockY();
            int offsetZ = ((IntTag) nbt.get("WEOffsetZ")).getValue() + baseOffset.getBlockZ();

            byte[] blocks = ((ByteArrayTag) nbt.get("Blocks")).getValue();
            byte[] data = ((ByteArrayTag) nbt.get("Data")).getValue();

            this.size = new Vector(width, height, length);
            this.offset = new Vector(offsetX, offsetY, offsetZ);

            this.blocks = blocks;
            this.data = data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load schematic: " + schematicFile, e);
        }
    }


    public String getBuildingName() {
        return buildingName;
    }

    public int getBaseRotation() {
        return baseRotation;
    }

    public Vector getOffset() {
        return offset;
    }

    public Vector getSize() {
        return size;
    }

    public byte[] getBlocks() {
        return blocks;
    }

    public byte[] getData() {
        return data;
    }

    public String getTransformName() {
        return transformName;
    }
}

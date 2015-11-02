package com.ithinkrok.mccw.util;

import com.flowpowered.nbt.*;
import com.flowpowered.nbt.stream.NBTInputStream;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 02/11/15.
 *
 * Builds schematics in the game world
 */
public class SchematicBuilder {

    public static List<Location> pasteSchematic(File schemFile, Location loc){
        try(NBTInputStream in = new NBTInputStream(new FileInputStream(schemFile))){
            CompoundMap nbt = ((CompoundTag) in.readTag()).getValue();

            short width = ((ShortTag)nbt.get("Width")).getValue();
            short height = ((ShortTag)nbt.get("Height")).getValue();
            short length = ((ShortTag)nbt.get("Length")).getValue();

            int offsetX = ((IntTag)nbt.get("WEOffsetX")).getValue();
            int offsetY = ((IntTag)nbt.get("WEOffsetY")).getValue();
            int offsetZ = ((IntTag)nbt.get("WEOffsetZ")).getValue();

            byte[] blocks = ((ByteArrayTag)nbt.get("Blocks")).getValue();
            byte[] data = ((ByteArrayTag)nbt.get("Data")).getValue();

            List<Location> locations = new ArrayList<>();

            for(int x = 0; x < width; ++x){
                for(int y = 0; y < height; ++y){
                    for(int z = 0; z < length; ++z){
                        int index = width * (y * length + z) + x;

                        Location l = new Location(loc.getWorld(), x + loc.getX() + offsetX, y + loc.getY() + offsetY,
                                z + loc.getZ() + offsetZ);

                        int bId = blocks[index] & 0xFF;
                        if(bId == 0) continue;
                        Block block = l.getBlock();

                        block.setTypeId(bId);
                        block.setData(data[index]);

                        locations.add(l);
                    }
                }
            }

            return locations;
        } catch (IOException e) {
            e.printStackTrace();

            return new ArrayList<>();
        }
    }
}

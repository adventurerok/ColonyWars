package com.ithinkrok.mccw.util;

import com.flowpowered.nbt.*;
import com.flowpowered.nbt.stream.NBTInputStream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

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

    public static List<Location> buildSchematic(Plugin plugin, File schemFile, Location loc){
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

                        locations.add(l);
                    }
                }
            }

            SchematicBuilderTask task = new SchematicBuilderTask(width, height, length, offsetX, offsetY, offsetZ,
                    blocks, data, locations);

            task.schedule(plugin);

            return locations;
        } catch (IOException e) {
            e.printStackTrace();

            return new ArrayList<>();
        }

    }

    private static class SchematicBuilderTask implements Runnable {

        int index = 0;
        short width, height, length;
        int offsetX, offsetY, offsetZ;
        byte[] blocks, data;

        int taskId;

        List<Location> locations = new ArrayList<>();

        public SchematicBuilderTask(short width, short height, short length, int offsetX, int offsetY,
                                    int offsetZ, byte[] blocks, byte[] data, List<Location> locations) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.blocks = blocks;
            this.data = data;
            this.locations = locations;
        }

        @Override
        public void run() {
            int count = 0;

            while(index < locations.size()){
                ++count;
                if(count > 2) return;

                Location loc = locations.get(index);

                int x = loc.getBlockX() - offsetX;
                int y = loc.getBlockY() - offsetY;
                int z = loc.getBlockZ() - offsetZ;

                int blockInd = width * (y * length + z) + x;

                int bId = blocks[blockInd] & 0xFF;

                Block block = loc.getBlock();
                block.setTypeId(bId);
                block.setData(data[blockInd]);
            }

            Bukkit.getScheduler().cancelTask(taskId);
        }

        public void schedule(Plugin plugin){
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1, 1);
        }
    }
}

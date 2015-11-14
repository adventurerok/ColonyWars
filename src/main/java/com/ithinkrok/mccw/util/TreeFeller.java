package com.ithinkrok.mccw.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by paul on 01/11/15.
 *
 * Destroys all logs in a tree and drops ingots for them
 */
public class TreeFeller {

    public static void fellTree(Location location){
        LinkedList<Location> locations = new LinkedList<>();

        addSurrounding(locations, location);

        ArrayList<Location> ingotsToSpawn = new ArrayList<>();
        
        while(locations.size() > 0){
            Location pos = locations.removeFirst();

            Block block = pos.getBlock();
            
            if(block.getType() != Material.LOG && block.getType() != Material.LOG_2) continue;
            
            block.setType(Material.AIR);
            ingotsToSpawn.add(pos);

            addSurrounding(locations, pos);
        }

        for(int i = ingotsToSpawn.size() - 1; i >= 0; --i){
            Location pos = ingotsToSpawn.get(i);
            pos.getWorld().dropItemNaturally(pos, new ItemStack(Material.GOLD_INGOT, 1));
        }
    }

    private static void addSurrounding(List<Location> locations, Location l){
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY(), l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY(), l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY(), l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY(), l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY(), l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY(), l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY(), l.getZ() - 1d));

        locations.add(new Location(l.getWorld(), l.getX(), l.getY() + 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() + 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() + 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() + 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() + 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() + 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() + 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() + 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() + 1d, l.getZ() - 1d));

        locations.add(new Location(l.getWorld(), l.getX(), l.getY() - 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() - 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() - 1d, l.getZ()));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() - 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX(), l.getY() - 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() - 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() + 1d, l.getY() - 1d, l.getZ() - 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() - 1d, l.getZ() + 1d));
        locations.add(new Location(l.getWorld(), l.getX() - 1d, l.getY() - 1d, l.getZ() - 1d));
    }
}

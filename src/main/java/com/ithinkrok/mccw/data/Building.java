package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.util.BoundingBox;
import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 04/11/15.
 *
 * Represents info on a building built during the current game
 */
public class Building {

    private String buildingName;
    private TeamColor teamColor;
    private WarsPlugin plugin;
    private List<Location> buildingBlocks;
    private Map<Location, BlockState> oldBlocks;
    private Location centerBlock;
    private BoundingBox bounds;
    private boolean finished;
    private int rotation;
    private List<Hologram> holograms = new ArrayList<>();

    public int getRotation() {
        return rotation;
    }

    public Building(WarsPlugin plugin, String buildingName, TeamColor teamColor, Location centerBlock, int rotation,
                    List<Location> buildingBlocks, Map<Location, BlockState> oldBlocks) {
        this.plugin = plugin;
        this.buildingName = buildingName;
        this.teamColor = teamColor;
        this.centerBlock = centerBlock;
        this.rotation = rotation;
        this.buildingBlocks = buildingBlocks;
        this.oldBlocks = oldBlocks;

        if(centerBlock == null) centerBlock = buildingBlocks.get(0);

        Vector minBB = new Vector(centerBlock.getX(), centerBlock.getY(), centerBlock.getZ());
        Vector maxBB = new Vector(centerBlock.getX(), centerBlock.getY(), centerBlock.getZ());
        
        for(Location l : buildingBlocks){
            if(l.getX() < minBB.getX()) minBB.setX(l.getX());
            else if(l.getX() > maxBB.getX()) maxBB.setX(l.getX());

            if(l.getY() < minBB.getY()) minBB.setY(l.getY());
            else if(l.getY() > maxBB.getY()) maxBB.setY(l.getY());

            if(l.getZ() < minBB.getZ()) minBB.setZ(l.getZ());
            else if(l.getZ() > maxBB.getZ()) maxBB.setZ(l.getZ());
        }

        this.bounds = new BoundingBox(minBB, maxBB);
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;

        if(finished && centerBlock != null){
            Location holo1 = centerBlock.clone().add(0.5d, 2.2d, 0.5d);
            Hologram hologram1 = HologramAPI.createHologram(holo1, plugin.getLocale("building.shop.name", buildingName));
            hologram1.spawn();

            Location holo2 = centerBlock.clone().add(0.5d, 1.9d, 0.5d);
            Hologram hologram2 = HologramAPI.createHologram(holo2, plugin.getLocale("building.shop.hologram"));
            hologram2.spawn();

            holograms.add(hologram1);
            holograms.add(hologram2);
        }
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public List<Location> getBuildingBlocks() {
        return buildingBlocks;
    }

    public Location getCenterBlock() {
        return centerBlock;
    }

    public boolean canBuild(BoundingBox other) {
        return !bounds.intercepts(other);

    }

    public void clearHolograms(){
        holograms.stream().filter(Hologram::isSpawned).forEach(Hologram::despawn);

        holograms.clear();
    }

    public void explode(){
        if(centerBlock != null){
            centerBlock.getWorld().playSound(centerBlock, Sound.EXPLODE, 1.0f, 1.0f);
        }

        for(Location loc : buildingBlocks){
            if(loc.equals(centerBlock)) continue;

            Block b = loc.getBlock();
            if(b.getType() == Material.AIR) continue;

            Material oldType = b.getType();
            byte oldData = b.getData();

            b.setType(Material.AIR);

            if(!oldType.isSolid()) continue;

            FallingBlock block = loc.getWorld().spawnFallingBlock(loc, oldType, oldData);
            float xv = -0.3f + (plugin.getRandom().nextFloat() * 0.6f);
            float yv = (plugin.getRandom().nextFloat() * 0.5f);
            float zv = -0.3f + (plugin.getRandom().nextFloat() * 0.6f);

            block.setVelocity(new Vector(xv, yv, zv));
        }

        plugin.getGameInstance().removeBuilding(this);
    }

    public void remove(){
        for(Location loc : buildingBlocks){
            if(loc.equals(centerBlock)) continue;

            oldBlocks.get(loc).update(true, false);
        }

        plugin.getGameInstance().removeBuilding(this);
        if(centerBlock != null) oldBlocks.get(centerBlock).update(true, false);
    }
}

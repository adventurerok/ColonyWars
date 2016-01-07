package com.ithinkrok.minigames.schematic;

import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.util.BoundingBox;
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
import java.util.Random;

/**
 * Created by paul on 07/01/16.
 */
public class PastedSchematic implements SchematicPaster.BoundsChecker {

    private static Random random = new Random();

    private String name;
    private List<Location> buildingBlocks;
    private Map<Location, BlockState> oldBlocks;
    private Location centerBlock;
    private BoundingBox bounds;
    private boolean finished;
    private int rotation;

    private List<Hologram> holograms = new ArrayList<>();
    private GameTask buildTask;

    public PastedSchematic(String name, Location centerBlock, BoundingBox bounds, int rotation,
                           List<Location> buildingBlocks, Map<Location, BlockState> oldBlocks) {
        this.name = name;
        this.centerBlock = centerBlock;
        this.bounds = bounds;
        this.rotation = rotation;
        this.buildingBlocks = buildingBlocks;
        this.oldBlocks = oldBlocks;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public String getName() {
        return name;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public List<Location> getBuildingBlocks() {
        return buildingBlocks;
    }

    public Location getCenterBlock() {
        return centerBlock;
    }

    @Override
    public boolean canPaste(BoundingBox bounds) {
        return !this.bounds.interceptsXZ(bounds);
    }

    public void removed() {
        if(buildTask != null && buildTask.getTaskState() == GameTask.TaskState.SCHEDULED) buildTask.cancel();

        holograms.forEach(HologramAPI::removeHologram);
    }

    public void addHologram(Hologram hologram){
        holograms.add(hologram);
    }

    public void removeHologram(Hologram hologram){
        holograms.remove(hologram);
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
            float xv = -0.3f + (random.nextFloat() * 0.6f);
            float yv = (random.nextFloat() * 0.5f);
            float zv = -0.3f + (random.nextFloat() * 0.6f);

            block.setVelocity(new Vector(xv, yv, zv));
        }

        //plugin.getGameInstance().removeBuilding(this);
    }

    public void remove(){
        for(Location loc : buildingBlocks){
            if(loc.equals(centerBlock)) continue;

            oldBlocks.get(loc).update(true, false);
        }

        //plugin.getGameInstance().removeBuilding(this);
        if(centerBlock != null) oldBlocks.get(centerBlock).update(true, false);
    }

    public GameTask getBuildTask() {
        return buildTask;
    }

    public void setBuildTask(GameTask buildTask) {
        this.buildTask = buildTask;
    }
}

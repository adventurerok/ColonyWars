package com.ithinkrok.minigames.schematic;

import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.schematic.event.SchematicDestroyedEvent;
import com.ithinkrok.minigames.schematic.event.SchematicFinishedEvent;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.util.BoundingBox;
import com.ithinkrok.minigames.util.EventExecutor;
import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Created by paul on 07/01/16.
 */
public class PastedSchematic implements SchematicPaster.BoundsChecker {

    private static long nextIdentifier = 0;

    private static Random random = new Random();

    private final long identifier = nextIdentifier++;

    private String name;
    private List<Location> buildingBlocks;
    private Map<Location, BlockState> oldBlocks;
    private Location centerBlock;
    private BoundingBox bounds;
    private boolean finished;
    private int rotation;

    private GameMap map;

    private List<Listener> listeners = new ArrayList<>();
    private List<Hologram> holograms = new ArrayList<>();
    private GameTask buildTask;

    public PastedSchematic(String name, GameMap map, Location centerBlock, BoundingBox bounds, int rotation,
                           List<Location> buildingBlocks, Map<Location, BlockState> oldBlocks) {
        this.name = name;
        this.map = map;
        this.centerBlock = centerBlock;
        this.bounds = bounds;
        this.rotation = rotation;
        this.buildingBlocks = buildingBlocks;
        this.oldBlocks = oldBlocks;

        map.addPastedSchematic(this);
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

    public int getRotation() {
        return rotation;
    }

    public void addListeners(Collection<Listener> listeners) {
        this.listeners.addAll(listeners);
    }

    public void setFinished() {
        this.finished = true;

        SchematicFinishedEvent destroyedEvent = new SchematicFinishedEvent(this);
        EventExecutor.executeEvent(destroyedEvent, listeners);
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
        map.removePastedSchematic(this);

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
        removed();

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

        SchematicDestroyedEvent destroyedEvent = new SchematicDestroyedEvent(this);
        EventExecutor.executeEvent(destroyedEvent, listeners);
    }

    public void remove(){
        removed();

        for(Location loc : buildingBlocks){
            if(loc.equals(centerBlock)) continue;

            oldBlocks.get(loc).update(true, false);
        }

        SchematicDestroyedEvent destroyedEvent = new SchematicDestroyedEvent(this);
        EventExecutor.executeEvent(destroyedEvent, listeners);
        if(centerBlock != null) oldBlocks.get(centerBlock).update(true, false);
    }

    public GameTask getBuildTask() {
        return buildTask;
    }

    public void setBuildTask(GameTask buildTask) {
        this.buildTask = buildTask;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PastedSchematic that = (PastedSchematic) o;

        if (identifier != that.identifier) return false;
        return name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = (int) (identifier ^ (identifier >>> 32));
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PastedSchematic{" +
                "name='" + name + '\'' +
                ", identifier=" + identifier +
                ", finished=" + finished +
                '}';
    }
}

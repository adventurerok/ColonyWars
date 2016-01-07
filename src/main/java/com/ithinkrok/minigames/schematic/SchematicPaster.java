package com.ithinkrok.minigames.schematic;

import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskScheduler;
import com.ithinkrok.minigames.util.BoundingBox;
import de.inventivegames.hologram.Hologram;
import de.inventivegames.hologram.HologramAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by paul on 07/01/16.
 */
public class SchematicPaster {

    private static final DecimalFormat percentFormat = new DecimalFormat("00%");

    public interface BoundsChecker {
        boolean canPaste(BoundingBox bounds);
    }

    public static class SchematicOptions {
        private Material centerBlockType;
        private boolean progressHologram = false;
        private int buildSpeed = 2;
        
        private Map<Material, Material> replaceMaterials = new HashMap<>();
        private DyeColor overrideDyeColor;
        private List<Listener> defaultListeners = new ArrayList<>();


        public Material getCenterBlockType() {
            return this.centerBlockType;
        }

        public SchematicOptions withCenterBlockType(Material centerBlockType) {
            this.centerBlockType = centerBlockType;
            return this;
        }

        public boolean getProgressHologram() {
            return this.progressHologram;
        }

        public SchematicOptions withProgressHologram(boolean progressHologram) {
            this.progressHologram = progressHologram;
            return this;
        }

        public int getBuildSpeed() {
            return this.buildSpeed;
        }

        public SchematicOptions withBuildSpeed(int buildSpeed) {
            this.buildSpeed = buildSpeed;
            return this;
        }

        public Map<Material, Material> getReplaceMaterials() {
            return this.replaceMaterials;
        }

        public SchematicOptions withReplaceMaterials(Map<Material, Material> replaceMaterials) {
            this.replaceMaterials.putAll(replaceMaterials);
            return this;
        }

        public SchematicOptions withReplaceMaterial(Material from, Material to) {
            this.replaceMaterials.put(from, to);
            return this;
        }

        public DyeColor getOverrideDyeColor() {
            return this.overrideDyeColor;
        }

        public SchematicOptions withOverrideDyeColor(DyeColor overrideDyeColor) {
            this.overrideDyeColor = overrideDyeColor;
            return this;
        }

        public SchematicOptions withDefaultListener(Listener defaultListener) {
            this.defaultListeners.add(defaultListener);
            return this;
        }
    }

    public static PastedSchematic pasteSchematic(Schematic schemData, Location loc, BoundsChecker boundsChecker,
                                         SchematicResolver schematicResolver, int rotation, SchematicOptions options) {
        return doSchematic(schemData, loc, boundsChecker, schematicResolver, rotation, options, null);
    }

    private static PastedSchematic doSchematic(Schematic schemData, Location loc, BoundsChecker boundsChecker, SchematicResolver
            schematicResolver, int rotation, SchematicOptions options,
                                               TaskScheduler taskScheduler) {
        SchematicRotation schem = schemData.getSchematicRotation(rotation);

        BoundingBox bounds = schem.calcBounds(schematicResolver, loc);

        if(!boundsChecker.canPaste(bounds)) return null;

        List<Location> locations = new ArrayList<>();
        Location centerBlock = null;
        HashMap<Location, BlockState> oldBlocks = new HashMap<>();

        BlockState oldState;

        for (int x = 0; x < schem.getWidth(); ++x) {
            for (int y = 0; y < schem.getHeight(); ++y) {
                for (int z = 0; z < schem.getLength(); ++z) {
                    Location l = new Location(loc.getWorld(), x + loc.getX() + schem.getOffsetX(),
                            y + loc.getY() + schem.getOffsetY(), z + loc.getZ() + schem.getOffsetZ());

                    oldState = l.getBlock().getState();

                    int bId = schem.getBlock(x, y, z);
                    if (bId == 0) continue;

                    if (bId == options.centerBlockType.getId()) centerBlock = l;

                    locations.add(l);

                    oldBlocks.put(l, oldState);
                }
            }
        }

        Collections.sort(locations, (o1, o2) -> {
            if (o1.getY() != o2.getY()) return Double.compare(o1.getY(), o2.getY());
            if (o1.getX() != o2.getX()) return Double.compare(o1.getX(), o2.getX());

            return Double.compare(o1.getZ(), o2.getZ());
        });

        PastedSchematic result = new PastedSchematic(schemData.getName(), centerBlock, bounds, rotation, locations,
                oldBlocks);
        result.addListeners(options.defaultListeners);

        SchematicBuilderTask builderTask = new SchematicBuilderTask(loc, result, schem, options);

        if(taskScheduler != null) {
            result.setBuildTask(builderTask.schedule(taskScheduler));
        } else {
            options.buildSpeed = -1;
            builderTask.run(null);
        }

        return result;
    }

    public static byte rotateData(Material type, int rotation, byte data) {
        switch (type) {
            case ACACIA_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case BRICK_STAIRS:
            case COBBLESTONE_STAIRS:
            case DARK_OAK_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case NETHER_BRICK_STAIRS:
            case QUARTZ_STAIRS:
            case RED_SANDSTONE_STAIRS:
            case SANDSTONE_STAIRS:
            case SMOOTH_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case WOOD_STAIRS:
                return (byte) ((data & 0x4) | Facing.rotateStairs(data & 3, rotation));
            case LADDER:
            case CHEST:
            case TRAPPED_CHEST:
            case FURNACE:
                return (byte) Facing.rotateLadderFurnaceChest(data, rotation);
            case LOG:
            case LOG_2:
                return (byte) ((data & 3) | Facing.rotateLogs(data & 12, rotation));
            default:
                return data;
        }

    }

    private static class SchematicBuilderTask implements GameRunnable {

        int index = 0;

        Location origin;
        Hologram hologram;
        private PastedSchematic building;
        private SchematicRotation schem;
        private SchematicOptions options;
        private int buildSpeed;

        private boolean clearedOrigin = false;

        public SchematicBuilderTask(Location origin, PastedSchematic building, SchematicRotation schem,
                                    SchematicOptions options) {
            this.origin = origin;
            this.building = building;
            this.schem = schem;
            this.options = options;

            this.buildSpeed = options.buildSpeed;

            if(options.progressHologram) {
                Location holoLoc;
                if (building.getCenterBlock() != null) holoLoc = building.getCenterBlock().clone().add(0.5d, 1.5d, 0.5d);
                else holoLoc = origin.clone().add(0.5d, 1.5d, 0.5d);

                hologram = HologramAPI.createHologram(holoLoc, "Building: 0%");

                hologram.spawn();

                building.addHologram(hologram);
            }
        }

        @Override
        public void run(GameTask task) {
            int count = 0;

            if (!clearedOrigin) {
                origin.getBlock().setType(Material.AIR);
                clearedOrigin = true;
            }

            List<Location> locations = building.getBuildingBlocks();

            while (index < locations.size()) {
                Location loc = locations.get(index);

                int x = loc.getBlockX() - origin.getBlockX() - schem.getOffsetX();
                int y = loc.getBlockY() - origin.getBlockY() - schem.getOffsetY();
                int z = loc.getBlockZ() - origin.getBlockZ() - schem.getOffsetZ();


                Material mat = Material.getMaterial(schem.getBlock(x, y, z));
                byte bData = schem.getData(x, y, z);

                Block block = loc.getBlock();


                Material replaceWith = options.replaceMaterials.get(mat);
                if(replaceWith != null) mat = replaceWith;

                if(options.overrideDyeColor != null) {
                    if(mat == Material.WOOL || mat == Material.STAINED_CLAY || mat == Material.STAINED_GLASS || mat
                            == Material.STAINED_GLASS_PANE) {
                        bData = options.overrideDyeColor.getWoolData();
                    }
                }

                block.setTypeIdAndData(mat.getId(), rotateData(mat, schem.getRotation(), bData), false);

                ++index;

                ++count;
                if (buildSpeed != -1 && count > buildSpeed) {
                    loc.getWorld().playEffect(loc, Effect.STEP_SOUND, mat);
                    if(options.progressHologram) {
                        hologram.setText("Building: " + percentFormat.format((double) index / (double) locations.size()));
                    }
                    return;
                }
            }

            building.setBuildTask(null);
            if(task != null) task.finish();

            if(options.progressHologram) {
                HologramAPI.removeHologram(hologram);
                building.removeHologram(hologram);
            }

            if (building.getCenterBlock() != null) {
                building.getCenterBlock().getWorld().playSound(building.getCenterBlock(), Sound.LEVEL_UP, 1.0f, 1.0f);
            }

            building.setFinished();

            building = null;
        }

        public GameTask schedule(TaskScheduler scheduler) {
            return scheduler.repeatInFuture(this, 1, 1);
        }
    }
}

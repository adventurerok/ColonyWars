package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.BentEarth;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserTeamBuildingBuiltEvent;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by paul on 15/11/15.
 * <p>
 * Handles the priest class
 */
public class PriestClass extends ClassItemClassHandler {

    public PriestClass(WarsPlugin plugin, FileConfiguration config) {
        super(new ClassItem(Material.DIAMOND_BOOTS, plugin.getLocale("items.healing-scroll.name"))
                        .withUpgradeBuildings(Buildings.CATHEDRAL).withUnlockOnBuildingBuild(true)
                        .withRightClickAction(new HealingScroll(plugin))
                        .withRightClickCooldown("healing", new LinearCalculator(240, -90),
                                plugin.getLocale("cooldowns.healing.finished")).withUpgradables(
                        new ClassItem.Upgradable("healing", plugin.getLocale("upgrades.healing-scroll.name"), 2,
                                configArrayCalculator(config, "costs.priest.healing", 2))),
                new ClassItem(Material.GOLD_CHESTPLATE, plugin.getLocale("items.earth-bender.name"))
                        .withUpgradeBuildings(Buildings.CATHEDRAL).withUnlockOnBuildingBuild(true)
                        .withRightClickAction(new EarthBenderRightClick())
                        .withRightClickCooldown("bender", new LinearCalculator(45, -15),
                                plugin.getLocale("cooldowns.bender.finished"))
                        .withLeftClickAction(new EarthBenderLeftClick()).withUpgradables(
                        new ClassItem.Upgradable("bender", plugin.getLocale("upgrades.earth-bender.name"), 2,
                                configArrayCalculator(config, "costs.priest.bender", 2))),
                new ClassItem(Material.GOLD_LEGGINGS, plugin.getLocale("items.cross.name"))
                        .withUpgradeBuildings(Buildings.CATHEDRAL).withUnlockOnBuildingBuild(true).withWeaponModifier(
                        new ClassItem.WeaponModifier("cross").withDamageCalculator(new LinearCalculator(2, 1)))
                        .withUpgradables(new ClassItem.Upgradable("cross", plugin.getLocale("upgrades.cross.name"), 2,
                                configArrayCalculator(config, "costs.priest.cross", 2))));
    }


    private static class HealingScroll implements ClassItem.InteractAction {

        private WarsPlugin plugin;

        public HealingScroll(WarsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onInteractWorld(UserInteractEvent event) {
            for (Player p : event.getTeam().getPlayers()) {
                p.setHealth(plugin.getMaxHealth());
            }

            return true;
        }
    }

    private static class EarthBenderRightClick implements ClassItem.InteractAction {

        @Override
        public boolean onInteractWorld(UserInteractEvent event) {
            Block target = event.getUser().rayTraceBlocks(200);
            if (target == null) return true;
            event.getUser().setUpgradeLevel("bending", 0);

            int maxDist = 3 * 3;

            Collection<Entity> nearby = target.getWorld().getNearbyEntities(target.getLocation(), 3, 3, 3);
            List<Player> riders = new ArrayList<>();

            for (Entity near : nearby) {
                if (!(near instanceof Player)) continue;
                riders.add((Player) near);
            }

            List<FallingBlock> fallingBlockList = new ArrayList<>();

            for (int y = -3; y <= 3; ++y) {
                int ys = y * y;
                for (int x = -3; x <= 3; ++x) {
                    int xs = x * x;
                    for (int z = -3; z <= 3; ++z) {
                        int zs = z * z;
                        if (xs + ys + zs > maxDist) continue;
                        Block block = target.getRelative(x, y, z);

                        if (block.getType() == Material.AIR || block.getType() == Material.OBSIDIAN ||
                                block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER) continue;
                        if (block.isLiquid()) {
                            block.setType(Material.AIR);
                            continue;
                        }
                        BlockState oldState = block.getState();
                        block.setType(Material.AIR);

                        FallingBlock falling = block.getWorld()
                                .spawnFallingBlock(block.getLocation(), oldState.getType(), oldState.getRawData());

                        falling.setVelocity(new Vector(0, 1.5, 0));
                        fallingBlockList.add(falling);
                    }
                }
            }

            BentEarth bentEarth = new BentEarth(fallingBlockList, riders);
            event.getUser().setBentEarth(bentEarth);

            return true;
        }
    }

    private static class EarthBenderLeftClick implements ClassItem.InteractAction {

        @Override
        public boolean onInteractWorld(UserInteractEvent event) {
            BentEarth bent = event.getUser().getBentEarth();
            if (bent == null) return true;
            if (event.getUser().getUpgradeLevel("bending") > 4) return true;
            Vector add = event.getPlayer().getLocation().getDirection();
            bent.addVelocity(add);
            event.getUser().setUpgradeLevel("bending", event.getUser().getUpgradeLevel("bending") + 1);

            return true;
        }
    }
}

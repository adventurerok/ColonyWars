package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.BentEarth;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.PlayerUtils;
import com.ithinkrok.mccw.util.item.TeamCompass;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
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

    public PriestClass(WarsPlugin plugin, PlayerClass playerClass) {
        super(new ClassItem(plugin, playerClass.getName(), Material.DIAMOND_BOOTS, "items.healing-scroll.name")
                        .withUpgradeBuildings(Buildings.CATHEDRAL).withUnlockOnBuildingBuild(true)
                        .withRightClickAction(new HealingScroll(plugin))
                        .withRightClickCooldown("healing", "healing", new LinearCalculator(240, -90),
                                "cooldowns.healing.finished").withUpgradables(
                        new ClassItem.Upgradable("healing", "upgrades.healing-scroll.name", 2)),
                new ClassItem(plugin, playerClass.getName(), Material.GOLD_CHESTPLATE, "items.earth-bender.name")
                        .withUpgradeBuildings(Buildings.CATHEDRAL).withUnlockOnBuildingBuild(true)
                        .withRightClickAction(new EarthBenderRightClick())
                        .withRightClickCooldown("bender", "bender", new LinearCalculator(50, -15),
                                "cooldowns.bender.finished").withLeftClickAction(new EarthBenderLeftClick())
                        .withUpgradables(new ClassItem.Upgradable("bender", "upgrades.earth-bender.name", 2)),
                new ClassItem(plugin, playerClass.getName(), Material.GOLD_LEGGINGS, "items.cross.name")
                        .withUpgradeBuildings(Buildings.CATHEDRAL).withUnlockOnBuildingBuild(true).withWeaponModifier(
                        new ClassItem.WeaponModifier("cross").withDamageCalculator(new LinearCalculator(2, 1)))
                        .withUpgradables(new ClassItem.Upgradable("cross", "upgrades.cross.name", 2)),
                TeamCompass.createTeamCompass(plugin));
    }


    private static class HealingScroll implements ClassItem.InteractAction {

        private WarsPlugin plugin;

        public HealingScroll(WarsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onInteractWorld(UserInteractEvent event) {
            for (User user : event.getTeam().getUsers()) {
                user.getPlayer().setHealth(plugin.getMaxHealth());
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

            target.getWorld().playSound(target.getLocation(), Sound.WITHER_SPAWN, 1.0f, 2.0f);

            int maxDist = 3 * 3;

            Collection<Entity> nearby = target.getWorld().getNearbyEntities(target.getLocation(), 3, 3, 3);
            List<LivingEntity> riders = new ArrayList<>();

            for (Entity near : nearby) {
                if (!(near instanceof LivingEntity)) continue;


                User other = PlayerUtils.getUserFromEntity(event.getUser().getPlugin(), near);
                if(other != null) {
                    if(!other.isInGame()) continue;
                    else if(other.getTeamColor() != event.getUser().getTeamColor()) {
                        ((LivingEntity) near).damage(10);
                    }
                }

                riders.add((LivingEntity) near);
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
            event.getUser().setMetadata("bentEarth", bentEarth, true);

            return true;
        }
    }

    private static class EarthBenderLeftClick implements ClassItem.InteractAction {

        @Override
        public boolean onInteractWorld(UserInteractEvent event) {
            Object bentObject = event.getUser().getMetadata("bentEarth");
            if(bentObject == null || !(bentObject instanceof BentEarth)) return true;
            BentEarth bent = (BentEarth) bentObject;
            if (event.getUser().getUpgradeLevel("bending") > 4) return true;
            Vector add = event.getPlayer().getLocation().getDirection();
            bent.addVelocity(add);
            bent.playKnockSound(event.getPlayer());
            event.getUser().setUpgradeLevel("bending", event.getUser().getUpgradeLevel("bending") + 1);

            return true;
        }
    }
}

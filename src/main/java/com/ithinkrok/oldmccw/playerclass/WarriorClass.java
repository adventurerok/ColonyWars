package com.ithinkrok.oldmccw.playerclass;

import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.oldmccw.enumeration.PlayerClass;
import com.ithinkrok.oldmccw.event.UserInteractEvent;
import com.ithinkrok.oldmccw.playerclass.items.ArrayCalculator;
import com.ithinkrok.oldmccw.playerclass.items.ClassItem;
import com.ithinkrok.oldmccw.playerclass.items.LinearCalculator;
import com.ithinkrok.oldmccw.strings.Buildings;
import com.ithinkrok.oldmccw.util.item.TeamCompass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;

/**
 * Created by paul on 15/11/15.
 * <p>
 * Handles the warrior class
 */
public class WarriorClass extends ClassItemClassHandler {

    public WarriorClass(WarsPlugin plugin, PlayerClass playerClass) {
        super(new ClassItem(plugin, playerClass.getName(), Material.IRON_SWORD).withUpgradeBuildings(Buildings.BLACKSMITH)
                        .withUnlockOnBuildingBuild(true).withEnchantmentEffects(
                        new ClassItem.EnchantmentEffect(Enchantment.DAMAGE_ALL, "sharpness",
                                new ArrayCalculator(0, 1, 3)),
                        new ClassItem.EnchantmentEffect(Enchantment.KNOCKBACK, "knockback", new LinearCalculator(0, 1)))
                        .withUpgradables(new ClassItem.Upgradable("sharpness", "upgrades.sharpness.name", 2),
                                new ClassItem.Upgradable("knockback", "upgrades.knockback.name", 2)),
                new ClassItem(plugin, playerClass.getName(), Material.GOLD_HELMET, "items.wolf-wand.name")
                        .withUpgradeBuildings(Buildings.BLACKSMITH).withUnlockOnBuildingBuild(true)
                        .withRightClickAction(new WolfWand())
                        .withRightClickCooldown("wolf", "wolf", new LinearCalculator(120, -30),
                                "cooldowns.wolf.finished").withUpgradables(
                        new ClassItem.Upgradable("wolf", "upgrades.wolf-wand.name", 2)),
                TeamCompass.createTeamCompass(plugin));
    }

    private static class WolfWand implements ClassItem.InteractAction {

        @Override
        public boolean onInteractWorld(UserInteractEvent event) {
            if (!event.hasBlock()) return false;
            if(!event.getUser().isPlayer()) return false;

            Location target = event.getClickedBlock().getLocation();
            BlockFace face = event.getBlockFace();
            target = target.clone().add(face.getModX(), face.getModY(), face.getModZ());

            Wolf wolf = (Wolf) target.getWorld().spawnEntity(target, EntityType.WOLF);
            wolf.setCollarColor(event.getUser().getTeamColor().getDyeColor());
            wolf.setOwner(event.getUser().getPlayer());
            wolf.setHealth(wolf.getMaxHealth());

            return true;
        }
    }

}
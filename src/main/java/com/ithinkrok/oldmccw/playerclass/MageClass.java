package com.ithinkrok.oldmccw.playerclass;

import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.oldmccw.enumeration.PlayerClass;
import com.ithinkrok.oldmccw.event.UserAttackEvent;
import com.ithinkrok.oldmccw.event.UserInteractEvent;
import com.ithinkrok.oldmccw.event.UserTeamBuildingBuiltEvent;
import com.ithinkrok.oldmccw.inventory.ItemBuyable;
import com.ithinkrok.oldmccw.playerclass.items.ClassItem;
import com.ithinkrok.oldmccw.playerclass.items.LinearCalculator;
import com.ithinkrok.oldmccw.strings.Buildings;
import com.ithinkrok.oldmccw.util.item.InventoryUtils;
import com.ithinkrok.oldmccw.util.item.TeamCompass;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionType;

/**
 * Created by paul on 12/11/15.
 * <p>
 * Handles the Mage class
 */
public class MageClass extends ClassItemClassHandler {


    public MageClass(WarsPlugin plugin, PlayerClass playerClass) {
        super(new ClassItem(plugin, playerClass.getName(), Material.DIAMOND_CHESTPLATE, "items.ender-wand.name")
                        .withUpgradeBuildings(Buildings.MAGETOWER).withUnlockOnBuildingBuild(true)
                        .withRightClickAction(new EnderWand())
                        .withRightClickCooldown("ender", "ender", new LinearCalculator(45, -15),
                                "cooldowns.ender.finished").withUpgradables(
                        new ClassItem.Upgradable("ender", "upgrades.ender-wand.name", 2)),
                new ClassItem(plugin, playerClass.getName(), Material.DIAMOND_LEGGINGS, "items.lightning-wand.name")
                        .withUpgradeBuildings(Buildings.MAGETOWER).withUnlockOnBuildingBuild(true)
                        .withRightClickAction(new LightningWand(plugin))
                        .withRightClickCooldown("lightning", "lightning", new LinearCalculator(50, -20),
                                "cooldowns.lightning.finished").withUpgradables(
                        new ClassItem.Upgradable("lightning", "upgrades.lightning-wand.name", 2)),
                TeamCompass.createTeamCompass(plugin));

        addExtraBuyables(new ItemBuyable(InventoryUtils.createPotion(PotionType.INSTANT_DAMAGE, 1, true, false, 32),
                        Buildings.MAGETOWER, plugin.getWarsConfig().getClassItemCost(playerClass, "harming"), true),
                new ItemBuyable(InventoryUtils.createPotion(PotionType.INSTANT_HEAL, 1, true, false, 32),
                        Buildings.MAGETOWER, plugin.getWarsConfig().getClassItemCost(playerClass, "healing"), true));

//        addExtraBuyables(new ItemBuyable(InventoryUtils.createPotion(PotionType.STRENGTH, 1, true, false, 1),
//                Buildings.MAGETOWER, plugin.getWarsConfig().getClassItemCost(playerClass, "strength"), true));
    }

    @Override
    public void onBuildingBuilt(UserTeamBuildingBuiltEvent event) {
        super.onBuildingBuilt(event);

        if (!Buildings.MAGETOWER.equals(event.getBuilding().getBuildingName())) return;

        PlayerInventory inv = event.getUserInventory();
        inv.addItem(InventoryUtils.createPotion(PotionType.INSTANT_HEAL, 1, false, false, 16));
        inv.addItem(InventoryUtils.createPotion(PotionType.INSTANT_DAMAGE, 1, true, false, 16));
    }

    private static class EnderWand implements ClassItem.InteractAction {

        @Override
        public boolean onInteractWorld(UserInteractEvent event) {
            event.getUser().launchProjectile(EnderPearl.class);
            event.getUser().playSound(event.getUser().getLocation(), Sound.SHOOT_ARROW, 1.0f, 1.0f);

            return true;
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {
        super.onUserAttack(event);

        if(event.getDamageCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setDamage(event.getDamage() * 2.8);
        }
    }

    private static class LightningWand implements ClassItem.InteractAction {

        private WarsPlugin plugin;

        public LightningWand(WarsPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onInteractWorld(UserInteractEvent event) {
            Block target = event.getUser().rayTraceBlocks(200);
            if (target == null) return false;
            LightningStrike strike = event.getUser().getLocation().getWorld().strikeLightning(target.getLocation());

            strike.setMetadata("striker", new FixedMetadataValue(plugin, event.getUser().getUniqueId()));
            return true;
        }
    }
}

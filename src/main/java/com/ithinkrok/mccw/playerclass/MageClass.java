package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.event.UserAttackEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserTeamBuildingBuiltEvent;
import com.ithinkrok.mccw.inventory.ItemBuyable;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.item.InventoryUtils;
import com.ithinkrok.mccw.util.item.TeamCompass;
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
                        .withRightClickCooldown("lightning", "lightning", new LinearCalculator(45, -15),
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
            event.getPlayer().launchProjectile(EnderPearl.class);
            event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.SHOOT_ARROW, 1.0f, 1.0f);

            return true;
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {
        super.onUserAttack(event);

        if(event.getDamageCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setDamage(event.getDamage() * 4);
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
            LightningStrike strike = event.getPlayer().getLocation().getWorld().strikeLightning(target.getLocation());

            strike.setMetadata("striker", new FixedMetadataValue(plugin, event.getPlayer().getUniqueId()));
            return true;
        }
    }
}

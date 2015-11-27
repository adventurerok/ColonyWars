package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserTeamBuildingBuiltEvent;
import com.ithinkrok.mccw.inventory.ItemBuyable;
import com.ithinkrok.mccw.playerclass.items.ClassItem;
import com.ithinkrok.mccw.playerclass.items.LinearCalculator;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import com.ithinkrok.mccw.util.TeamCompass;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LightningStrike;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionType;

/**
 * Created by paul on 12/11/15.
 * <p>
 * Handles the Mage class
 */
public class MageClass extends ClassItemClassHandler {


    public MageClass(WarsPlugin plugin, FileConfiguration config) {
        super(new ClassItem(plugin.getLangFile(), Material.DIAMOND_CHESTPLATE, "items.ender-wand.name")
                        .withUpgradeBuildings(Buildings.MAGETOWER).withUnlockOnBuildingBuild(true)
                        .withRightClickAction(new EnderWand())
                        .withRightClickCooldown("ender", "ender", new LinearCalculator(45, -15),
                                "cooldowns.ender.finished").withUpgradables(
                        new ClassItem.Upgradable("ender", "upgrades.ender-wand.name", 2,
                                configArrayCalculator(config, "costs.mage.ender", 2))),
                new ClassItem(plugin.getLangFile(), Material.DIAMOND_LEGGINGS, "items.lightning-wand.name")
                        .withUpgradeBuildings(Buildings.MAGETOWER).withUnlockOnBuildingBuild(true)
                        .withRightClickAction(new LightningWand(plugin))
                        .withRightClickCooldown("lightning", "lightning", new LinearCalculator(45, -15),
                                "cooldowns.lightning.finished").withUpgradables(
                        new ClassItem.Upgradable("lightning", "upgrades.lightning-wand.name", 2,
                                configArrayCalculator(config, "costs.mage.lightning", 2))),
                TeamCompass.createTeamCompass(plugin, config));

        addExtraBuyables(new ItemBuyable(InventoryUtils.createPotion(PotionType.INSTANT_DAMAGE, 1, true, false, 32),
                        Buildings.MAGETOWER, config.getInt("costs.mage.harming"), true),
                new ItemBuyable(InventoryUtils.createPotion(PotionType.INSTANT_HEAL, 1, true, false, 32),
                        Buildings.MAGETOWER, config.getInt("costs.mage.healing"), true));
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

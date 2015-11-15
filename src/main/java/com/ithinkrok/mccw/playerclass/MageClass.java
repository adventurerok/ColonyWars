package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserAttackEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.ItemBuyable;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionType;

/**
 * Created by paul on 12/11/15.
 * <p>
 * Handles the Mage class
 */
public class MageClass extends BuyableInventory implements PlayerClassHandler {



    private final WarsPlugin plugin;

    public MageClass(WarsPlugin plugin, FileConfiguration config) {
        super(new ItemBuyable(InventoryUtils.createPotion(PotionType.INSTANT_DAMAGE, 1, true, false, 32),
                        Buildings.MAGETOWER, config.getInt("costs.mage.harming"), true),
                new ItemBuyable(InventoryUtils.createPotion(PotionType.INSTANT_HEAL, 1, true, false, 32),
                        Buildings.MAGETOWER, config.getInt("costs.mage.healing"), true), new UpgradeBuyable(
                        InventoryUtils
                                .createItemWithNameAndLore(Material.DIAMOND_CHESTPLATE, 1, 0, "Ender Wand Upgrade 1",
                                        "Cooldown: 30 seconds"), Buildings.MAGETOWER,
                        config.getInt("costs.mage.ender1"), "ender", 1), new UpgradeBuyable(InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_CHESTPLATE, 1, 0, "Ender Wand Upgrade 2",
                                "Cooldown: 15 seconds"), Buildings.MAGETOWER, config.getInt("costs.mage.ender2"),
                        "ender", 2), new UpgradeBuyable(InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_LEGGINGS, 1, 0, "Lightning Wand Upgrade 1",
                                "Cooldown: 20 seconds"), Buildings.MAGETOWER, config.getInt("costs.mage.lightning1"),
                        "lightning", 1), new UpgradeBuyable(InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_LEGGINGS, 1, 0, "Lightning Wand Upgrade 2",
                                "Cooldown: 10 seconds"), Buildings.MAGETOWER, config.getInt("costs.mage.lightning2"),
                        "lightning", 2));
        this.plugin = plugin;
    }


    @Override
    public void onBuildingBuilt(String buildingName, User user, Team team) {
        if (!Buildings.MAGETOWER.equals(buildingName)) return;

        PlayerInventory inv = user.getPlayer().getInventory();

        inv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.DIAMOND_LEGGINGS, 1, 0, "Lightning Wand", "Cooldown: 30 seconds"));
        inv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.DIAMOND_CHESTPLATE, 1, 0, "Ender Staff", "Cooldown: 45 seconds"));
        inv.addItem(InventoryUtils.createPotion(PotionType.INSTANT_HEAL, 1, false, false, 16));
        inv.addItem(InventoryUtils.createPotion(PotionType.INSTANT_DAMAGE, 1, true, false, 16));
    }

    @Override
    public void onGameBegin(User user, Team team) {

    }

    @Override
    public boolean onInteractWorld(UserInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
        ItemStack item = event.getItem();
        if (item == null) return false;

        User user = event.getUserClicked();

        switch (item.getType()) {
            case DIAMOND_CHESTPLATE:
                if (!user.startCoolDown("ender", 45 - 15 * user.getUpgradeLevel("ender"),
                        plugin.getLocale("ender-wand-cooldown"))) break;
                event.getPlayer().launchProjectile(EnderPearl.class);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.SHOOT_ARROW, 1.0f, 1.0f);
                break;
            case DIAMOND_LEGGINGS:
                if (!user.startCoolDown("lightning", 45 - 15 * user.getUpgradeLevel("lightning"),
                        plugin.getLocale("lightning-wand-cooldown"))) break;
                Block target = user.rayTraceBlocks(200);

                if (target == null) break;
                LightningStrike strike =
                        event.getPlayer().getLocation().getWorld().strikeLightning(target.getLocation());

                strike.setMetadata("striker", new FixedMetadataValue(plugin, event.getPlayer().getUniqueId()));
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        switch (event.getUpgradeName()) {
            case "ender":
                int enderCooldown = 45 - 15 * event.getUpgradeLevel();
                ItemStack enderWand = InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_CHESTPLATE, 1, 0, "Ender Wand",
                                "Cooldown: " + enderCooldown + " seconds");
                InventoryUtils.replaceItem(event.getUserInventory(), enderWand);
                break;
            case "lightning":
                int lightningCooldown = 30 - 10 * event.getUpgradeLevel();
                ItemStack lightningWand = InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_LEGGINGS, 1, 0, "Lightning Wand",
                                "Cooldown: " + lightningCooldown + " seconds");
                InventoryUtils.replaceItem(event.getUserInventory(), lightningWand);
                break;
        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {

    }
}

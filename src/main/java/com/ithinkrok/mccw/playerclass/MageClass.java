package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionType;

import java.util.HashSet;

/**
 * Created by paul on 12/11/15.
 * <p>
 * Handles the Mage class
 */
public class MageClass extends BuyableInventory implements PlayerClassHandler {

    private static final HashSet<Material> SEE_THROUGH = new HashSet<>();

    static {
        SEE_THROUGH.add(Material.AIR);
        SEE_THROUGH.add(Material.WATER);
        SEE_THROUGH.add(Material.STATIONARY_WATER);
    }

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
    public void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        if (!Buildings.MAGETOWER.equals(buildingName)) return;

        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        inv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.DIAMOND_LEGGINGS, 1, 0, "Lightning Wand", "Cooldown: 30 seconds"));
        inv.addItem(InventoryUtils
                .createItemWithNameAndLore(Material.DIAMOND_CHESTPLATE, 1, 0, "Ender Staff", "Cooldown: 45 seconds"));
        inv.addItem(InventoryUtils.createPotion(PotionType.INSTANT_HEAL, 1, false, false, 16));
        inv.addItem(InventoryUtils.createPotion(PotionType.INSTANT_DAMAGE, 1, true, false, 16));
    }

    @Override
    public void onGameBegin(PlayerInfo playerInfo, TeamInfo teamInfo) {

    }

    @Override
    public void onInteractWorld(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;

        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        switch (item.getType()) {
            case DIAMOND_CHESTPLATE:
                if (!playerInfo.startCoolDown("ender", 45 - 15 * playerInfo.getUpgradeLevel("ender"),
                        plugin.getLocale("ender-wand-cooldown"))) break;
                event.getPlayer().launchProjectile(EnderPearl.class);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.SHOOT_ARROW, 1.0f, 1.0f);
                break;
            case DIAMOND_LEGGINGS:
                if (!playerInfo.startCoolDown("lightning", 45 - 15 * playerInfo.getUpgradeLevel("lightning"),
                        plugin.getLocale("lightning-wand-cooldown"))) break;
                Block target = event.getPlayer().getTargetBlock(SEE_THROUGH, 200);

                if (target == null) break;
                LightningStrike strike =
                        event.getPlayer().getLocation().getWorld().strikeLightning(target.getLocation());

                strike.setMetadata("striker", new FixedMetadataValue(plugin, event.getPlayer().getUniqueId()));
                break;
        }
    }

    @Override
    public void onPlayerUpgrade(PlayerInfo playerInfo, String upgradeName, int upgradeLevel) {
        switch (upgradeName) {
            case "ender":
                int enderCooldown = 45 - 15 * upgradeLevel;
                ItemStack enderWand = InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_CHESTPLATE, 1, 0, "Ender Wand",
                                "Cooldown: " + enderCooldown + " seconds");
                InventoryUtils.replaceItem(playerInfo.getPlayer().getInventory(), enderWand);
                break;
            case "lightning":
                int lightningCooldown = 30 - 10 * upgradeLevel;
                ItemStack lightningWand = InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_LEGGINGS, 1, 0, "Lightning Wand",
                                "Cooldown: " + lightningCooldown + " seconds");
                InventoryUtils.replaceItem(playerInfo.getPlayer().getInventory(), lightningWand);
                break;
        }
    }
}

package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 05/11/15.
 * <p>
 * Handles the Scout class
 */
public class ScoutClass extends BuyableInventory implements PlayerClassHandler {


    private WarsPlugin plugin;

    public ScoutClass(WarsPlugin plugin, FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Sharpness Upgrade 1", null,
                        Enchantment.DAMAGE_ALL, 1), Buildings.LUMBERMILL, config.getInt("costs.scout.sharpness1"),
                "sharpness", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Sharpness Upgrade 2", null,
                        Enchantment.DAMAGE_ALL, 2), Buildings.LUMBERMILL, config.getInt("costs.scout.sharpness2"),
                "sharpness", 2), new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Knockback Upgrade 1", null,
                        Enchantment.KNOCKBACK, 1), Buildings.LUMBERMILL, config.getInt("costs.scout.knockback1"),
                "knockback", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithEnchantments(Material.WOOD_SWORD, 1, 0, "Knockback Upgrade 2", null,
                        Enchantment.KNOCKBACK, 2), Buildings.LUMBERMILL, config.getInt("costs.scout.knockback2"),
                "knockback", 2), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.COMPASS, 1, 0, "Player Compass",
                        "Locates the closest enemy player"), Buildings.CHURCH, config.getInt("costs.scout.compass"),
                "compass", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.CHAINMAIL_HELMET, 1, 0, "Regeneration Ability 2",
                        "Cooldown: 45 seconds"), Buildings.MAGETOWER, config.getInt("costs.scout.regen"), "regen", 1));
        this.plugin = plugin;
    }

    @Override
    public void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        PlayerInventory inv = playerInfo.getPlayer().getInventory();

        switch (buildingName) {
            case Buildings.LUMBERMILL:
                inv.addItem(new ItemStack(Material.WOOD_SWORD));
                break;
            case Buildings.MAGETOWER:
                inv.addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.CHAINMAIL_HELMET, 1, 0, "Regeneration Ability",
                                "Cooldown: 35 seconds"));

        }
    }

    @Override
    public void onGameBegin(PlayerInfo playerInfo, TeamInfo teamInfo) {
        playerInfo.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), true);
    }

    @Override
    public void onInteractWorld(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack item = event.getItem();
        if (item == null) return;

        switch (item.getType()) {
            case COMPASS:
                TeamColor exclude = plugin.getPlayerInfo(event.getPlayer()).getTeamColor();
                plugin.updateScoutCompass(item, event.getPlayer(), exclude);
                break;
            case CHAINMAIL_HELMET:
                PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());
                if (!playerInfo.startCoolDown("regen", 35 + 10 * playerInfo.getUpgradeLevel("regen"),
                        "Your regeneration ability has cooled down!")) return;

                event.getPlayer().addPotionEffect(
                        new PotionEffect(PotionEffectType.REGENERATION, 200, playerInfo.getUpgradeLevel("regen")));

                break;
        }


    }

    @Override
    public void onPlayerUpgrade(PlayerInfo playerInfo, String upgradeName, int upgradeLevel) {
        switch (upgradeName) {
            case "sharpness":
            case "knockback":
                ItemStack sword = new ItemStack(Material.WOOD_SWORD);
                InventoryUtils.enchantItem(sword, Enchantment.DAMAGE_ALL, playerInfo.getUpgradeLevel("sharpness"),
                        Enchantment.KNOCKBACK, playerInfo.getUpgradeLevel("knockback"));

                InventoryUtils.replaceItem(playerInfo.getPlayer().getInventory(), sword);

                break;
            case "compass":
                playerInfo.getPlayer().getInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.COMPASS, 1, 0, "Player Compass", "Oriented at: No One"));
                break;
            case "regen":
                ItemStack regen = InventoryUtils
                        .createItemWithNameAndLore(Material.CHAINMAIL_HELMET, 1, 0, "Regeneration Ability",
                                "Cooldown: 45 seconds");

                InventoryUtils.replaceItem(playerInfo.getPlayer().getInventory(), regen);
        }
    }
}

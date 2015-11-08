package com.ithinkrok.mccw.playerclass;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by paul on 07/11/15.
 * <p>
 * Handles the Cloaker class
 */
public class CloakerClass extends BuyableInventory implements PlayerClassHandler {


    private final WarsPlugin plugin;

    public CloakerClass(WarsPlugin plugin, FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.IRON_LEGGINGS, 1, 0, "Cloak", "Cooldown: 35 seconds",
                        "Invisibility: 15 seconds"), Buildings.MAGETOWER, config.getInt("costs.cloaker.cloak1"),
                "cloak", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.IRON_LEGGINGS, 1, 0, "Cloak", "Cooldown: 45 seconds",
                        "Invisibility: 25 seconds"), Buildings.MAGETOWER, config.getInt("costs.cloaker.cloak2"),
                "cloak", 2));
        this.plugin = plugin;
    }

    @Override
    public void onBuildingBuilt(String buildingName, PlayerInfo playerInfo, TeamInfo teamInfo) {
        switch (buildingName) {
            case Buildings.MAGETOWER:
                playerInfo.getPlayer().getInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.IRON_LEGGINGS, 1, 0, "Cloak", "Cooldown: 25 seconds",
                                "Invisibility: 10 seconds"));
                break;
        }
    }

    @Override
    public void onGameBegin(PlayerInfo playerInfo, TeamInfo teamInfo) {
        playerInfo.getPlayer()
                .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false), false);
        playerInfo.getPlayer()
                .addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 3, false, false), false);
    }

    @Override
    public void onInteractWorld(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || event.getItem().getType() != Material.IRON_LEGGINGS) return;

        PlayerInfo playerInfo = plugin.getPlayerInfo(event.getPlayer());

        if (playerInfo.isCoolingDown("cloaking")) {
            playerInfo.message(ChatColor.RED + "You are already cloaked!");
            return;
        }

        if (playerInfo.isCoolingDown("cloak")) {
            playerInfo.message(ChatColor.RED + "Please wait for the cloak to cool down!");
            return;
        }

        int cloak = 10;
        int cooldown = 25;

        switch (playerInfo.getUpgradeLevel("cloak")) {
            case 1:
                cloak = 15;
                cooldown = 35;
                break;
            case 2:
                cloak = 25;
                cooldown = 45;
                break;
        }

        playerInfo.startCoolDown("cloaking", cloak, null);

        plugin.cloak(event.getPlayer());

        event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, cloak * 20, 1, false, true), true);

        final int finalCooldown = cooldown;

        final int swirlTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            Location loc = playerInfo.getPlayer().getLocation();

            plugin.sendPlayersParticle(playerInfo.getPlayer(), loc, EnumWrappers.Particle.SPELL, 1);
        }, 20, 20);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.decloak(event.getPlayer());
            Bukkit.getScheduler().cancelTask(swirlTask);
            playerInfo.message(ChatColor.RED + "Your cloak has run out!");
            playerInfo.startCoolDown("cloak", finalCooldown, "Your cloak has cooled down!");
            event.getPlayer()
                    .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false),
                            true);
        }, cloak * 20);
    }

    @Override
    public void onPlayerUpgrade(PlayerInfo playerInfo, String upgradeName, int upgradeLevel) {
        switch (upgradeName) {
            case "cloak":
                int cooldown = 35;
                int invisibility = 15;
                if (upgradeLevel == 2) {
                    cooldown = 45;
                    invisibility = 25;
                }

                ItemStack cloak = InventoryUtils.createItemWithNameAndLore(Material.IRON_LEGGINGS, 1, 0, "Cloak",
                        "Cooldown: " + cooldown + " seconds", "Invisibility: " + invisibility + " seconds");

                InventoryUtils.replaceItem(playerInfo.getPlayer().getInventory(), cloak);
                break;
        }
    }
}

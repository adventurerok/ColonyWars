package com.ithinkrok.mccw.playerclass;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.BentEarth;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.event.UserAttackEvent;
import com.ithinkrok.mccw.event.UserInteractEvent;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.BuyableInventory;
import com.ithinkrok.mccw.inventory.UpgradeBuyable;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by paul on 15/11/15.
 * <p>
 * Handles the priest class
 */
public class PriestClass extends BuyableInventory implements PlayerClassHandler {

    private WarsPlugin plugin;

    public PriestClass(WarsPlugin plugin, FileConfiguration config) {
        super(new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.DIAMOND_BOOTS, 1, 0, "Healing Scroll Upgrade 1",
                        "Cooldown: 150 seconds"), Buildings.CATHEDRAL, config.getInt("costs.priest.healing1"),
                "healing", 1), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.DIAMOND_BOOTS, 1, 0, "Healing Scroll Upgrade 2",
                        "Cooldown: 60 seconds"), Buildings.CATHEDRAL, config.getInt("costs.priest.healing2"), "healing",
                2), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.GOLD_CHESTPLATE, 1, 0, "Earth Bender Upgrade 1",
                        "Cooldown: 30 seconds"), Buildings.CATHEDRAL, config.getInt("costs.priest.bender1"), "bender",
                1), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.GOLD_CHESTPLATE, 1, 0, "Earth Bender Upgrade 2",
                        "Cooldown: 15 seconds"), Buildings.CATHEDRAL, config.getInt("costs.priest.bender2"), "bender",
                2), new UpgradeBuyable(InventoryUtils
                .createItemWithNameAndLore(Material.GOLD_LEGGINGS, 1, 0, "Cross Upgrade 1", "Damage: 3.0 Hearts"),
                Buildings.CATHEDRAL, config.getInt("costs.priest.cross1"), "cross", 1), new UpgradeBuyable(
                InventoryUtils.createItemWithNameAndLore(Material.GOLD_LEGGINGS, 1, 0, "Cross Upgrade 2",
                        "Damage: 4.0 Hearts"), Buildings.CATHEDRAL, config.getInt("costs.priest.cross2"), "cross", 2));
        this.plugin = plugin;
    }

    @Override
    public void onBuildingBuilt(String buildingName, User user, Team team) {
        switch (buildingName) {
            case Buildings.CATHEDRAL:
                user.getPlayer().getInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_BOOTS, 1, 0, "Healing Scroll",
                                "Cooldown: 240 seconds"));
                user.getPlayer().getInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.GOLD_CHESTPLATE, 1, 0, "Earth Bender",
                                "Cooldown: 45 seconds"));
                user.getPlayer().getInventory().addItem(InventoryUtils
                        .createItemWithNameAndLore(Material.GOLD_LEGGINGS, 1, 0, "Cross", "Damage: 2.0 Hearts"));
                break;
        }
    }

    @Override
    public void onGameBegin(User user, Team team) {

    }

    @Override
    public boolean onInteractWorld(UserInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return false;

        switch (item.getType()) {
            case DIAMOND_BOOTS:
                return handleHealingScroll(event);
            case GOLD_CHESTPLATE:
                return handleEarthBender(event);
            default:
                return false;
        }
    }

    private boolean handleHealingScroll(UserInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return false;
        User user = event.getUserClicked();
        int cooldown = 240 - 90 * user.getUpgradeLevel("healing");
        if (!event.getUserClicked().startCoolDown("healing", cooldown, plugin.getLocale("healing-scroll-cooldown")))
            return true;

        for(Player p : user.getTeam().getPlayers()){
            p.setHealth(plugin.getMaxHealth());
        }

        return true;
    }

    private boolean handleEarthBender(UserInteractEvent event) {
        User user = event.getUserClicked();

        switch (event.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                earthBenderLeftClick(user);
                return true;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                int cooldown = 45 - 15 * user.getUpgradeLevel("bender");
                if (!user.startCoolDown("bender", cooldown, plugin.getLocale("earth-bender-cooldown"))) return true;
                Block target = user.rayTraceBlocks(200);
                if (target == null) return true;
                user.setUpgradeLevel("bending", 0);

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
                                    block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER)
                                continue;
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
                user.setBentEarth(bentEarth);
                return true;
            default:
                return false;
        }
    }

    private void earthBenderLeftClick(User user) {
        BentEarth bent = user.getBentEarth();
        if (bent == null) return;
        if (user.getUpgradeLevel("bending") > 4) return;
        Vector add = user.getPlayer().getLocation().getDirection();
        bent.addVelocity(add);
        user.setUpgradeLevel("bending", user.getUpgradeLevel("bending") + 1);
    }

    @Override
    public void onPlayerUpgrade(UserUpgradeEvent event) {
        switch (event.getUpgradeName()) {
            case "healing":
                int healingCooldown = 240 - 90 * event.getUpgradeLevel();
                ItemStack scroll = InventoryUtils
                        .createItemWithNameAndLore(Material.DIAMOND_BOOTS, 1, 0, "Healing Scroll",
                                "Cooldown: " + healingCooldown + " seconds");
                InventoryUtils.replaceItem(event.getUserInventory(), scroll);
                break;
            case "bender":
                int benderCooldown = 45 - 15 * event.getUpgradeLevel();
                ItemStack bender = InventoryUtils
                        .createItemWithNameAndLore(Material.GOLD_CHESTPLATE, 1, 0, "Earth Bender",
                                "Cooldown: " + benderCooldown + " seconds");
                InventoryUtils.replaceItem(event.getUserInventory(), bender);
                break;
            case "cross":
                double damage = 2.0 + 1.0 * event.getUpgradeLevel();
                ItemStack cross = InventoryUtils.createItemWithNameAndLore(Material.GOLD_LEGGINGS, 1, 0, "Cross",
                        "Damage: " + damage + " Hearts");
                InventoryUtils.replaceItem(event.getUserInventory(), cross);
                break;

        }
    }

    @Override
    public void onUserAttack(UserAttackEvent event) {
        ItemStack item = event.getWeapon();
        if (item == null) return;

        switch (item.getType()) {
            case GOLD_LEGGINGS:
                double damage = 4 + 2 * event.getAttacker().getUpgradeLevel("cross");
                event.setDamage(damage);
                break;
            case GOLD_CHESTPLATE:
                earthBenderLeftClick(event.getAttacker());
                break;
        }

    }
}

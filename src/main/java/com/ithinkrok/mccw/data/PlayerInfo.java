package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import com.ithinkrok.mccw.playerclass.PlayerClassHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 01/11/15.
 * <p>
 * Stores the player's info while they are online
 */
public class PlayerInfo {

    private Player player;
    private TeamColor teamColor;
    private WarsPlugin plugin;
    private Location shopBlock;
    private Inventory shopInventory;
    private PlayerClass playerClass;

    private HashMap<String, Integer> upgradeLevels = new HashMap<>();
    private HashMap<String, Boolean> coolingDown = new HashMap<>();

    private List<String> oldBuildingNows = new ArrayList<>();

    private int playerCash = 0;
    private boolean cloaked = false;

    private boolean inGame = false;

    public PlayerInfo(WarsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;

        updateScoreboard();
    }

    public boolean isCloaked() {
        return cloaked;
    }

    public void setCloaked(boolean cloaked) {
        this.cloaked = cloaked;
    }

    public void updateScoreboard() {
        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }



        if (inGame) {
            Objective mainObjective = scoreboard.getObjective("main");
            if (mainObjective == null) {
                mainObjective = scoreboard.registerNewObjective("main", "dummy");
                mainObjective.setDisplayName("Colony Wars");
                mainObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
            }

            mainObjective.getScore(ChatColor.YELLOW + "Balance:").setScore(getPlayerCash());

            TeamInfo teamInfo = plugin.getTeamInfo(teamColor);
            mainObjective.getScore(ChatColor.YELLOW + "Team Balance:").setScore(teamInfo.getTeamCash());

            mainObjective.getScore(ChatColor.GOLD + "Building Now:").setScore(teamInfo.getTotalBuildingNowCount());

            HashMap<String, Integer> buildingNow = teamInfo.getBuildingNowCounts();

            for (String s : oldBuildingNows) {
                if (buildingNow.containsKey(s)) continue;
                mainObjective.getScoreboard().resetScores(ChatColor.GREEN + s + ":");
            }

            oldBuildingNows.clear();

            for (Map.Entry<String, Integer> entry : buildingNow.entrySet()) {
                mainObjective.getScore(ChatColor.GREEN + entry.getKey() + ":").setScore(entry.getValue());
                oldBuildingNows.add(entry.getKey());
            }

            if (teamInfo.getRespawnChance() > 0) {
                mainObjective.getScore(ChatColor.AQUA + "Revival Rate:").setScore(teamInfo.getRespawnChance());
            }

        } else {
            Objective mainObjective = scoreboard.getObjective("main");
            if(mainObjective != null){
                mainObjective.unregister();
            }
        }



    }

    public int getPlayerCash() {
        return playerCash;
    }

    public Player getPlayer() {
        return player;
    }

    public void addPlayerCash(int cash) {
        playerCash += cash;
        updateScoreboard();
    }

    public void setUpgradeLevel(String upgrade, int level) {
        int oldLevel = getUpgradeLevel(upgrade);
        if (oldLevel == level && upgradeLevels.containsKey(upgrade)) return;

        upgradeLevels.put(upgrade, level);

        plugin.onPlayerUpgrade(this, upgrade, level);
    }

    public int getUpgradeLevel(String upgrade) {
        Integer level = upgradeLevels.get(upgrade);

        return level == null ? 0 : level;
    }

    public boolean startCoolDown(String ability, int seconds, String coolDownMessage) {
        if (isCoolingDown(ability)) {
            message(ChatColor.RED + "Please wait for this ability to cool down!");
            return false;
        }

        coolingDown.put(ability, true);

        Bukkit.getScheduler().runTaskLater(plugin, () -> stopCoolDown(ability, coolDownMessage), seconds * 20);

        return true;
    }

    public boolean isCoolingDown(String ability) {
        Boolean b = coolingDown.get(ability);

        if (b == null) return false;
        return b;
    }

    public void message(String message) {
        player.sendMessage(WarsPlugin.CHAT_PREFIX + message);
    }

    public void stopCoolDown(String ability, String message) {
        if (!isCoolingDown(ability)) return;

        coolingDown.put(ability, false);

        if (message == null) return;
        message(ChatColor.GREEN + message);
        player.playSound(player.getLocation(), Sound.ZOMBIE_UNFECT, 1.0f, 2.0f);
    }

    public boolean subtractPlayerCash(int cash) {
        if (cash > playerCash) return false;
        playerCash -= cash;

        updateScoreboard();

        message(ChatColor.RED + "$" + cash + ChatColor.YELLOW + " were deducted from your Account!");
        message("Your new Balance is: " + ChatColor.GREEN + "$" + playerCash + "!");

        return true;
    }

    public void recalculateInventory() {
        if (shopInventory == null || shopBlock == null) return;

        BuildingInfo buildingInfo = plugin.getBuildingInfo(shopBlock);
        if (buildingInfo == null || shopBlock.getBlock().getType() != Material.OBSIDIAN) {
            player.closeInventory();
            return;
        }

        TeamInfo teamInfo = plugin.getTeamInfo(getTeamColor());

        InventoryHandler inventoryHandler = plugin.getBuildingInventoryHandler();
        List<ItemStack> contents = new ArrayList<>();

        if (inventoryHandler != null) inventoryHandler.addInventoryItems(contents, buildingInfo, this, teamInfo);

        PlayerClassHandler classHandler = plugin.getPlayerClassHandler(this.getPlayerClass());
        classHandler.addInventoryItems(contents, buildingInfo, this, teamInfo);

        int index = 0;

        shopInventory.clear();

        for (ItemStack item : contents) {
            shopInventory.setItem(index++, item);
        }
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    public void setTeamColor(TeamColor teamColor) {
        this.teamColor = teamColor;

        player.setPlayerListName(getFormattedName());

        if (teamColor != null) updateTeamArmor();
    }

    public String getFormattedName() {
        if (teamColor == null) return player.getName();

        return teamColor.chatColor + player.getName() + ChatColor.DARK_AQUA;
    }

    public void updateTeamArmor() {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        setArmorColor(helmet);
        setArmorColor(chestplate);
        setArmorColor(leggings);
        setArmorColor(boots);

        PlayerInventory inv = player.getInventory();

        inv.setHelmet(helmet);
        inv.setChestplate(chestplate);
        inv.setLeggings(leggings);
        inv.setBoots(boots);
    }

    private void setArmorColor(ItemStack armor) {
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();

        meta.setColor(teamColor.armorColor);
        armor.setItemMeta(meta);
    }

    public boolean hasPlayerCash(int cash) {
        return cash <= playerCash;
    }

    public Location getShopBlock() {
        return shopBlock;
    }

    public void setShopBlock(Location shopBlock) {
        this.shopBlock = shopBlock;
    }

    public Inventory getShopInventory() {
        return shopInventory;
    }

    public void setShopInventory(Inventory shopInventory) {
        this.shopInventory = shopInventory;
    }

}

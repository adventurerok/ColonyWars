package com.ithinkrok.mccw.data;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import com.ithinkrok.mccw.playerclass.PlayerClassHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
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
public class User {

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

    public User(WarsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    public boolean isInGame() {
        return inGame && teamColor != null;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;

        if(!inGame){
            upgradeLevels.clear();
            coolingDown.clear();
            oldBuildingNows.clear();
            playerClass = null;
            shopInventory = null;
            shopBlock = null;
            playerCash = 0;
            oldBuildingNows.clear();
        }

        updateScoreboard();
    }

    public boolean isCloaked() {
        return cloaked;
    }

    private void setCloaked(boolean cloaked) {
        this.cloaked = cloaked;
    }

    public void cloak(){
        setCloaked(true);

        for(User u : plugin.getUsers()){
            if(this == u) continue;

            u.getPlayer().hidePlayer(player);
        }
    }

    public void decloak(){
        setCloaked(false);

        for(User u : plugin.getUsers()){
            if(this == u) continue;

            u.getPlayer().showPlayer(player);
        }
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

            Team team = getTeam();
            mainObjective.getScore(ChatColor.YELLOW + "Team Balance:").setScore(team.getTeamCash());

            mainObjective.getScore(ChatColor.GOLD + "Building Now:").setScore(team.getTotalBuildingNowCount());

            HashMap<String, Integer> buildingNow = team.getBuildingNowCounts();

            for (String s : oldBuildingNows) {
                if (buildingNow.containsKey(s)) continue;
                mainObjective.getScoreboard().resetScores(ChatColor.GREEN + s + ":");
            }

            oldBuildingNows.clear();

            for (Map.Entry<String, Integer> entry : buildingNow.entrySet()) {
                mainObjective.getScore(ChatColor.GREEN + entry.getKey() + ":").setScore(entry.getValue());
                oldBuildingNows.add(entry.getKey());
            }

            if (team.getRespawnChance() > 0) {
                mainObjective.getScore(ChatColor.AQUA + "Revival Rate:").setScore(team.getRespawnChance());
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

        plugin.getGameInstance().onPlayerUpgrade(new UserUpgradeEvent(this, upgrade, level));
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

    public void messageLocale(String locale, Object...objects){
        message(plugin.getLocale(locale, objects));
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

    public void openShopInventory(Location shopBlock){
        this.shopBlock = shopBlock;

        Building building = plugin.getGameInstance().getBuildingInfo(shopBlock);
        if (building == null || shopBlock.getBlock().getType() != Material.OBSIDIAN) {
            message(plugin.getLocale("obsidian-not-building"));
            return;
        }

        if(getTeamColor() != building.getTeamColor()){
            message(plugin.getLocale("building-not-yours"));
            return;
        }

        if(!building.isFinished()){
            message(plugin.getLocale("building-not-finished"));
            return;
        }

        redoShopInventory();

        player.openInventory(this.shopInventory);
    }

    public void redoShopInventory() {
        if (shopBlock == null) return;

        Building building = plugin.getGameInstance().getBuildingInfo(shopBlock);
        if (building == null || shopBlock.getBlock().getType() != Material.OBSIDIAN) {
            if(shopInventory != null) player.closeInventory();
            return;
        }

        List<ItemStack> contents = calculateInventoryContents(building);

        int index = 0;
        if(shopInventory != null) shopInventory.clear();
        else {
            int slots = 9 * ((contents.size() + 9) / 9);
            shopInventory = Bukkit.createInventory(player, slots, building.getBuildingName());
        }

        for (ItemStack item : contents) {
            shopInventory.setItem(index++, item);
        }
    }

    public void removeFromGame(){
        if(!isInGame()) return;

        Team team = getTeam();
        setTeamColor(null);
        setInGame(false);

        plugin.messageAll(ChatColor.GOLD + "The " + team.getTeamColor().name + ChatColor.GOLD +
                " has lost a player!");
        plugin.messageAll(ChatColor.GOLD + "There are now " + ChatColor.DARK_AQUA + team.getPlayerCount() +
                ChatColor.GOLD + " players left on the " + team.getTeamColor().name + ChatColor.GOLD + " Team");

        if(team.getPlayerCount() == 0){
            team.eliminate();
        }

        plugin.getGameInstance().checkVictory(true);

        plugin.getGameInstance().updateSpectatorInventories();

        setSpectator();
    }

    public void resetPlayerStats(boolean removePotionEffects){
        player.setMaxHealth(plugin.getMaxHealth());
        player.setHealth(plugin.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(5);

        if(removePotionEffects) removePotionEffects();
    }

    public void removePotionEffects(){
        List<PotionEffect> effects = new ArrayList<>(player.getActivePotionEffects());

        for (PotionEffect effect : effects) {
            player.removePotionEffect(effect.getType());
        }
    }

    public void setSpectator() {
        if(isInGame()) throw new RuntimeException("You cannot be a spectator when you are already in a game");

        cloak();

        player.setAllowFlight(true);
        player.spigot().setCollidesWithEntities(false);
        clearArmor();

        plugin.getGameInstance().setupSpectatorInventory(player);

        updateScoreboard();
    }

    public void unsetSpectator(){
        player.setAllowFlight(false);
        player.spigot().setCollidesWithEntities(true);

        decloak();
    }

    private List<ItemStack> calculateInventoryContents(Building building) {
        Team team = plugin.getTeam(getTeamColor());

        InventoryHandler inventoryHandler = plugin.getBuildingInventoryHandler();
        List<ItemStack> contents = new ArrayList<>();

        if (inventoryHandler != null) inventoryHandler.addInventoryItems(contents, building, this, team);

        PlayerClassHandler classHandler = plugin.getPlayerClassHandler(this.getPlayerClass());
        classHandler.addInventoryItems(contents, building, this, team);
        return contents;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public Team getTeam(){
        return plugin.getTeam(teamColor);
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    public void setTeamColor(TeamColor teamColor) {
        if(this.teamColor != null){
            getTeam().removePlayer(player);
        }

        this.teamColor = teamColor;

        if(this.teamColor != null){
            getTeam().addPlayer(player);
        }

        player.setPlayerListName(getFormattedName());
        player.setDisplayName(getFormattedName());

        if (teamColor != null) updateTeamArmor();
        else clearArmor();
    }

    public String getFormattedName() {
        if (teamColor == null) return player.getName();

        return teamColor.chatColor + player.getName();
    }
    public void clearArmor(){
        PlayerInventory inv = player.getInventory();

        inv.setHelmet(null);
        inv.setChestplate(null);
        inv.setLeggings(null);
        inv.setBoots(null);
    }

    public void updateTeamArmor() {
        if(teamColor == null){
            clearArmor();
            return;
        }
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

    public Inventory getShopInventory() {
        return shopInventory;
    }

    public void shopInventoryClosed(){
        shopInventory = null;
        shopBlock = null;
    }

}

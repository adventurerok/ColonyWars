package com.ithinkrok.mccw;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.ithinkrok.mccw.data.BuildingInfo;
import com.ithinkrok.mccw.data.PlayerInfo;
import com.ithinkrok.mccw.data.SchematicData;
import com.ithinkrok.mccw.data.TeamInfo;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import com.ithinkrok.mccw.inventory.OmniInventory;
import com.ithinkrok.mccw.playerclass.CloakerClass;
import com.ithinkrok.mccw.playerclass.GeneralClass;
import com.ithinkrok.mccw.playerclass.PlayerClassHandler;
import com.ithinkrok.mccw.playerclass.ScoutClass;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.InventoryUtils;
import com.ithinkrok.mccw.util.InvisiblePlayerAttacker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


/**
 * Created by paul on 01/11/15.
 * <p>
 * The main plugin class for Colony Wars
 */
public class WarsPlugin extends JavaPlugin {

    public static final String CHAT_PREFIX =
            ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + "ColonyWars" + ChatColor.GRAY + "] " + ChatColor.YELLOW;

    private HashMap<UUID, PlayerInfo> playerInfoHashMap = new HashMap<>();
    private EnumMap<TeamColor, TeamInfo> teamInfoEnumMap = new EnumMap<>(TeamColor.class);
    private HashMap<String, SchematicData> schematicDataHashMap = new HashMap<>();
    private List<BuildingInfo> buildings = new ArrayList<>();
    private HashMap<Location, BuildingInfo> buildingCentres = new HashMap<>();

    private boolean inGame = false;
    private boolean inShowdown = false;

    private OmniInventory buildingInventoryHandler;

    private EnumMap<PlayerClass, PlayerClassHandler> classHandlerEnumMap = new EnumMap<>(PlayerClass.class);
    private Random random = new Random();

    private ProtocolManager protocolManager;

    private Listener currentListener;

    private int countDown = 0;
    private int countDownTask = 0;

    public double getMaxHealth() {
        return (double) 40;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public boolean isInShowdown() {
        return inShowdown;
    }

    public void setInShowdown(boolean inShowdown) {
        this.inShowdown = inShowdown;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        protocolManager = ProtocolLibrary.getProtocolManager();
        InvisiblePlayerAttacker.enablePlayerAttacker(this, protocolManager);

        //WarsGameListener pluginListener = new WarsGameListener(this);
        //getServer().getPluginManager().registerEvents(pluginListener, this);

        currentListener = new WarsLobbyListener(this);
        getServer().getPluginManager().registerEvents(new WarsBaseListener(this), this);
        getServer().getPluginManager().registerEvents(currentListener, this);

        for (TeamColor c : TeamColor.values()) {
            teamInfoEnumMap.put(c, new TeamInfo(this, c));
        }

        schematicDataHashMap.put(Buildings.BASE, new SchematicData(Buildings.BASE, getConfig()));
        schematicDataHashMap.put(Buildings.FARM, new SchematicData(Buildings.FARM, getConfig()));
        schematicDataHashMap.put(Buildings.BLACKSMITH, new SchematicData(Buildings.BLACKSMITH, getConfig()));
        schematicDataHashMap.put(Buildings.MAGETOWER, new SchematicData(Buildings.MAGETOWER, getConfig()));
        schematicDataHashMap.put(Buildings.LUMBERMILL, new SchematicData(Buildings.LUMBERMILL, getConfig()));
        schematicDataHashMap.put(Buildings.CHURCH, new SchematicData(Buildings.CHURCH, getConfig()));
        schematicDataHashMap.put(Buildings.CATHEDRAL, new SchematicData(Buildings.CATHEDRAL, getConfig()));
        schematicDataHashMap.put(Buildings.GREENHOUSE, new SchematicData(Buildings.GREENHOUSE, getConfig()));

        buildingInventoryHandler = new OmniInventory(this, getConfig());

        classHandlerEnumMap.put(PlayerClass.GENERAL, new GeneralClass(getConfig()));
        classHandlerEnumMap.put(PlayerClass.SCOUT, new ScoutClass(this, getConfig()));
        classHandlerEnumMap.put(PlayerClass.CLOAKER, new CloakerClass(this, getConfig()));

        startLobbyCountdown();
    }

    private void startLobbyCountdown() {
        countDown = 180;

        messageAll(ChatColor.GREEN + "Starting count down to game from " + countDown);
        messageAll(ChatColor.GREEN + "If there are not enough players when the countdown ends, the countdown will " +
                "start again.");

        countDownTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            --countDown;

            for (PlayerInfo p : playerInfoHashMap.values()) {
                p.getPlayer().setLevel(countDown);
            }

            if (countDown == 0) {
                getServer().getScheduler().cancelTask(countDownTask);
                if (playerInfoHashMap.size() > 5) {
                    startGame();
                } else {
                    messageAll(ChatColor.RED + "You need at least 6 players to start a Colony Wars game.");
                    startLobbyCountdown();
                }
            }
        }, 20, 20);
    }

    public void startGame() {

    }

    public void messageAll(String message) {
        for (PlayerInfo p : playerInfoHashMap.values()) {
            p.message(message);
        }
    }


    public SchematicData getSchematicData(String buildingName) {
        return schematicDataHashMap.get(buildingName);
    }

    public InventoryHandler getBuildingInventoryHandler() {
        return buildingInventoryHandler;
    }

    public void setPlayerInfo(Player player, PlayerInfo playerInfo) {
        if (playerInfo == null) playerInfoHashMap.remove(player.getUniqueId());
        else playerInfoHashMap.put(player.getUniqueId(), playerInfo);
    }

    public void sendPlayersParticle(Player exclude, Location loc, EnumWrappers.Particle particle, int particleCount) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getParticles().write(0, particle);
        packet.getIntegers().write(0, particleCount);
        packet.getBooleans().write(0, false);
        packet.getFloat().write(0, (float) loc.getX()).write(1, (float) loc.getY()).write(2, (float) loc.getZ())
                .write(3, 0f).write(4, 0f).write(5, 0f).write(6, 0f);
        packet.getIntegerArrays().write(0, new int[0]);

        try {
            for (PlayerInfo playerInfo : playerInfoHashMap.values()) {
                if (playerInfo.getPlayer() == exclude) continue;
                protocolManager.sendServerPacket(playerInfo.getPlayer(), packet);
            }
        } catch (InvocationTargetException e) {
            getLogger().warning("Failed to send particle packet");
            e.printStackTrace();
        }
    }

    public Random getRandom() {
        return random;
    }

    public void addBuilding(BuildingInfo buildingInfo) {
        buildings.add(buildingInfo);

        if (buildingInfo.getCenterBlock() != null) buildingCentres.put(buildingInfo.getCenterBlock(), buildingInfo);

        getTeamInfo(buildingInfo.getTeamColor()).buildingStarted(buildingInfo.getBuildingName());
    }

    public TeamInfo getTeamInfo(TeamColor teamColor) {
        return teamInfoEnumMap.get(teamColor);
    }

    public void finishBuilding(BuildingInfo buildingInfo) {
        if (getTeamInfo(buildingInfo.getTeamColor()).getBuildingCount(buildingInfo.getBuildingName()) == 0) {
            for (PlayerInfo info : playerInfoHashMap.values()) {
                if (info.getTeamColor() != buildingInfo.getTeamColor()) continue;

                info.recalculateInventory();

                PlayerClassHandler playerClassHandler = getPlayerClassHandler(info.getPlayerClass());
                playerClassHandler
                        .onBuildingBuilt(buildingInfo.getBuildingName(), info, getTeamInfo(info.getTeamColor()));
            }
        }

        getTeamInfo(buildingInfo.getTeamColor()).buildingFinished(buildingInfo.getBuildingName());
    }

    public PlayerClassHandler getPlayerClassHandler(PlayerClass playerClass) {
        return classHandlerEnumMap.get(playerClass);
    }

    public BuildingInfo getBuildingInfo(Location center) {
        return buildingCentres.get(center);
    }

    public boolean canBuild(Vector minBB, Vector maxBB) {
        for (BuildingInfo building : buildings) {
            if (!building.canBuild(minBB, maxBB)) return false;
        }

        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to execute Colony Wars commands");
            return true;
        }

        Player player = (Player) sender;

        switch (command.getName().toLowerCase()) {
            case "transfer":
                if (args.length < 1) return false;

                try {
                    int amount = Integer.parseInt(args[0]);

                    PlayerInfo playerInfo = getPlayerInfo(player);
                    if (!playerInfo.subtractPlayerCash(amount)) {
                        player.sendMessage("You do not have that amount of money");
                        return true;
                    }

                    TeamInfo teamInfo = getTeamInfo(playerInfo.getTeamColor());
                    teamInfo.addTeamCash(amount);

                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }

            case "test":
                return args.length >= 1 && onTestCommand(player, command, args);

            default:
                return false;
        }

    }

    public PlayerInfo getPlayerInfo(Player player) {
        return playerInfoHashMap.get(player.getUniqueId());
    }

    private boolean onTestCommand(Player player, Command command, String[] args) {
        PlayerInfo playerInfo = getPlayerInfo(player);

        switch (args[0]) {
            case "team":
                if (args.length < 2) return false;

                TeamColor teamColor = TeamColor.valueOf(args[1].toUpperCase());
                setPlayerTeam(player, teamColor);

                break;
            case "class":
                if (args.length < 2) return false;

                PlayerClass playerClass = PlayerClass.valueOf(args[1].toUpperCase());
                playerInfo.setPlayerClass(playerClass);

                break;
            case "money":

                playerInfo.addPlayerCash(10000);
                getTeamInfo(playerInfo.getTeamColor()).addTeamCash(10000);
                break;
            case "build":
                if (args.length < 2) return false;

                player.getInventory()
                        .addItem(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 16, 0, args[1]));
                break;
        }

        return true;
    }

    public void setPlayerTeam(Player player, TeamColor teamColor) {
        PlayerInfo playerInfo = getPlayerInfo(player);

        if (playerInfo.getTeamColor() != null) {
            getTeamInfo(playerInfo.getTeamColor()).removePlayer(player);
        }

        playerInfo.setTeamColor(teamColor);
        getTeamInfo(teamColor).addPlayer(player);
    }

    public void removeBuilding(BuildingInfo buildingInfo) {
        buildings.remove(buildingInfo);
        getTeamInfo(buildingInfo.getTeamColor()).removeBuilding(buildingInfo.getBuildingName());

        buildingCentres.remove(buildingInfo.getCenterBlock());

        for (PlayerInfo info : playerInfoHashMap.values()) {
            if (info.getTeamColor() != buildingInfo.getTeamColor()) continue;

            info.recalculateInventory();
        }
    }

    public void setupPlayers() {
        for (PlayerInfo info : playerInfoHashMap.values()) {
            PlayerClassHandler classHandler = getPlayerClassHandler(info.getPlayerClass());

            classHandler.onGameBegin(info, getTeamInfo(info.getTeamColor()));
        }
    }

    public void updateScoutCompass(ItemStack item, Player player, TeamColor exclude) {
        InventoryUtils.setItemNameAndLore(item, "Locating closest player...");

        Bukkit.getScheduler().runTaskLater(this, () -> {
            Location closest = null;
            double minDist = 99999999999d;
            String closestName = null;

            for (PlayerInfo info : playerInfoHashMap.values()) {
                if (info.getTeamColor() == exclude) continue;

                double dist = player.getLocation().distanceSquared(info.getPlayer().getLocation());

                if (dist < minDist) {
                    minDist = dist;
                    closest = info.getPlayer().getLocation();
                    closestName = info.getPlayer().getName();
                }
            }


            if (closest != null) player.setCompassTarget(closest);
            else closestName = "No One";

            int compassIndex = player.getInventory().first(Material.COMPASS);
            ItemStack newCompass = player.getInventory().getItem(compassIndex);

            InventoryUtils.setItemNameAndLore(newCompass, "Player Compass", "Oriented at: " + closestName);

            player.getInventory().setItem(compassIndex, newCompass);
        }, 60);
    }

    public void onPlayerUpgrade(PlayerInfo playerInfo, String upgrade, int level) {
        PlayerClassHandler classHandler = getPlayerClassHandler(playerInfo.getPlayerClass());

        classHandler.onPlayerUpgrade(playerInfo, upgrade, level);
    }

    public void cloak(Player player) {
        for (PlayerInfo p : playerInfoHashMap.values()) {
            if (p.getPlayer() == player) continue;

            p.getPlayer().hidePlayer(player);
        }
    }

    public void decloak(Player player) {
        for (PlayerInfo p : playerInfoHashMap.values()) {
            if (p.getPlayer() == player) continue;

            p.getPlayer().showPlayer(player);
        }
    }

    public int getPlayerCount() {
        return playerInfoHashMap.size();
    }
}

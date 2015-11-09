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
import com.ithinkrok.mccw.util.DirectoryUtils;
import com.ithinkrok.mccw.util.InventoryUtils;
import com.ithinkrok.mccw.util.InvisiblePlayerAttacker;
import com.ithinkrok.mccw.util.SchematicBuilder;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
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

    private String map = "canyon";

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

    public void startLobbyCountdown() {
        countDown = 180;

        messageAll(ChatColor.GREEN + "Starting count down to game from " + countDown);
        messageAll(ChatColor.GREEN + "If there are not enough players when the countdown ends, the countdown will " +
                "start again.");

        countDownTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            --countDown;

            for (PlayerInfo p : playerInfoHashMap.values()) {
                p.getPlayer().setLevel(countDown);
            }


            if (countDown == 120) {
                messageAll(ChatColor.GREEN + "Game starting in " + ChatColor.DARK_AQUA + "2" + ChatColor.GREEN +
                        " minutes!");
            }
            if (countDown == 60) {
                messageAll(ChatColor.GREEN + "Game starting in " + ChatColor.DARK_AQUA + "1" + ChatColor.GREEN +
                        " minute!");
            } else if (countDown == 30) {
                messageAll(ChatColor.GREEN + "Game starting in " + ChatColor.DARK_AQUA + "30" + ChatColor.GREEN +
                        " seconds!");
            } else if (countDown == 10) {
                messageAll(ChatColor.GREEN + "Game starting in " + ChatColor.DARK_AQUA + "10" + ChatColor.GREEN +
                        " seconds!");
            } else if (countDown == 0) {
                getServer().getScheduler().cancelTask(countDownTask);
                countDownTask = 0;
                if (playerInfoHashMap.size() > 5) {
                    startGame();
                } else {
                    messageAll(ChatColor.RED + "You need at least 6 players to start a Colony Wars game.");
                    startLobbyCountdown();
                }
            } else if (countDown < 6) {
                messageAll(ChatColor.DARK_AQUA.toString() + countDown + ChatColor.GREEN + "!");
            }


        }, 20, 20);
    }

    public void messageAll(String message) {
        for (PlayerInfo p : playerInfoHashMap.values()) {
            p.message(message);
        }
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

        getTeamInfo(buildingInfo.getTeamColor()).buildingFinished(buildingInfo);
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
                        playerInfo.message(ChatColor.RED + "You do not have that amount of money");
                        return true;
                    }

                    TeamInfo teamInfo = getTeamInfo(playerInfo.getTeamColor());
                    teamInfo.addTeamCash(amount);

                    teamInfo.message(playerInfo.getFormattedName() + " transferred " + ChatColor.GREEN + "$" + amount +
                            ChatColor.YELLOW + " to your team's account!");
                    teamInfo.message("Your Team's new Balance is: " + ChatColor.GREEN + "$" + teamInfo.getTeamCash() +
                            ChatColor.YELLOW + "!");

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

                playerInfo.message("You were changed to team " + teamColor);

                break;
            case "class":
                if (args.length < 2) return false;

                PlayerClass playerClass = PlayerClass.valueOf(args[1].toUpperCase());
                playerInfo.setPlayerClass(playerClass);

                playerInfo.message("You were changed to class " + playerClass);

                break;
            case "money":

                playerInfo.addPlayerCash(10000);
                getTeamInfo(playerInfo.getTeamColor()).addTeamCash(10000);

                playerInfo.message("10000 added to both you and your team's balance");
                break;
            case "build":
                if (args.length < 2) return false;

                player.getInventory()
                        .addItem(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 16, 0, args[1]));

                playerInfo.message("Added 16 " + args[1] + " build blocks to your inventory");
                break;
            case "start_game":
                playerInfo.message("Attempting to start a new game!");

                stopCountdown();
                startGame();

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
        if (teamColor != null) getTeamInfo(teamColor).addPlayer(player);
    }

    public void stopCountdown() {
        if (countDownTask == 0) return;

        getServer().getScheduler().cancelTask(countDownTask);
        countDownTask = 0;

        for (PlayerInfo p : playerInfoHashMap.values()) {
            p.getPlayer().setLevel(0);
        }
    }

    public void startGame() {
        try {
            DirectoryUtils.copy(Paths.get("./canyon/"), Paths.get("./playing/"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.createWorld(new WorldCreator("playing"));

        HandlerList.unregisterAll(currentListener);
        currentListener = new WarsGameListener(this);
        getServer().getPluginManager().registerEvents(currentListener, this);
        setupPlayers();
        setupBases();
    }

    public void setupPlayers() {
        for (PlayerInfo info : playerInfoHashMap.values()) {

            if (info.getTeamColor() == null) {
                setPlayerTeam(info.getPlayer(), assignPlayerTeam());
            }

            if (info.getPlayerClass() == null) {
                info.setPlayerClass(assignPlayerClass());
            }

            info.getPlayer().teleport(getMapSpawn(info.getTeamColor()));

            info.getPlayer().setGameMode(GameMode.SURVIVAL);
            info.getPlayer().setMaxHealth(40);
            info.getPlayer().setHealth(40);
            info.getPlayer().setSaturation(5);
            info.getPlayer().setFoodLevel(20);

            info.setInGame(true);

            info.getPlayer().getInventory().clear();
            info.updateTeamArmor();
            info.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE));

            info.updateScoreboard();

            PlayerClassHandler classHandler = getPlayerClassHandler(info.getPlayerClass());
            classHandler.onGameBegin(info, getTeamInfo(info.getTeamColor()));

            info.message(ChatColor.GOLD + "You are playing on the " + info.getTeamColor().name + ChatColor.GOLD +
                    " Team");

            info.message(ChatColor.GOLD + "You are playing as the class " + ChatColor.DARK_AQUA +
                    info.getPlayerClass().name);
        }

        for (PlayerInfo info : playerInfoHashMap.values()) {
            decloak(info.getPlayer());
        }
    }

    public void setupBases() {
        World world = getServer().getWorld("playing");
        FileConfiguration config = getConfig();

        for (TeamColor team : TeamColor.values()) {
            String base = "maps." + map + "." + team.toString().toLowerCase() + ".base";

            Location build = new Location(world, config.getInt(base + ".x"), config.getInt(base + ".y"),
                    config.getInt(base + ".z"));

            SchematicBuilder.pasteSchematic(this, getSchematicData(Buildings.BASE), build, 0, team);

            if(getTeamInfo(team).getPlayerCount() == 0){
                getTeamInfo(team).eliminate();
            }
        }
    }

    public TeamColor assignPlayerTeam() {
        ArrayList<TeamColor> smallest = new ArrayList<>();
        int leastCount = Integer.MAX_VALUE;

        for (TeamColor team : TeamColor.values()) {
            TeamInfo info = getTeamInfo(team);
            if (info.getPlayerCount() < leastCount) {
                leastCount = info.getPlayerCount();

                smallest.clear();
            }

            if (info.getPlayerCount() == leastCount) {
                smallest.add(team);
            }
        }

        return smallest.get(random.nextInt(smallest.size()));
    }

    public PlayerClass assignPlayerClass() {
        return PlayerClass.values()[random.nextInt(PlayerClass.values().length)];
    }

    public void decloak(Player player) {
        getPlayerInfo(player).setCloaked(false);

        for (PlayerInfo p : playerInfoHashMap.values()) {
            if (p.getPlayer() == player) continue;

            p.getPlayer().showPlayer(player);
        }
    }

    public SchematicData getSchematicData(String buildingName) {
        return schematicDataHashMap.get(buildingName);
    }

    public Location getMapSpawn(TeamColor team) {
        World world = getServer().getWorld("playing");
        FileConfiguration config = getConfig();

        String base;
        if (team == null) base = "maps." + map + ".center";
        else base = "maps." + map + "." + team.toString().toLowerCase() + ".spawn";

        return new Location(world, config.getDouble(base + ".x"), config.getDouble(base + ".y"),
                config.getDouble(base + ".z"));
    }

    public void removeBuilding(BuildingInfo buildingInfo) {
        buildings.remove(buildingInfo);
        getTeamInfo(buildingInfo.getTeamColor()).removeBuilding(buildingInfo);

        buildingCentres.remove(buildingInfo.getCenterBlock());

        for (PlayerInfo info : playerInfoHashMap.values()) {
            if (info.getTeamColor() != buildingInfo.getTeamColor()) continue;

            info.recalculateInventory();
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
        getPlayerInfo(player).setCloaked(true);

        for (PlayerInfo p : playerInfoHashMap.values()) {
            if (p.getPlayer() == player) continue;

            p.getPlayer().hidePlayer(player);
        }
    }

    public int getPlayerCount() {
        return playerInfoHashMap.size();
    }
}

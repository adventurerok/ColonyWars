package com.ithinkrok.mccw;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.ithinkrok.mccw.data.*;
import com.ithinkrok.mccw.enumeration.CountdownType;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.event.UserUpgradeEvent;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import com.ithinkrok.mccw.inventory.OmniInventory;
import com.ithinkrok.mccw.inventory.SpectatorInventory;
import com.ithinkrok.mccw.listener.CommandListener;
import com.ithinkrok.mccw.listener.WarsBaseListener;
import com.ithinkrok.mccw.listener.WarsGameListener;
import com.ithinkrok.mccw.listener.WarsLobbyListener;
import com.ithinkrok.mccw.playerclass.*;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by paul on 01/11/15.
 * <p>
 * The main plugin class for Colony Wars
 */
public class WarsPlugin extends JavaPlugin {

    public static final String CHAT_PREFIX =
            ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + "ColonyWars" + ChatColor.GRAY + "] " + ChatColor.YELLOW;

    private ConcurrentHashMap<UUID, User> playerInfoHashMap = new ConcurrentHashMap<>();
    private EnumMap<TeamColor, Team> teamInfoEnumMap = new EnumMap<>(TeamColor.class);
    private HashMap<String, Schematic> schematicDataHashMap = new HashMap<>();
    private List<Building> buildings = new ArrayList<>();
    private HashMap<Location, Building> buildingCentres = new HashMap<>();

    /**
     * Is there a game currently in progress
     */
    private boolean inGame = false;

    /**
     * Are we currently in a showdown
     */
    private boolean inShowdown = false;

    /**
     * Has the game been won already
     */
    private boolean inAftermath = false;

    private OmniInventory buildingInventoryHandler;
    private SpectatorInventory spectatorInventoryHandler;

    private EnumMap<PlayerClass, PlayerClassHandler> classHandlerEnumMap = new EnumMap<>(PlayerClass.class);
    private Random random = new Random();

    private ProtocolManager protocolManager;

    private Listener currentListener;
    private CommandListener commandListener;

    private CountdownHandler countdownHandler;

    private ShowdownArena showdownArena;

    private String map = "canyon";
    private TeamColor winningTeam;

    public TeamColor getWinningTeam() {
        return winningTeam;
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

    public ShowdownArena getShowdownArena() {
        return showdownArena;
    }

    @Override
    public void onEnable() {
        commandListener = new CommandListener(this);

        saveDefaultConfig();

        protocolManager = ProtocolLibrary.getProtocolManager();
        InvisiblePlayerAttacker.enablePlayerAttacker(this, protocolManager);

        //WarsGameListener pluginListener = new WarsGameListener(this);
        //getServer().getPluginManager().registerEvents(pluginListener, this);

        currentListener = new WarsLobbyListener(this);
        getServer().getPluginManager().registerEvents(new WarsBaseListener(this), this);
        getServer().getPluginManager().registerEvents(currentListener, this);

        for (TeamColor c : TeamColor.values()) {
            teamInfoEnumMap.put(c, new Team(this, c));
        }

        schematicDataHashMap.put(Buildings.BASE, new Schematic(Buildings.BASE, getConfig()));
        schematicDataHashMap.put(Buildings.FARM, new Schematic(Buildings.FARM, getConfig()));
        schematicDataHashMap.put(Buildings.BLACKSMITH, new Schematic(Buildings.BLACKSMITH, getConfig()));
        schematicDataHashMap.put(Buildings.MAGETOWER, new Schematic(Buildings.MAGETOWER, getConfig()));
        schematicDataHashMap.put(Buildings.LUMBERMILL, new Schematic(Buildings.LUMBERMILL, getConfig()));
        schematicDataHashMap.put(Buildings.CHURCH, new Schematic(Buildings.CHURCH, getConfig()));
        schematicDataHashMap.put(Buildings.CATHEDRAL, new Schematic(Buildings.CATHEDRAL, getConfig()));
        schematicDataHashMap.put(Buildings.GREENHOUSE, new Schematic(Buildings.GREENHOUSE, getConfig()));
        schematicDataHashMap.put(Buildings.SCOUTTOWER, new Schematic(Buildings.SCOUTTOWER, getConfig()));
        schematicDataHashMap.put(Buildings.CANNONTOWER, new Schematic(Buildings.CANNONTOWER, getConfig()));
        schematicDataHashMap.put(Buildings.WALL, new Schematic(Buildings.WALL, getConfig()));
        schematicDataHashMap.put(Buildings.LANDMINE, new Schematic(Buildings.LANDMINE, getConfig()));
        schematicDataHashMap.put(Buildings.WIRELESSBUFFER, new Schematic(Buildings.WIRELESSBUFFER, getConfig()));
        schematicDataHashMap.put(Buildings.TIMERBUFFER, new Schematic(Buildings.TIMERBUFFER, getConfig()));

        buildingInventoryHandler = new OmniInventory(this, getConfig());
        spectatorInventoryHandler = new SpectatorInventory(this);

        classHandlerEnumMap.put(PlayerClass.GENERAL, new GeneralClass(getConfig()));
        classHandlerEnumMap.put(PlayerClass.SCOUT, new ScoutClass(this, getConfig()));
        classHandlerEnumMap.put(PlayerClass.CLOAKER, new CloakerClass(this, getConfig()));
        classHandlerEnumMap.put(PlayerClass.ARCHER, new ArcherClass(getConfig()));
        classHandlerEnumMap.put(PlayerClass.MAGE, new MageClass(this, getConfig()));
        classHandlerEnumMap.put(PlayerClass.PEASANT, new PeasantClass(getConfig()));

        countdownHandler = new CountdownHandler(this);
        countdownHandler.startLobbyCountdown();


    }

    public void preEndGame() {
        buildings.forEach(Building::clearHolograms);
    }

    public void endGame() {
        for (User user : playerInfoHashMap.values()) {
            playerJoinLobby(user.getPlayer());
        }

        playerInfoHashMap.values().forEach(User::decloak);

        for (TeamColor c : TeamColor.values()) {
            teamInfoEnumMap.put(c, new Team(this, c));
        }

        buildingCentres.clear();

        buildings.forEach(Building::clearHolograms);

        buildings.clear();

        showdownArena = null;
        winningTeam = null;

        HandlerList.unregisterAll(currentListener);
        currentListener = new WarsLobbyListener(this);
        getServer().getPluginManager().registerEvents(currentListener, this);

        Bukkit.unloadWorld("playing", false);

        try {
            DirectoryUtils.delete(Paths.get("./playing/"));
        } catch (IOException e) {
            getLogger().info("Failed to unload old world");
            e.printStackTrace();
        }

        System.gc();

        setInGame(false);
        setInAftermath(false);
        setInShowdown(false);

        for (User user : playerInfoHashMap.values()) {
            playerJoinLobby(user.getPlayer());
        }

        countdownHandler.startLobbyCountdown();
    }

    public String getLocale(String name, Object... params) {
        return String.format(getConfig().getString("locale." + name), params);
    }

    public void playerJoinLobby(Player player) {
        User user = getUser(player);

        user.setInGame(false);
        user.getPlayer().setGameMode(GameMode.ADVENTURE);
        user.resetPlayerStats(true);
        user.clearArmor();

        setPlayerTeam(player, null);

        PlayerInventory inv = player.getInventory();

        inv.clear();

        inv.addItem(InventoryUtils.createItemWithNameAndLore(Material.LEATHER_HELMET, 1, 0, getLocale("team-chooser"),
                getLocale("team-chooser-desc")));

        inv.addItem(InventoryUtils.createItemWithNameAndLore(Material.WOOD_SWORD, 1, 0, getLocale("class-chooser"),
                getLocale("class-chooser-desc")));

        user.message(getLocale("choose-team-class"));

        user.message(getLocale("map-info"));

        user.getPlayer().teleport(Bukkit.getWorld("world").getSpawnLocation());
    }

    public InventoryHandler getBuildingInventoryHandler() {
        return buildingInventoryHandler;
    }

    public void setPlayerInfo(Player player, User user) {
        if (user == null) playerInfoHashMap.remove(player.getUniqueId());
        else playerInfoHashMap.put(player.getUniqueId(), user);
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
            for (User user : playerInfoHashMap.values()) {
                if (user.getPlayer() == exclude) continue;
                protocolManager.sendServerPacket(user.getPlayer(), packet);
            }
        } catch (InvocationTargetException e) {
            getLogger().warning("Failed to send particle packet");
            e.printStackTrace();
        }
    }

    public Random getRandom() {
        return random;
    }

    public void addBuilding(Building building) {
        buildings.add(building);

        if (building.getCenterBlock() != null) buildingCentres.put(building.getCenterBlock(), building);

        getTeam(building.getTeamColor()).buildingStarted(building.getBuildingName());
    }

    public Team getTeam(TeamColor teamColor) {
        return teamInfoEnumMap.get(teamColor);
    }

    public void finishBuilding(Building building) {
        if (getTeam(building.getTeamColor()).getBuildingCount(building.getBuildingName()) == 0) {
            for (User info : playerInfoHashMap.values()) {
                if (info.getTeamColor() != building.getTeamColor()) continue;

                info.redoShopInventory();

                PlayerClassHandler playerClassHandler = getPlayerClassHandler(info.getPlayerClass());
                playerClassHandler.onBuildingBuilt(building.getBuildingName(), info, getTeam(info.getTeamColor()));
            }
        }

        getTeam(building.getTeamColor()).buildingFinished(building);
    }

    public PlayerClassHandler getPlayerClassHandler(PlayerClass playerClass) {
        return classHandlerEnumMap.get(playerClass);
    }

    public Building getBuildingInfo(Location center) {
        return buildingCentres.get(center);
    }

    public boolean canBuild(BoundingBox bounds) {
        for (Building building : buildings) {
            if (!building.canBuild(bounds)) return false;
        }

        return !bounds.interceptsXZ(showdownArena.getBounds());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandListener.onCommand(sender, command, label, args);
    }

    public void setPlayerTeam(Player player, TeamColor teamColor) {
        User user = getUser(player);

        if (user.getTeamColor() != null) {
            getTeam(user.getTeamColor()).removePlayer(player);
        }

        user.setTeamColor(teamColor);
        if (teamColor != null) getTeam(teamColor).addPlayer(player);
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

        calculateShowdownArena();

        setupBases();

        checkVictory(false);
    }

    private void calculateShowdownArena() {
        FileConfiguration config = getConfig();
        String base = "maps." + map + ".showdown-size";
        int radiusX = config.getInt(base + ".x");
        int radiusZ = config.getInt(base + ".z");

        Location center = getMapSpawn(null);
        Vector showdownMin = center.toVector().add(new Vector(-radiusX - 5, 0, -radiusZ - 5));
        Vector showdownMax = center.toVector().add(new Vector(radiusX + 5, 0, radiusZ + 5));

        BoundingBox bounds = new BoundingBox(showdownMin, showdownMax);

        showdownArena = new ShowdownArena(radiusX, radiusZ, center, bounds);
    }

    public void startShowdown() {
        int x = showdownArena.getRadiusX();
        int z = showdownArena.getRadiusZ();

        for (User user : getUsers()) {
            int offsetX = (-x / 2) + random.nextInt(x);
            int offsetZ = (-z / 2) + random.nextInt(z);
            int offsetY = 1;

            Location teleport = getMapSpawn(null);
            teleport.setX(teleport.getX() + offsetX);
            teleport.setY(teleport.getY() + offsetY);
            teleport.setZ(teleport.getZ() + offsetZ);

            user.getPlayer().teleport(teleport);
        }

        setInShowdown(true);

        messageAll(ChatColor.BOLD.toString() + ChatColor.GOLD + "Showdown starts NOW!");
    }

    public void setupPlayers() {
        for (User info : getUsers()) {

            if (info.getTeamColor() == null) {
                setPlayerTeam(info.getPlayer(), assignPlayerTeam());
            }

            if (info.getPlayerClass() == null) {
                info.setPlayerClass(assignPlayerClass());
            }

            info.getPlayer().teleport(getMapSpawn(info.getTeamColor()));

            info.getPlayer().setGameMode(GameMode.SURVIVAL);

            info.resetPlayerStats(true);

            info.setInGame(true);

            info.getPlayer().getInventory().clear();
            info.updateTeamArmor();
            info.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE));

            info.updateScoreboard();

            PlayerClassHandler classHandler = getPlayerClassHandler(info.getPlayerClass());
            classHandler.onGameBegin(info, getTeam(info.getTeamColor()));

            info.message(ChatColor.GOLD + "You are playing on the " + info.getTeamColor().name + ChatColor.GOLD +
                    " Team");

            info.message(ChatColor.GOLD + "You are playing as the class " + ChatColor.DARK_AQUA +
                    info.getPlayerClass().name);
        }

        playerInfoHashMap.values().forEach(User::decloak);
    }

    public void setupBases() {
        World world = getServer().getWorld("playing");
        FileConfiguration config = getConfig();

        for (TeamColor team : TeamColor.values()) {
            String base = "maps." + map + "." + team.toString().toLowerCase() + ".base";

            Location build = new Location(world, config.getInt(base + ".x"), config.getInt(base + ".y"),
                    config.getInt(base + ".z"));


            SchematicBuilder.pasteSchematic(this, getSchematicData(Buildings.BASE), build, 0, team);

            if (getTeam(team).getPlayerCount() == 0) {
                getTeam(team).eliminate();
            }

        }
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

    public void messageAll(String message) {
        for (User p : playerInfoHashMap.values()) {
            p.message(message);
        }
    }

    public TeamColor assignPlayerTeam() {
        ArrayList<TeamColor> smallest = new ArrayList<>();
        int leastCount = Integer.MAX_VALUE;

        for (TeamColor team : TeamColor.values()) {
            Team info = getTeam(team);
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

    public double getMaxHealth() {
        return (double) 40;
    }

    public Schematic getSchematicData(String buildingName) {
        return schematicDataHashMap.get(buildingName);
    }

    public void removeBuilding(Building building) {
        buildings.remove(building);
        getTeam(building.getTeamColor()).removeBuilding(building);

        buildingCentres.remove(building.getCenterBlock());

        for (User info : playerInfoHashMap.values()) {
            if (info.getTeamColor() != building.getTeamColor()) continue;

            info.redoShopInventory();
        }

        building.clearHolograms();
    }

    public void updateScoutCompass(ItemStack item, Player player, TeamColor exclude) {
        InventoryUtils.setItemNameAndLore(item, "Locating closest player...");

        Bukkit.getScheduler().runTaskLater(this, () -> {
            Location closest = null;
            double minDist = 99999999999d;
            String closestName = null;

            for (User info : playerInfoHashMap.values()) {
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

    public void onPlayerUpgrade(UserUpgradeEvent event) {
        PlayerClassHandler classHandler = getPlayerClassHandler(event.getUser().getPlayerClass());

        classHandler.onPlayerUpgrade(event);
    }


    public User getUser(Player player) {
        return playerInfoHashMap.get(player.getUniqueId());
    }

    public User getUser(UUID uniqueId) {
        return playerInfoHashMap.get(uniqueId);
    }

    public int getPlayerCount() {
        return playerInfoHashMap.size();
    }

    public void checkVictory(boolean checkShowdown) {
        if (isInAftermath()) return;
        Set<TeamColor> teamsInGame = new HashSet<>();

        for (User info : playerInfoHashMap.values()) {
            if (!info.isInGame()) continue;
            if (info.getTeamColor() == null) continue;

            teamsInGame.add(info.getTeamColor());
        }

        if (teamsInGame.size() == 0) {
            messageAll(ChatColor.GOLD + "Oh dear. Everyone is dead!");
            setInAftermath(true);
            countdownHandler.startEndCountdown();
            return;
        } else if (teamsInGame.size() > 1) {
            if (checkShowdown) checkShowdownStart(teamsInGame.size());
            return;
        }

        TeamColor winner = teamsInGame.iterator().next();
        this.winningTeam = winner;

        messageAll(ChatColor.GOLD + "The " + winner.name + ChatColor.GOLD + " Team has won the game!");

        setInAftermath(true);

        countdownHandler.startEndCountdown();
    }

    public void checkShowdownStart(int teamsInGame) {
        if (isInShowdown() || countdownHandler.getCountdownType() == CountdownType.SHOWDOWN_START) return;
        if (teamsInGame > 2 && getPlayersInGame() > 4) return;

        countdownHandler.startShowdownCountdown();
    }

    public int getPlayersInGame() {
        int count = 0;

        for (Team team : teamInfoEnumMap.values()) {
            count += team.getPlayerCount();
        }

        return count;
    }

    public boolean isInAftermath() {
        return inAftermath;
    }

    public void setInAftermath(boolean inAftermath) {
        this.inAftermath = inAftermath;
    }


    public void updateSpectatorInventories() {
        for (User info : getUsers()) {
            if (info.isInGame() && info.getTeamColor() != null) return;

            setupSpectatorInventory(info.getPlayer());
        }
    }

    public Collection<User> getUsers() {
        return playerInfoHashMap.values();
    }

    public void setupSpectatorInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        int slot = 9;

        List<ItemStack> items = new ArrayList<>();
        spectatorInventoryHandler.addInventoryItems(items, null, getUser(player), null);

        for (ItemStack item : items) {
            inv.setItem(slot++, item);
        }
    }

    public void handleSpectatorInventory(InventoryClickEvent event) {
        event.setCancelled(true);

        spectatorInventoryHandler
                .onInventoryClick(event.getCurrentItem(), null, getUser((Player) event.getWhoClicked()), null);
    }
}

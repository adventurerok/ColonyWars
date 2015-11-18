package com.ithinkrok.mccw;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.ithinkrok.mccw.data.*;
import com.ithinkrok.mccw.enumeration.GameState;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.handler.CountdownHandler;
import com.ithinkrok.mccw.handler.GameInstance;
import com.ithinkrok.mccw.lobby.LobbyMinigame;
import com.ithinkrok.mccw.lobby.ParcourMinigame;
import com.ithinkrok.mccw.lobby.WoolHeadMinigame;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import com.ithinkrok.mccw.inventory.OmniInventory;
import com.ithinkrok.mccw.inventory.SpectatorInventory;
import com.ithinkrok.mccw.listener.CommandListener;
import com.ithinkrok.mccw.listener.WarsBaseListener;
import com.ithinkrok.mccw.listener.WarsLobbyListener;
import com.ithinkrok.mccw.playerclass.*;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.Handbook;
import com.ithinkrok.mccw.util.InventoryUtils;
import com.ithinkrok.mccw.util.InvisiblePlayerAttacker;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

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


    /**
     * Is there a game currently in progress
     */
    private boolean inGame = false;

    private OmniInventory buildingInventoryHandler;
    private SpectatorInventory spectatorInventoryHandler;

    private EnumMap<PlayerClass, PlayerClassHandler> classHandlerEnumMap = new EnumMap<>(PlayerClass.class);
    private Random random = new Random();

    private ProtocolManager protocolManager;

    private Listener currentListener;
    private CommandListener commandListener;

    private CountdownHandler countdownHandler;
    private GameInstance gameInstance;

    private List<String> mapList;
    private ConcurrentHashMap<String, Integer> mapVotes = new ConcurrentHashMap<>();

    private List<LobbyMinigame> lobbyMinigames = new ArrayList<>();

    private String handbookMeta;
    private ItemStack handbook;

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

    public void changeListener(Listener newListener) {
        HandlerList.unregisterAll(currentListener);
        currentListener = newListener;
        getServer().getPluginManager().registerEvents(currentListener, this);
    }

    public void resetTeams() {
        for (TeamColor c : TeamColor.values()) {
            teamInfoEnumMap.put(c, new Team(this, c));
        }
    }

    @Override
    public void onEnable() {
        commandListener = new CommandListener(this);

        saveDefaultConfig();

        mapList = getConfig().getStringList("map-list");

        handbookMeta = Handbook.loadHandbookMeta(this);

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
        schematicDataHashMap.put("PlayerCathedral", new Schematic("PlayerCathedral", getConfig()));
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
        classHandlerEnumMap.put(PlayerClass.INFERNO, new InfernoClass(this, getConfig()));
        classHandlerEnumMap.put(PlayerClass.DARK_KNIGHT, new DarkKnightClass(getConfig()));
        classHandlerEnumMap.put(PlayerClass.PRIEST, new PriestClass(this, getConfig()));
        classHandlerEnumMap.put(PlayerClass.WARRIOR, new WarriorClass(this, getConfig()));

        lobbyMinigames.add(new WoolHeadMinigame(this));
        lobbyMinigames.add(new ParcourMinigame(this));

        getLobbyMinigames().forEach(LobbyMinigame::resetMinigame);

        countdownHandler = new CountdownHandler(this);
        countdownHandler.startLobbyCountdown();


    }

    public List<LobbyMinigame> getLobbyMinigames() {
        return lobbyMinigames;
    }

    public List<String> getMapList() {
        return mapList;
    }

    public void setMapVotes(String map, int votes) {
        mapVotes.put(map, votes);
    }

    public void playerTeleportLobby(Player player) {
        player.teleport(Bukkit.getWorld("world").getSpawnLocation());
    }

    public void playerJoinLobby(Player player) {
        User user = getUser(player);

        user.setInGame(false);
        user.getPlayer().setGameMode(GameMode.ADVENTURE);
        user.unsetSpectator();
        user.resetPlayerStats(true);
        user.clearArmor();

        user.setTeamColor(null);
        user.setMapVote(null);

        PlayerInventory inv = player.getInventory();

        inv.clear();

        inv.addItem(InventoryUtils.createItemWithNameAndLore(Material.LEATHER_HELMET, 1, 0, getLocale("team-chooser"),
                getLocale("team-chooser-desc")));

        inv.addItem(InventoryUtils.createItemWithNameAndLore(Material.WOOD_SWORD, 1, 0, getLocale("class-chooser"),
                getLocale("class-chooser-desc")));

        inv.addItem(InventoryUtils.createItemWithNameAndLore(Material.EMPTY_MAP, 1, 0, getLocale("map-chooser"),
                getLocale("map-chooser-desc")));

        user.message(getLocale("choose-team-class"));

        user.message(getLocale("map-info"));

        player.teleport(getLobbySpawn());

        givePlayerHandbook(player);

        for (LobbyMinigame minigame : getLobbyMinigames()) {
            minigame.onUserJoinLobby(user);
        }
    }

    public User getUser(Player player) {
        if (player == null) return null;
        return playerInfoHashMap.get(player.getUniqueId());
    }

    public String getLocale(String name, Object... params) {
        return String.format(getConfig().getString("locale." + name), params);
    }

    public Location getLobbySpawn() {
        return Bukkit.getWorld("world").getSpawnLocation();
    }

    public Location getMapSpawn(TeamColor teamColorOrNull){
        if(gameInstance == null) return getLobbySpawn();
        return gameInstance.getMapSpawn(teamColorOrNull);
    }

    public Building getBuildingInfo(Location centerBlock){
        return gameInstance.getBuildingInfo(centerBlock);
    }

    public boolean isInBuilding(Location blockLocation){
        return gameInstance.isInBuilding(blockLocation);
    }

    public void handleSpectatorInventory(InventoryClickEvent event){
        gameInstance.handleSpectatorInventory(event);
    }

    public ShowdownArena getShowdownArena(){
        return gameInstance.getShowdownArena();
    }

    public void givePlayerHandbook(Player player) {
        PlayerInventory inv = player.getInventory();
        if (getHandbook() == null) {
            String meta = getHandbookMeta();

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "minecraft:give " + player.getName() + " written_book 1 0 " + meta);

            int index = inv.first(Material.WRITTEN_BOOK);
            ItemStack book = inv.getItem(index);
            book.setAmount(1);
            inv.setItem(index, null);
            setHandbook(book.clone());
        }

        if (inv.getItem(8) == null || inv.getItem(8).getType() == Material.AIR) {
            inv.setItem(8, getHandbook().clone());
        } else inv.addItem(getHandbook().clone());
    }

    public ItemStack getHandbook() {
        return handbook;
    }

    public String getHandbookMeta() {
        return handbookMeta;
    }

    public void setHandbook(ItemStack handbook) {
        this.handbook = handbook;
    }

    public InventoryHandler getBuildingInventoryHandler() {
        return buildingInventoryHandler;
    }

    public void setPlayerInfo(Player player, User user) {
        if (user == null) playerInfoHashMap.remove(player.getUniqueId());
        else playerInfoHashMap.put(player.getUniqueId(), user);
    }

    public Random getRandom() {
        return random;
    }

    public Team getTeam(TeamColor teamColor) {
        return teamInfoEnumMap.get(teamColor);
    }


    public PlayerClassHandler getPlayerClassHandler(PlayerClass playerClass) {
        return classHandlerEnumMap.get(playerClass);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandListener.onCommand(sender, command, label, args);
    }

    public CountdownHandler getCountdownHandler() {
        return countdownHandler;
    }

    public GameInstance getGameInstance() {
        return gameInstance;
    }


    public void messageAllLocale(String locale, Object... args) {
        messageAll(getLocale(locale, args));
    }

    public void messageAll(String message) {
        for (User p : playerInfoHashMap.values()) {
            p.message(message);
        }
    }

    public double getMaxHealth() {
        return (double) 40;
    }

    public Schematic getSchematicData(String buildingName) {
        return schematicDataHashMap.get(buildingName);
    }

    public User getUser(UUID uniqueId) {
        if (uniqueId == null) return null;
        return playerInfoHashMap.get(uniqueId);
    }

    public int getPlayerCount() {
        return playerInfoHashMap.size();
    }

    public int getPlayersInGame() {
        int count = 0;

        for (Team team : teamInfoEnumMap.values()) {
            count += team.getPlayerCount();
        }

        return count;
    }

    public SpectatorInventory getSpectatorInventoryHandler() {
        return spectatorInventoryHandler;
    }

    public Collection<User> getUsers() {
        return playerInfoHashMap.values();
    }

    public void changeGameState(GameState state) {
        if (state == GameState.GAME) {
            if(gameInstance != null) return;
            setInGame(true);
            gameInstance = new GameInstance(this, assignGameMap());
        }

        if(gameInstance == null) return;
        gameInstance.changeGameState(state);

        if(state == GameState.LOBBY){
            gameInstance = null;
            setInGame(false);

            getLobbyMinigames().forEach(LobbyMinigame::resetMinigame);

            for (User user : getUsers()) {
                playerJoinLobby(user.getPlayer());
            }

            countdownHandler.startLobbyCountdown();
        }
    }

    public String assignGameMap() {
        int highestVotes = Integer.MIN_VALUE;

        List<String> possible = new ArrayList<>();

        for (String map : mapList) {
            int votes = getMapVotes(map);

            if (votes > highestVotes) {
                highestVotes = votes;
                possible.clear();
            }
            if (votes == highestVotes) possible.add(map);
        }

        String bestMap = possible.get(random.nextInt(possible.size()));

        while (bestMap.equals(getConfig().getString("random-map"))) {
            bestMap = mapList.get(random.nextInt(mapList.size()));
        }

        messageAll(getLocale("map-chosen", bestMap));

        return bestMap;
    }

    public int getMapVotes(String map) {
        Integer result = mapVotes.get(map);

        if (result == null) return 0;
        return result;
    }

    public GameState getGameState() {
        if (gameInstance != null) return gameInstance.getGameState();
        else return GameState.LOBBY;
    }


}

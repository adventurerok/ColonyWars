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
import com.ithinkrok.mccw.enumeration.CountdownType;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.inventory.InventoryHandler;
import com.ithinkrok.mccw.inventory.OmniInventory;
import com.ithinkrok.mccw.playerclass.*;
import com.ithinkrok.mccw.strings.Buildings;
import com.ithinkrok.mccw.util.DirectoryUtils;
import com.ithinkrok.mccw.util.InventoryUtils;
import com.ithinkrok.mccw.util.InvisiblePlayerAttacker;
import com.ithinkrok.mccw.util.SchematicBuilder;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
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

    private ConcurrentHashMap<UUID, PlayerInfo> playerInfoHashMap = new ConcurrentHashMap<>();
    private EnumMap<TeamColor, TeamInfo> teamInfoEnumMap = new EnumMap<>(TeamColor.class);
    private HashMap<String, SchematicData> schematicDataHashMap = new HashMap<>();
    private List<BuildingInfo> buildings = new ArrayList<>();
    private HashMap<Location, BuildingInfo> buildingCentres = new HashMap<>();

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

    private EnumMap<PlayerClass, PlayerClassHandler> classHandlerEnumMap = new EnumMap<>(PlayerClass.class);
    private Random random = new Random();

    private ProtocolManager protocolManager;

    private Listener currentListener;

    private int countDown = 0;
    private int countDownTask = 0;
    private CountdownType countdownType;

    private int showdownRadiusX;
    private int showdownRadiusZ;
    private Location showdownCenter;

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

    public int getShowdownRadiusX() {
        return showdownRadiusX;
    }

    public int getShowdownRadiusZ() {
        return showdownRadiusZ;
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
        classHandlerEnumMap.put(PlayerClass.ARCHER, new ArcherClass(getConfig()));
        classHandlerEnumMap.put(PlayerClass.MAGE, new MageClass(this, getConfig()));

        startLobbyCountdown();


    }

    public void startLobbyCountdown() {
        messageAll(getLocale("start-minute-warning", "3"));

        startCountdown(180, CountdownType.GAME_START, () -> {
            if (playerInfoHashMap.size() > 5) {
                startGame();
            } else {
                messageAll(getLocale("not-enough-players"));
                startLobbyCountdown();
            }
        }, null);
    }

    public void endGame() {
        for (PlayerInfo playerInfo : playerInfoHashMap.values()) {
            playerJoinLobby(playerInfo.getPlayer());
        }

        for (PlayerInfo playerInfo : playerInfoHashMap.values()) {
            decloak(playerInfo.getPlayer());
        }

        for (TeamColor c : TeamColor.values()) {
            teamInfoEnumMap.put(c, new TeamInfo(this, c));
        }

        buildingCentres.clear();

        buildings.forEach(BuildingInfo::clearHolograms);

        buildings.clear();

        Bukkit.unloadWorld("playing", false);

        HandlerList.unregisterAll(currentListener);
        currentListener = new WarsLobbyListener(this);
        getServer().getPluginManager().registerEvents(currentListener, this);

        setInGame(false);
        setInAftermath(false);
        setInShowdown(false);

        startLobbyCountdown();
    }

    public void setupSpectatorInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        int slot = 9;
        for (PlayerInfo info : playerInfoHashMap.values()) {
            if (!info.isInGame() || info.getTeamColor() == null) continue;

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            InventoryUtils.setItemNameAndLore(head, info.getFormattedName(),
                    getLocale("spectate-player", info.getFormattedName()));


            SkullMeta skullMeta = (SkullMeta) head.getItemMeta();

            skullMeta.setOwner(info.getPlayer().getName());
            head.setItemMeta(skullMeta);

            inv.setItem(slot++, head);
        }
    }

    public String getLocale(String name, Object... params) {
        return String.format(getConfig().getString("locale." + name), params);
    }

    public void handleSpectatorInventory(InventoryClickEvent event) {
        event.setCancelled(true);

        HumanEntity clicker = event.getWhoClicked();

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.SKULL_ITEM) return;

        String owner = ((SkullMeta) clicked.getItemMeta()).getOwner();

        for (PlayerInfo info : playerInfoHashMap.values()) {
            if (!info.getPlayer().getName().equals(owner)) continue;

            clicker.teleport(info.getPlayer().getLocation());
            return;
        }
    }

    public void playerJoinLobby(Player player) {
        PlayerInfo playerInfo = getPlayerInfo(player);

        playerInfo.setInGame(false);
        playerInfo.getPlayer().setGameMode(GameMode.ADVENTURE);
        playerInfo.getPlayer().setMaxHealth(getMaxHealth());
        playerInfo.getPlayer().setHealth(getMaxHealth());
        playerInfo.getPlayer().setFoodLevel(20);
        playerInfo.getPlayer().setSaturation(20);
        playerInfo.getPlayer().setAllowFlight(false);
        playerInfo.clearArmor();

        playerInfo.getPlayer().getActivePotionEffects().clear();

        removePotionEffects(player);

        setPlayerTeam(player, null);

        PlayerInventory inv = player.getInventory();

        inv.clear();

        inv.addItem(InventoryUtils.createItemWithNameAndLore(Material.LEATHER_HELMET, 1, 0, getLocale("team-chooser"),
                getLocale("team-chooser-desc")));

        inv.addItem(InventoryUtils.createItemWithNameAndLore(Material.WOOD_SWORD, 1, 0, getLocale("class-chooser"),
                getLocale("class-chooser-desc")));

        playerInfo.message(getLocale("choose-team-class"));

        playerInfo.message(getLocale("map-info"));

        playerInfo.getPlayer().teleport(Bukkit.getWorld("world").getSpawnLocation());
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

                    teamInfo.message(playerInfo.getFormattedName() + ChatColor.DARK_AQUA + " transferred " +
                            ChatColor.GREEN + "$" + amount +
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
            case "start_showdown":
                playerInfo.message("Attempting to start showdown");

                stopCountdown();
                startShowdown();
                break;
            case "base_location":
                TeamInfo teamInfo = getTeamInfo(playerInfo.getTeamColor());
                if (teamInfo == null) {
                    playerInfo.message("Your team is null");
                    break;
                }

                playerInfo.message("Base location: " + teamInfo.getBaseLocation());
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

        checkVictory(false);
    }

    public void startShowdown() {
        FileConfiguration config = getConfig();
        String base = "maps." + map + ".showdown-size";
        int x = config.getInt(base + ".x");
        int z = config.getInt(base + ".z");

        showdownRadiusX = x;
        showdownRadiusZ = z;
        showdownCenter = getMapSpawn(null);

        for (PlayerInfo playerInfo : getPlayers()) {
            int offsetX = (-x / 2) + random.nextInt(x);
            int offsetZ = (-z / 2) + random.nextInt(z);
            int offsetY = 1;

            Location teleport = getMapSpawn(null);
            teleport.setX(teleport.getX() + offsetX);
            teleport.setY(teleport.getY() + offsetY);
            teleport.setZ(teleport.getZ() + offsetZ);

            playerInfo.getPlayer().teleport(teleport);
        }

        setInShowdown(true);

        messageAll(ChatColor.BOLD.toString() + ChatColor.GOLD + "Showdown starts NOW!");
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
            info.getPlayer().setMaxHealth(getMaxHealth());
            info.getPlayer().setHealth(getMaxHealth());
            info.getPlayer().setSaturation(5);
            info.getPlayer().setFoodLevel(20);
            info.getPlayer().setAllowFlight(false);

            removePotionEffects(info.getPlayer());

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

            if (getTeamInfo(team).getPlayerCount() == 0) {
                getTeamInfo(team).eliminate();
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

    public Collection<PlayerInfo> getPlayers() {
        return playerInfoHashMap.values();
    }

    public void messageAll(String message) {
        for (PlayerInfo p : playerInfoHashMap.values()) {
            p.message(message);
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

    public double getMaxHealth() {
        return (double) 40;
    }

    public void removePotionEffects(Player player) {
        List<PotionEffect> effects = new ArrayList<>(player.getActivePotionEffects());

        for (PotionEffect effect : effects) {
            player.removePotionEffect(effect.getType());
        }
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

    public void removeBuilding(BuildingInfo buildingInfo) {
        buildings.remove(buildingInfo);
        getTeamInfo(buildingInfo.getTeamColor()).removeBuilding(buildingInfo);

        buildingCentres.remove(buildingInfo.getCenterBlock());

        for (PlayerInfo info : playerInfoHashMap.values()) {
            if (info.getTeamColor() != buildingInfo.getTeamColor()) continue;

            info.recalculateInventory();
        }

        buildingInfo.clearHolograms();
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

    public PlayerInfo getPlayerInfo(Player player) {
        return playerInfoHashMap.get(player.getUniqueId());
    }

    public int getPlayerCount() {
        return playerInfoHashMap.size();
    }

    public void checkVictory(boolean checkShowdown) {
        if (isInAftermath()) return;
        Set<TeamColor> teamsInGame = new HashSet<>();

        for (PlayerInfo info : playerInfoHashMap.values()) {
            if (!info.isInGame()) continue;
            if (info.getTeamColor() == null) continue;

            teamsInGame.add(info.getTeamColor());
        }

        if (teamsInGame.size() == 0) {
            messageAll(ChatColor.GOLD + "Oh dear. Everyone is dead!");
            setInAftermath(true);
            startEndCountdown();
            return;
        } else if (teamsInGame.size() > 1) {
            if (checkShowdown) checkShowdownStart(teamsInGame.size());
            return;
        }

        TeamColor winner = teamsInGame.iterator().next();
        this.winningTeam = winner;

        messageAll(ChatColor.GOLD + "The " + winner.name + ChatColor.GOLD + " Team has won the game!");

        setInAftermath(true);

        startEndCountdown();
    }

    public void checkShowdownStart(int teamsInGame) {
        if (isInShowdown() || countdownType == CountdownType.SHOWDOWN_START) return;
        if (teamsInGame > 2 && getPlayersInGame() > 4) return;

        startShowdownCountdown();
    }

    public int getPlayersInGame() {
        int count = 0;

        for (TeamInfo teamInfo : teamInfoEnumMap.values()) {
            count += teamInfo.getPlayerCount();
        }

        return count;
    }

    public boolean isInAftermath() {
        return inAftermath;
    }

    public void setInAftermath(boolean inAftermath) {
        this.inAftermath = inAftermath;
    }

    public void startEndCountdown() {
        messageAll(ChatColor.GREEN + "Teleporting back to the lobby in 15 seconds!");

        startCountdown(15, CountdownType.GAME_END, this::endGame, () -> {
            if (countDown < 10) return;
            Player randomPlayer = getTeamInfo(winningTeam).getRandomPlayer();
            if (randomPlayer == null) return;
            Location loc = randomPlayer.getLocation();
            Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

            Color color = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
            Color fade = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));

            firework.setVelocity(new Vector(0, 0.5f, 0));
            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(
                    FireworkEffect.builder().with(FireworkEffect.Type.BURST).trail(true).withColor(color).withFade(fade)
                            .build());
            firework.setFireworkMeta(meta);
        });
    }

    public void startShowdownCountdown() {
        messageAll(ChatColor.GREEN + "Showdown starting in 30 seconds!");

        startCountdown(30, CountdownType.SHOWDOWN_START, this::startShowdown, null);
    }

    public boolean isInShowdownBounds(Location loc) {
        double xd = Math.abs(loc.getX() - showdownCenter.getX());
        double zd = Math.abs(loc.getZ() - showdownCenter.getZ());

        return !(xd > showdownRadiusX || zd > showdownRadiusZ);

    }

    private void startCountdown(int countdownFrom, CountdownType countdownType, Runnable finished, Runnable during) {
        if (this.countDownTask != 0) {
            getServer().getScheduler().cancelTask(countDownTask);
            this.countDownTask = 0;
        }

        this.countDown = countdownFrom;
        this.countdownType = countdownType;

        countDownTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            --countDown;

            for (PlayerInfo p : playerInfoHashMap.values()) {
                p.getPlayer().setLevel(countDown);
            }

            if (during != null) during.run();

            if (countDown == 120) messageAll(getLocale(countdownType.name + "-minute-warning", "2"));
            else if (countDown == 60) messageAll(getLocale(countdownType.name + "-minute-warning", "1"));
            else if (countDown == 30) messageAll(getLocale(countdownType.name + "-seconds-warning", "30"));
            else if (countDown == 10) messageAll(getLocale(countdownType.name + "-seconds-warning", "10"));
            else if (countDown == 0) {
                getServer().getScheduler().cancelTask(countDownTask);
                countDownTask = 0;
                finished.run();
                this.countdownType = null;
            } else if (countDown < 6) {
                messageAll(getLocale(countdownType.name + "-final-warning", Integer.toString(countDown)));
            }


        }, 20, 20);
    }
}

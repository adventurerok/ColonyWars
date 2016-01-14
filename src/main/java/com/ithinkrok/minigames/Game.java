package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.map.MapBlockBreakNaturallyEvent;
import com.ithinkrok.minigames.event.map.MapCreatureSpawnEvent;
import com.ithinkrok.minigames.event.map.MapItemSpawnEvent;
import com.ithinkrok.minigames.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.event.user.game.UserQuitEvent;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryCloseEvent;
import com.ithinkrok.minigames.event.user.state.UserAttackedEvent;
import com.ithinkrok.minigames.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.event.user.world.*;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.item.IdentifierMap;
import com.ithinkrok.minigames.lang.LangFile;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.MultipleLanguageLookup;
import com.ithinkrok.minigames.map.GameMap;
import com.ithinkrok.minigames.map.GameMapInfo;
import com.ithinkrok.minigames.schematic.Schematic;
import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskScheduler;
import com.ithinkrok.minigames.user.UserResolver;
import com.ithinkrok.minigames.util.EntityUtils;
import com.ithinkrok.minigames.util.io.*;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by paul on 31/12/15.
 */
@SuppressWarnings("unchecked")
public class Game implements LanguageLookup, TaskScheduler, UserResolver, FileLoader, ConfigHolder {

    private ConcurrentMap<UUID, User> usersInServer = new ConcurrentHashMap<>();
    private List<GameGroup> gameGroups = new ArrayList<>();

    private Plugin plugin;

    private GameGroup spawnGameGroup;

    private ConfigurationSection config;

    private MultipleLanguageLookup multipleLanguageLookup = new MultipleLanguageLookup();

    private IdentifierMap<CustomItem> customItemIdentifierMap = new IdentifierMap<>();

    private HashMap<String, Listener> defaultListeners = new HashMap<>();
    private WeakHashMap<String, GameGroup> mapToGameGroup = new WeakHashMap<>();

    private Map<String, GameState> gameStates = new HashMap<>();
    private Map<String, TeamIdentifier> teamIdentifiers = new HashMap<>();
    private Map<String, Kit> kits = new HashMap<>();

    private Map<String, GameMapInfo> maps = new HashMap<>();
    private Map<String, Schematic> schematicMap = new HashMap<>();
    private Map<String, ConfigurationSection> sharedObjects = new HashMap<>();



    private String startMapName;
    private String startGameStateName;

    public Game(Plugin plugin) {
        this.plugin = plugin;

        unloadDefaultWorlds();
    }

    private void unloadDefaultWorlds() {
        if (Bukkit.getWorlds().size() != 1) System.out.println("You should disable the nether/end worlds to save RAM!");

        for (World world : Bukkit.getWorlds()) {
            world.setKeepSpawnInMemory(false);

            for (Chunk chunk : world.getLoadedChunks()) {
                chunk.unload(false, true);
            }
        }
    }

    @Override
    public void addListener(String name, Listener listener) {
        defaultListeners.put(name, listener);
    }

    @Override
    public void addLanguageLookup(LanguageLookup languageLookup) {
        multipleLanguageLookup.addLanguageLookup(languageLookup);
    }

    @Override
    public void addSharedObject(String name, ConfigurationSection config) {
        sharedObjects.put(name, config);
    }

    @Override
    public void addSchematic(Schematic schematic) {
        schematicMap.put(schematic.getName(), schematic);
    }

    @Override
    public void addCustomItem(CustomItem item) {
        customItemIdentifierMap.put(item.getName(), item);
    }

    public CustomItem getCustomItem(String name) {
        return customItemIdentifierMap.get(name);
    }

    public CustomItem getCustomItem(int identifier) {
        return customItemIdentifierMap.get(identifier);
    }

    public Collection<GameState> getGameStates() {
        return gameStates.values();
    }

    public void registerListeners() {
        Listener listener = new GameListener();
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public GameGroup getSpawnGameGroup() {
        return spawnGameGroup;
    }

    public void reloadConfig() {
        plugin.reloadConfig();

        config = plugin.getConfig();

        reloadTeamIdentifiers();
        reloadGameStates();
        reloadKits();
        reloadMaps();

        sharedObjects.clear();
        defaultListeners.clear();
        multipleLanguageLookup = new MultipleLanguageLookup();
        customItemIdentifierMap.clear();

        ConfigParser.parseConfig(this, this, this, "config.yml", config);
    }

    private void reloadTeamIdentifiers() {
        teamIdentifiers.clear();

        ConfigurationSection teamConfigs = config.getConfigurationSection("team_identifiers");

        for (String name : teamConfigs.getKeys(false)) {
            ConfigurationSection teamConfig = teamConfigs.getConfigurationSection(name);

            DyeColor dyeColor = DyeColor.valueOf(teamConfig.getString("dye_color").toUpperCase());

            String formattedName = teamConfig.getString("formatted_name", null);

            String armorColorString = teamConfig.getString("armor_color", null);
            Color armorColor = null;
            if (armorColorString != null) {
                armorColor = Color.fromRGB(Integer.parseInt(armorColorString.replace("#", "")));
            }
            String chatColorString = teamConfig.getString("chat_color", null);
            ChatColor chatColor = null;
            if (chatColorString != null) {
                chatColor = ChatColor.valueOf(chatColorString);
            }

            teamIdentifiers.put(name, new TeamIdentifier(name, formattedName, dyeColor, armorColor, chatColor));
        }
    }

    private void reloadGameStates() {
        gameStates.clear();

        ConfigurationSection gameStatesConfig = config.getConfigurationSection("game_states");

        for (String name : gameStatesConfig.getKeys(false)) {
            ConfigurationSection gameStateConfig = gameStatesConfig.getConfigurationSection(name);
            List<Listener> listeners = new ArrayList<>();

            ConfigurationSection listenersConfig = gameStateConfig.getConfigurationSection("listeners");

            for (String listenerName : listenersConfig.getKeys(false)) {
                ConfigurationSection listenerConfig = listenersConfig.getConfigurationSection(listenerName);

                try {
                    listeners.add(ListenerLoader.loadListener(this, listenerConfig));
                } catch (Exception e) {
                    System.out.println("Failed to create listener: " + listenerName + " for gamestate: " + name);
                    e.printStackTrace();
                }
            }
            gameStates.put(name, new GameState(name, listeners));
        }

        startGameStateName = config.getString("start_game_state");

    }



    private void reloadKits() {
        kits.clear();

        ConfigurationSection kitsConfig = config.getConfigurationSection("kits");

        for (String name : kitsConfig.getKeys(false)) {
            ConfigurationSection gameStateConfig = kitsConfig.getConfigurationSection(name);
            List<Listener> listeners = new ArrayList<>();

            String formattedName = gameStateConfig.getString("formatted_name", null);

            ConfigurationSection listenersConfig = gameStateConfig.getConfigurationSection("listeners");

            for (String listenerName : listenersConfig.getKeys(false)) {
                ConfigurationSection listenerConfig = listenersConfig.getConfigurationSection(listenerName);

                try {
                    listeners.add(ListenerLoader.loadListener(this, listenerConfig));
                } catch (Exception e) {
                    System.out.println("Failed to create listener: " + listenerName + " for kit: " + name);
                    e.printStackTrace();
                }
            }
            kits.put(name, new Kit(name, formattedName, listeners));
        }

    }


    private void reloadMaps() {
        maps.clear();

        File mapsFolder = new File(plugin.getDataFolder(), GameMapInfo.MAPS_FOLDER);
        if (!mapsFolder.exists() || mapsFolder.isFile()) {
            throw new RuntimeException("Maps directory does not exist!");
        }

        String[] mapNames = mapsFolder.list((dir, name) -> name.endsWith(".yml"));

        for (String mapNameWithYml : mapNames) {
            String mapNameWithoutYml = mapNameWithYml.substring(0, mapNameWithYml.length() - 4);

            loadMapInfo(mapNameWithoutYml);
        }

        startMapName = config.getString("start_map");
    }

    private void loadMapInfo(String mapName) {
        maps.put(mapName, new GameMapInfo(this, mapName));
    }

    public ConfigurationSection getSharedObject(String name) {
        return sharedObjects.get(name);
    }

    @Override
    public LangFile loadLangFile(String path) {
        return new LangFile(ResourceHandler.getPropertiesResource(plugin, path));
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    public Schematic getSchematic(String name) {
        return schematicMap.get(name);
    }

    public GameMapInfo getStartMapInfo() {
        return maps.get(startMapName);
    }

    public GameMapInfo getMapInfo(String mapName) {
        return maps.get(mapName);
    }

    @Override
    public ConfigurationSection loadConfig(String path) {
        return ResourceHandler.getConfigResource(plugin, path);
    }

    public ConfigurationSection getConfig() {
        return config;
    }


    @Override
    public User getUser(UUID uuid) {
        return usersInServer.get(uuid);
    }

    private GameGroup createGameGroup() {
        GameGroup gameGroup = new GameGroup(this);

        gameGroup.setDefaultListeners(defaultListeners);
        gameGroup.setTeamIdentifiers(teamIdentifiers.values());
        gameGroup.setKits(kits.values());

        gameGroup.prepareStart();
        gameGroup.changeGameState(startGameStateName);
        gameGroup.changeMap(startMapName);

        return gameGroup;
    }

    private User createUser(GameGroup gameGroup, Team team, UUID uuid, LivingEntity entity) {
        User user = new User(this, gameGroup, team, uuid, entity);

        usersInServer.put(user.getUuid(), user);
        return user;
    }

    public String getChatPrefix() {
        return ChatColor.GRAY + "[" + ChatColor.DARK_AQUA + "ColonyWars" + ChatColor.GRAY + "] " + ChatColor.YELLOW;
    }

    @Override
    public boolean hasLocale(String name) {
        return multipleLanguageLookup.hasLocale(name);
    }

    @Override
    public String getLocale(String name) {
        return multipleLanguageLookup.getLocale(name);
    }

    @Override
    public String getLocale(String name, Object... args) {
        return multipleLanguageLookup.getLocale(name, args);
    }

    public void unload() {
        gameGroups.forEach(GameGroup::unload);
    }

    @Override
    public GameTask doInFuture(GameRunnable task) {
        return doInFuture(task, 1);
    }

    @Override
    public GameTask doInFuture(GameRunnable task, int delay) {
        GameTask gameTask = new GameTask(task);

        gameTask.schedule(plugin, delay);
        return gameTask;
    }

    @Override
    public GameTask repeatInFuture(GameRunnable task, int delay, int period) {
        GameTask gameTask = new GameTask(task);

        gameTask.schedule(plugin, delay, period);
        return gameTask;
    }

    @Override
    public void cancelAllTasks() {
        throw new RuntimeException("You cannot cancel all game tasks");
    }

    public void makeEntityRepresentUser(User user, Entity entity) {
        entity.setMetadata("rep", new FixedMetadataValue(plugin, user.getUuid()));
    }

    public void setGameGroupForMap(GameGroup gameGroup, String mapName) {
        mapToGameGroup.values().remove(gameGroup);
        mapToGameGroup.put(mapName, gameGroup);
    }

    public TeamIdentifier getTeamIdentifier(String team) {
        return teamIdentifiers.get(team);
    }

    public Kit getKit(String kitName) {
        return kits.get(kitName);
    }

    private class GameListener implements Listener {

        @EventHandler
        public void eventPlayerJoined(PlayerJoinEvent event) {
            event.setJoinMessage(null);

            Player player = event.getPlayer();

            User user = getUser(player.getUniqueId());
            GameGroup gameGroup;

            if (user != null) {
                gameGroup = user.getGameGroup();
            } else {
                if (spawnGameGroup == null) {
                    spawnGameGroup = createGameGroup();
                    gameGroups.add(spawnGameGroup);
                }

                gameGroup = spawnGameGroup;
                user = createUser(gameGroup, null, player.getUniqueId(), player);
            }

            gameGroup.userEvent(new UserJoinEvent(user, UserJoinEvent.JoinReason.JOINED_SERVER));
        }

        @EventHandler
        public void eventPlayerQuit(PlayerQuitEvent event) {
            event.setQuitMessage(null);

            User user = getUser(event.getPlayer().getUniqueId());

            UserQuitEvent userEvent = new UserQuitEvent(user, UserQuitEvent.QuitReason.QUIT_SERVER);
            user.getGameGroup().userEvent(userEvent);

            if (userEvent.getRemoveUser()) {
                usersInServer.remove(event.getPlayer().getUniqueId());
                user.cancelAllTasks();
            }
        }

        @EventHandler
        public void eventBlockExp(BlockExpEvent event) {
            if (event instanceof BlockBreakEvent) return;

            String mapName = event.getBlock().getWorld().getName();
            GameGroup gameGroup = mapToGameGroup.get(mapName);
            GameMap map = gameGroup.getCurrentMap();
            if (!map.getWorld().getName().equals(mapName))
                throw new RuntimeException("Map still registered to old GameGroup");

            gameGroup.gameEvent(new MapBlockBreakNaturallyEvent(gameGroup, map, event));
        }

        @EventHandler
        public void eventItemSpawn(ItemSpawnEvent event) {
            String mapName = event.getEntity().getWorld().getName();
            GameGroup gameGroup = mapToGameGroup.get(mapName);
            GameMap map = gameGroup.getCurrentMap();
            if (!map.getWorld().getName().equals(mapName))
                throw new RuntimeException("Map still registered to old GameGroup");

            gameGroup.gameEvent(new MapItemSpawnEvent(gameGroup, map, event));
        }

        @EventHandler
        public void eventCreatureSpawn(CreatureSpawnEvent event) {
            String mapName = event.getEntity().getWorld().getName();
            GameGroup gameGroup = mapToGameGroup.get(mapName);
            GameMap map = gameGroup.getCurrentMap();
            if (!map.getWorld().getName().equals(mapName))
                throw new RuntimeException("Map still registered to old GameGroup");

            gameGroup.gameEvent(new MapCreatureSpawnEvent(gameGroup, map, event));
        }

        @EventHandler
        public void eventPlayerDropItem(PlayerDropItemEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserDropItemEvent(user, event));
        }

        @EventHandler
        public void eventPlayerPickupItem(PlayerPickupItemEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserPickupItemEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInteractWorld(PlayerInteractEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserInteractWorldEvent(user, event));
        }

        @EventHandler
        public void eventBlockBreak(BlockBreakEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserBreakBlockEvent(user, event));
        }

        @EventHandler
        public void eventBlockPlace(BlockPlaceEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserPlaceBlockEvent(user, event));
        }

        @EventHandler(priority = EventPriority.LOW)
        public void eventEntityDamagedByEntity(EntityDamageByEntityEvent event) {
            User attacker = EntityUtils.getRepresentingUser(Game.this, event.getDamager());
            if (attacker == null) return;

            User attacked = EntityUtils.getRepresentingUser(attacker, event.getEntity());
            boolean representing = !attacker.equals(EntityUtils.getActualUser(Game.this, event.getDamager()));

            attacker.getGameGroup().userEvent(new UserAttackEvent(attacker, event, attacked, representing));
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void eventEntityDamaged(EntityDamageEvent event) {
            User attacked = EntityUtils.getActualUser(Game.this, event.getEntity());
            if (attacked == null) return;

            if (event instanceof EntityDamageByEntityEvent) {
                User attacker = EntityUtils.getRepresentingUser(attacked, event.getEntity());
                attacked.getGameGroup()
                        .userEvent(new UserAttackedEvent(attacked, (EntityDamageByEntityEvent) event, attacker));
            } else {
                attacked.getGameGroup().userEvent(new UserDamagedEvent(attacked, event));
            }


        }

        @EventHandler
        public void eventPlayerFoodLevelChange(FoodLevelChangeEvent event) {
            User user = getUser(event.getEntity().getUniqueId());
            user.getGameGroup().userEvent(new UserFoodLevelChangeEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInteractEntity(PlayerInteractEntityEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserRightClickEntityEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInventoryClick(InventoryClickEvent event) {
            User user = getUser(event.getWhoClicked().getUniqueId());
            user.getGameGroup().userEvent(new UserInventoryClickEvent(user, event));
        }

        @EventHandler
        public void eventPlayerInventoryClose(InventoryCloseEvent event) {
            User user = getUser(event.getPlayer().getUniqueId());
            user.getGameGroup().userEvent(new UserInventoryCloseEvent(user, event));
        }
    }
}

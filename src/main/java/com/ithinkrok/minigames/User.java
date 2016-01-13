package com.ithinkrok.minigames;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.game.*;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryCloseEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.inventory.ClickableInventory;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.metadata.MetadataHolder;
import com.ithinkrok.minigames.metadata.UserMetadata;
import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskList;
import com.ithinkrok.minigames.task.TaskScheduler;
import com.ithinkrok.minigames.user.AttackerTracker;
import com.ithinkrok.minigames.user.CooldownHandler;
import com.ithinkrok.minigames.user.UpgradeHandler;
import com.ithinkrok.minigames.user.UserResolver;
import com.ithinkrok.minigames.user.scoreboard.ScoreboardDisplay;
import com.ithinkrok.minigames.user.scoreboard.ScoreboardHandler;
import com.ithinkrok.minigames.util.EventExecutor;
import com.ithinkrok.minigames.util.InventoryUtils;
import com.ithinkrok.minigames.util.SoundEffect;
import com.ithinkrok.minigames.util.playerstate.PlayerState;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import static com.ithinkrok.minigames.util.InventoryUtils.*;

import java.util.*;

/**
 * Created by paul on 31/12/15.
 */
@SuppressWarnings("unchecked")
public class User implements Messagable, TaskScheduler, Listener, UserResolver, MetadataHolder<UserMetadata>,
        SharedObjectAccessor {

    private static final HashSet<Material> SEE_THROUGH = new HashSet<>();

    static {
        SEE_THROUGH.add(Material.AIR);
        SEE_THROUGH.add(Material.WATER);
        SEE_THROUGH.add(Material.STATIONARY_WATER);
    }

    private Game game;
    private GameGroup gameGroup;
    private Team team;
    private Kit kit;
    private UUID uuid;
    private LivingEntity entity;
    private PlayerState playerState;

    private boolean cloaked = false;
    private boolean showCloakedPlayers = false;

    private ScoreboardDisplay scoreboardDisplay;
    private ScoreboardHandler scoreboardHandler;

    private AttackerTracker fireAttacker = new AttackerTracker(this);
    private AttackerTracker witherAttacker = new AttackerTracker(this);
    private AttackerTracker lastAttacker = new AttackerTracker(this);

    private boolean isInGame = false;

    private UpgradeHandler upgradeHandler = new UpgradeHandler(this);

    private CooldownHandler cooldownHandler = new CooldownHandler(this);

    private ClassToInstanceMap<UserMetadata> metadataMap = MutableClassToInstanceMap.create();

    private String name;
    private TaskList userTaskList = new TaskList();
    private TaskList inGameTaskList = new TaskList();
    private ClickableInventory openInventory;
    private Collection<Listener> listeners = new ArrayList<>();
    private Vector inventoryTether;

    public User(Game game, GameGroup gameGroup, Team team, UUID uuid, LivingEntity entity) {
        this.game = game;
        this.gameGroup = gameGroup;
        this.team = team;
        this.uuid = uuid;
        this.entity = entity;

        this.name = entity.getName();
        listeners.add(new UserListener());

        if (isPlayer()) {
            scoreboardDisplay = new ScoreboardDisplay(this, getPlayer());
        }
    }

    public ClickableInventory getOpenInventory() {
        return openInventory;
    }

    public boolean showCloakedPlayers() {
        return showCloakedPlayers;
    }

    public void setShowCloakedPlayers(boolean showCloakedPlayers) {
        this.showCloakedPlayers = showCloakedPlayers;

        for(User u : gameGroup.getUsers()) {
            if(this == u) continue;

            if(!u.isCloaked()) continue;

            if(showCloakedPlayers) showPlayer(u);
            else hidePlayer(u);
        }
    }

    public void setAllowFlight(boolean allowFlight) {
        if(isPlayer()) getPlayer().setAllowFlight(allowFlight);
        else playerState.setAllowFlight(allowFlight);
    }

    public void setFlySpeed(double flySpeed) {
        if(isPlayer()) getPlayer().setFlySpeed((float) flySpeed);
        else playerState.setFlySpeed((float) flySpeed);
    }

    public void resetUserStats(boolean removePotionEffects) {
        ConfigurationSection defaultStats = gameGroup.getSharedObject("user").getConfigurationSection("default_stats");

        setMaxHealth(defaultStats.getDouble("max_health", 10) * 2);
        setHealth(defaultStats.getDouble("health", 10) * 2);
        setFoodLevel((int)(defaultStats.getDouble("food_level", 10) * 2));
        setSaturation(defaultStats.getDouble("saturation", 5.0));
        setFlySpeed(defaultStats.getDouble("fly_speed", 0.1));
        setWalkSpeed(defaultStats.getDouble("walk_speed", 0.1));

        if(removePotionEffects) removePotionEffects();
    }

    public void setMaxHealth(double maxHealth) {
        entity.setMaxHealth(maxHealth);
    }

    public void setHealth(double health) {
        entity.setHealth(health);
    }

    public void setFoodLevel(int foodLevel) {
        if(isPlayer()) getPlayer().setFoodLevel(foodLevel);
        else playerState.setFoodLevel(foodLevel);
    }

    public void setSaturation(double saturation) {
        if(isPlayer()) getPlayer().setSaturation((float) saturation);
        else playerState.setSaturation((float) saturation);
    }

    public void setWalkSpeed(double walkSpeed) {
        if(isPlayer()) getPlayer().setWalkSpeed((float) walkSpeed);
        else playerState.setWalkSpeed((float) walkSpeed);
    }

    public void removePotionEffects() {
        List<PotionEffect> effects = new ArrayList<>(entity.getActivePotionEffects());

        for (PotionEffect effect : effects) {
            entity.removePotionEffect(effect.getType());
        }

        entity.setFireTicks(0);
    }

    public void setCollidesWithEntities(boolean collides) {
        if(!isPlayer()) return;

        getPlayer().spigot().setCollidesWithEntities(collides);
    }

    public void setFlying(boolean flying) {
        if(!isPlayer()) return;
        getPlayer().setFlying(flying);
    }

    public void decloak() {
        cloaked = false;

        for (User u : gameGroup.getUsers()) {
            if (this == u) continue;

            u.showPlayer(this);
        }
    }

    public boolean isCloaked() {
        return cloaked;
    }

    private void hidePlayer(User other) {
        if(!isPlayer() || !other.isPlayer()) return;
        getPlayer().hidePlayer(other.getPlayer());
    }

    private void showPlayer(User other) {
        if(!isPlayer() || !other.isPlayer()) return;
        getPlayer().showPlayer(other.getPlayer());
    }

    public void cloak() {
        cloaked = true;

        for (User u : gameGroup.getUsers()) {
            if (this == u) continue;

            if(u.showCloakedPlayers()) continue;

            u.hidePlayer(this);
        }
    }

    public boolean isPlayer() {
        return entity instanceof Player;
    }

    protected Player getPlayer() {
        if (!isPlayer()) throw new RuntimeException("You have no player");
        return (Player) entity;
    }

    public String getTeamName() {
        return team != null ? team.getName() : null;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        if (team == this.team) return;

        Team oldTeam = this.team;
        Team newTeam = this.team = team;

        if (oldTeam != null) oldTeam.removeUser(this);
        if (newTeam != null) newTeam.addUser(this);

        UserChangeTeamEvent event = new UserChangeTeamEvent(this, oldTeam, newTeam);
        gameGroup.userEvent(event);
    }

    public void setKit(Kit kit) {
        if(kit == this.kit) return;

        Kit oldKit = this.kit;
        Kit newKit = this.kit = kit;

        if(oldKit != null) {
            this.listeners.removeAll(oldKit.getListeners());
        }

        if(newKit != null) {
            this.listeners.addAll(newKit.getListeners());
        }

        UserChangeKitEvent event = new UserChangeKitEvent(this, oldKit, newKit);
        gameGroup.userEvent(event);
    }

    public Kit getKit() {
        return kit;
    }

    public TeamIdentifier getTeamIdentifier() {
        return team != null ? team.getTeamIdentifier() : null;
    }

    public Collection<Listener> getListeners() {
        return listeners;
    }

    public GameGroup getGameGroup() {
        return gameGroup;
    }

    @Override
    public User getUser(UUID uuid) {
        return gameGroup.getUser(uuid);
    }

    public boolean isInGame() {
        return isInGame;
    }

    @SuppressWarnings("unchecked")
    public void setInGame(boolean inGame) {
        isInGame = inGame;

        inGameTaskList.cancelAllTasks();

        gameGroup.userEvent(new UserInGameChangeEvent(this));
    }

    @Override
    public <B extends UserMetadata> void setMetadata(B metadata) {
        metadataMap.put(metadata.getMetadataClass(), metadata);
    }

    @Override
    public <B extends UserMetadata> B getMetadata(Class<? extends B> clazz) {
        return metadataMap.getInstance(clazz);
    }

    public void setScoreboardHandler(ScoreboardHandler scoreboardHandler) {
        this.scoreboardHandler = scoreboardHandler;
        if (scoreboardDisplay != null) {
            if (scoreboardHandler == null) scoreboardDisplay.remove();
            else scoreboardHandler.setupScoreboard(this, scoreboardDisplay);
        }
    }

    public void updateScoreboard() {
        if (scoreboardDisplay == null || scoreboardHandler == null) return;

        scoreboardHandler.updateScoreboard(this, scoreboardDisplay);
    }

    @Override
    public void sendLocale(String locale, Object... args) {
        sendMessage(gameGroup.getLocale(locale, args));
    }

    @Override
    public void sendMessage(String message) {
        sendMessageNoPrefix(gameGroup.getChatPrefix() + message);
    }

    @Override
    public void sendMessageNoPrefix(String message) {
        entity.sendMessage(message);
    }

    @Override
    public void sendLocaleNoPrefix(String locale, Object... args) {
        sendMessageNoPrefix(gameGroup.getLocale(locale, args));
    }

    public GameMode getGameMode() {
        return isPlayer() ? getPlayer().getGameMode() : playerState.getGameMode();
    }

    public void setGameMode(GameMode gameMode) {
        if (isPlayer()) getPlayer().setGameMode(gameMode);
        else playerState.setGameMode(gameMode);
    }

    public int getFireTicks() {
        return entity.getFireTicks();
    }

    public void setFireTicks(User fireAttacker, int fireTicks) {
        this.fireAttacker.setAttacker(fireAttacker, fireTicks);
        entity.setFireTicks(fireTicks);
    }

    public void setWitherTicks(User witherAttacker, int witherTicks) {
        setWitherTicks(witherAttacker, witherTicks, 0);
    }

    /**
     * @param witherAmplifier The amplifier for the effect. Level n is amplifier n-1.
     */
    public void setWitherTicks(User witherAttacker, int witherTicks, int witherAmplifier) {
        this.witherAttacker.setAttacker(witherAttacker, witherTicks);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherAmplifier, witherAmplifier));
    }

    public void setLastAttacker(User lastAttacker) {
        this.lastAttacker.setAttacker(lastAttacker);
    }

    public boolean startCoolDown(String ability, int seconds, String coolDownLocale) {
        return cooldownHandler.startCoolDown(ability, seconds, coolDownLocale);
    }

    public void stopCoolDown(String ability, String stopLocale) {
        cooldownHandler.stopCoolDown(ability, stopLocale);
    }

    public void playSound(Location location, SoundEffect sound) {
        if (!isPlayer()) return;

        getPlayer().playSound(location, sound.getSound(), sound.getVolume(), sound.getPitch());
    }

    public boolean isCoolingDown(String ability) {
        return cooldownHandler.isCoolingDown(ability);
    }

    public int getUpgradeLevel(String upgrade) {
        return upgradeHandler.getUpgradeLevel(upgrade);
    }

    public void setUpgradeLevel(String upgrade, int level) {
        upgradeHandler.setUpgradeLevel(upgrade, level);
    }

    public ItemStack createCustomItemForUser(CustomItem item) {
        return item.createWithVariables(gameGroup, upgradeHandler);
    }

    public UpgradeHandler getUpgradeLevels() {
        return upgradeHandler;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean teleport(Vector loc) {
        Location target =
                new Location(getLocation().getWorld(), loc.getX(), loc.getY(), loc.getZ(), getLocation().getYaw(),
                        getLocation().getPitch());
        return teleport(target);

    }

    public Location getLocation() {
        return entity.getLocation();
    }

    @SuppressWarnings("unchecked")
    public boolean teleport(Location location) {
        UserTeleportEvent event = new UserTeleportEvent(this, getLocation(), location);

        gameGroup.userEvent(event);

        if (event.isCancelled()) return false;
        return entity.teleport(event.getTo());
    }

    public boolean isViewingClickableInventory() {
        return openInventory != null;
    }

    public ClickableInventory getClickableInventory() {
        return openInventory;
    }

    @SuppressWarnings("unchecked")
    public void showInventory(ClickableInventory inventory, Location inventoryTether) {
        doInFuture(task -> {
            if (!isPlayer()) return;

            this.openInventory = inventory;
            this.inventoryTether = inventoryTether.toVector();
            getPlayer().openInventory(inventory.createInventory(this));
        });
    }

    public void redoInventory() {
        if(this.openInventory == null) return;

        showInventory(this.openInventory, getInventoryTether());
    }

    public Location getInventoryTether() {
        return gameGroup.getCurrentMap().getLocation(inventoryTether);
    }

    @Override
    public GameTask doInFuture(GameRunnable task) {
        GameTask gameTask = gameGroup.doInFuture(task);

        userTaskList.addTask(gameTask);
        return gameTask;
    }

    public String getFormattedName() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public void setXpLevel(int level) {
        if (isPlayer()) getPlayer().setLevel(level);
        else playerState.setLevel(level);
    }

    public void bindTaskToInGame(GameTask task) {
        inGameTaskList.addTask(task);
    }

    public void makeEntityRepresentUser(Entity entity) {
        gameGroup.getGame().makeEntityRepresentUser(this, entity);
    }

    public Block rayTraceBlocks(int maxDistance) {
        return entity.getTargetBlock(SEE_THROUGH, maxDistance);
    }

    public boolean hasPermission(String permission) {
        return entity.hasPermission(permission);
    }

    @Override
    public GameTask doInFuture(GameRunnable task, int delay) {
        GameTask gameTask = gameGroup.doInFuture(task, delay);

        userTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public GameTask repeatInFuture(GameRunnable task, int delay, int period) {
        GameTask gameTask = gameGroup.repeatInFuture(task, delay, period);

        userTaskList.addTask(gameTask);
        return gameTask;
    }

    @Override
    public void cancelAllTasks() {
        userTaskList.cancelAllTasks();
    }

    public Inventory createInventory(int size, String title) {
        size = ((size / 9) + 1) * 9;

        return Bukkit.createInventory((InventoryHolder) entity, size, title);
    }

    public void closeInventory() {
        doInFuture(task -> {
            if (!isPlayer()) return;

            openInventory = null;
            inventoryTether = null;
            getPlayer().closeInventory();
        });
    }

    @Override
    public boolean hasMetadata(Class<? extends UserMetadata> clazz) {
        return metadataMap.containsKey(clazz);
    }

    @Override
    public LanguageLookup getLanguageLookup() {
        return gameGroup.getLanguageLookup();
    }

    @Override
    public ConfigurationSection getSharedObject(String name) {
        return gameGroup.getSharedObject(name);
    }

    public void giveColoredArmor(Color color, boolean unbreakable) {
        PlayerInventory inv = getInventory();
        if (color == null) {
            inv.setHelmet(null);
            inv.setChestplate(null);
            inv.setLeggings(null);
            inv.setBoots(null);
        } else {
            inv.setHelmet(setUnbreakable(createLeatherArmorItem(Material.LEATHER_HELMET, color), unbreakable));
            inv.setChestplate(setUnbreakable(createLeatherArmorItem(Material.LEATHER_CHESTPLATE, color), unbreakable));
            inv.setLeggings(setUnbreakable(createLeatherArmorItem(Material.LEATHER_LEGGINGS, color), unbreakable));
            inv.setBoots(setUnbreakable(createLeatherArmorItem(Material.LEATHER_BOOTS, color), unbreakable));
        }
    }

    public PlayerInventory getInventory() {
        return isPlayer() ? getPlayer().getInventory() : playerState.getInventory();
    }

    public String getKitName() {
        return kit != null ? kit.getName() : null;
    }

    private class UserListener implements Listener {

        @EventHandler
        public void eventInGameChange(UserInGameChangeEvent event) {
            Iterator<UserMetadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                UserMetadata metadata = iterator.next();

                if (metadata.removeOnInGameChange(event)) iterator.remove();
            }
        }

        @EventHandler
        public void eventGameStateChange(GameStateChangedEvent event) {
            Iterator<UserMetadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                UserMetadata metadata = iterator.next();

                if (metadata.removeOnGameStateChange(event)) iterator.remove();
            }
        }

        @EventHandler
        public void eventMapChange(MapChangedEvent event) {
            Iterator<UserMetadata> iterator = metadataMap.values().iterator();

            while (iterator.hasNext()) {
                UserMetadata metadata = iterator.next();

                if (metadata.removeOnMapChange(event)) iterator.remove();
            }
        }

        @EventHandler
        public void eventInventoryClick(UserInventoryClickEvent event) {
            if (!isViewingClickableInventory()) return;

            getClickableInventory().inventoryClick(event);
        }

        @EventHandler
        public void eventInventoryClose(UserInventoryCloseEvent event) {
            if (!isViewingClickableInventory()) return;

            openInventory = null;
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void eventInteract(UserInteractEvent event) {
            ItemStack item = getInventory().getItemInHand();
            int identifier = InventoryUtils.getIdentifier(item);
            if (identifier < 0) return;

            CustomItem customItem = gameGroup.getCustomItem(identifier);

            //If event is a UserAttackEvent this will call both event handler methods in CustomItem
            EventExecutor.executeEvent(event, customItem);
        }

        @EventHandler
        public void eventAbilityCooldown(UserAbilityCooldownEvent event) {
            for (ItemStack item : getInventory()) {
                int identifier = InventoryUtils.getIdentifier(item);
                if (identifier < 0) continue;

                CustomItem customItem = gameGroup.getCustomItem(identifier);

                EventExecutor.executeEvent(event, customItem);
            }
        }
    }
}

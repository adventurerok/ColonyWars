package com.ithinkrok.minigames;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.game.UserAbilityCooldownEvent;
import com.ithinkrok.minigames.event.user.game.UserInGameChangeEvent;
import com.ithinkrok.minigames.event.user.game.UserTeleportEvent;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryClickEvent;
import com.ithinkrok.minigames.event.user.inventory.UserInventoryCloseEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.item.ClickableInventory;
import com.ithinkrok.minigames.item.CustomItem;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.metadata.UserMetadata;
import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskList;
import com.ithinkrok.minigames.task.TaskScheduler;
import com.ithinkrok.minigames.user.*;
import com.ithinkrok.minigames.util.EventExecutor;
import com.ithinkrok.minigames.util.InventoryUtils;
import com.ithinkrok.minigames.util.SoundEffect;
import com.ithinkrok.minigames.util.playerstate.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

import java.util.*;

/**
 * Created by paul on 31/12/15.
 */
@SuppressWarnings("unchecked")
public abstract class User<U extends User<U, T, G, M>, T extends Team<U, T, G>, G extends GameGroup<U, T, G, M>, M extends Game<U, T, G, M>>
        implements Messagable, TaskScheduler, Listener, UserResolver<U> {

    private static final HashSet<Material> SEE_THROUGH = new HashSet<>();

    static {
        SEE_THROUGH.add(Material.AIR);
        SEE_THROUGH.add(Material.WATER);
        SEE_THROUGH.add(Material.STATIONARY_WATER);
    }

    private M game;
    private G gameGroup;
    private T team;
    private UUID uuid;
    private LivingEntity entity;
    private PlayerState playerState;

    private AttackerTracker<U> fireAttacker = new AttackerTracker<>((U) this);
    private AttackerTracker<U> witherAttacker = new AttackerTracker<>((U) this);
    private AttackerTracker<U> lastAttacker = new AttackerTracker<>((U) this);

    private boolean isInGame = false;

    private UpgradeHandler<U> upgradeHandler = new UpgradeHandler<>((U) this);

    private CooldownHandler<U> cooldownHandler = new CooldownHandler<>((U) this);

    private ClassToInstanceMap<UserMetadata> metadataMap = MutableClassToInstanceMap.create();

    private String name;

    private TaskList userTaskList = new TaskList();
    private TaskList inGameTaskList = new TaskList();
    private ClickableInventory<U> openInventory;
    private Collection<Listener> listeners = new ArrayList<>();

    public User(M game, G gameGroup, T team, UUID uuid, LivingEntity entity) {
        this.game = game;
        this.gameGroup = gameGroup;
        this.team = team;
        this.uuid = uuid;
        this.entity = entity;

        this.name = entity.getName();
        listeners.add(new UserListener());
    }

    public Collection<Listener> getListeners() {
        return listeners;
    }

    public G getGameGroup() {
        return gameGroup;
    }

    @Override
    public U getUser(UUID uuid) {
        return gameGroup.getUser(uuid);
    }

    public boolean isInGame() {
        return isInGame;
    }

    @SuppressWarnings("unchecked")
    public void setInGame(boolean inGame) {
        isInGame = inGame;

        inGameTaskList.cancelAllTasks();

        gameGroup.userEvent(new UserInGameChangeEvent<>((U) this));
    }

    public <B extends UserMetadata> void setMetadata(B metadata) {
        metadataMap.put(metadata.getMetadataClass(), metadata);
    }

    public <B extends UserMetadata> B getMetadata(Class<B> clazz) {
        return metadataMap.getInstance(clazz);
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

    public PlayerInventory getInventory() {
        return isPlayer() ? getPlayer().getInventory() : playerState.getInventory();
    }

    public boolean isPlayer() {
        return entity instanceof Player;
    }

    protected Player getPlayer() {
        if (!isPlayer()) throw new RuntimeException("You have no player");
        return (Player) entity;
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

    public void setFireTicks(U fireAttacker, int fireTicks) {
        this.fireAttacker.setAttacker(fireAttacker, fireTicks);
        entity.setFireTicks(fireTicks);
    }

    public void setWitherTicks(U witherAttacker, int witherTicks) {
        setWitherTicks(witherAttacker, witherTicks, 0);
    }

    /**
     *
     * @param witherAmplifier The amplifier for the effect. Level n is amplifier n-1.
     */
    public void setWitherTicks(U witherAttacker, int witherTicks, int witherAmplifier) {
        this.witherAttacker.setAttacker(witherAttacker, witherTicks);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherAmplifier, witherAmplifier));
    }

    public void setLastAttacker(U lastAttacker) {
        this.lastAttacker.setAttacker(lastAttacker);
    }

    public boolean startCoolDown(String ability, int seconds, String coolDownLocale) {
        return cooldownHandler.startCoolDown(ability, seconds, coolDownLocale);
    }

    public void stopCoolDown(String ability, String stopLocale) {
        cooldownHandler.stopCoolDown(ability, stopLocale);
    }

    public void playSound(Location location, SoundEffect sound) {
        if(!isPlayer()) return;

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

    public UpgradeHandler<U> getUpgradeLevels() {
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
        UserTeleportEvent<U> event = new UserTeleportEvent<>((U) this, getLocation(), location);

        gameGroup.userEvent(event);

        if (event.isCancelled()) return false;
        return entity.teleport(event.getTo());
    }


    public boolean isViewingClickableInventory() {
        return openInventory != null;
    }

    public ClickableInventory<U> getClickableInventory() {
        return openInventory;
    }

    @SuppressWarnings("unchecked")
    public void showInventory(ClickableInventory<U> inventory) {
        if (!isPlayer()) return;

        this.openInventory = inventory;
        getPlayer().openInventory(inventory.createInventory((U) this));
    }

    public String getFormattedName() {
        return getName();
    }

    public String getName() {
        return name;
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
    public GameTask doInFuture(GameRunnable task) {
        GameTask gameTask = gameGroup.doInFuture(task);

        userTaskList.addTask(gameTask);
        return gameTask;
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

    private class UserListener implements Listener {

        @EventHandler
        public void eventInGameChange(UserInGameChangeEvent<U> event) {
            Iterator<UserMetadata> iterator = metadataMap.values().iterator();

            while(iterator.hasNext()) {
                UserMetadata metadata = iterator.next();

                if(metadata.removeOnInGameChange(event)) iterator.remove();
            }
        }

        @EventHandler
        public void eventGameStateChange(GameStateChangedEvent<G> event) {
            Iterator<UserMetadata> iterator = metadataMap.values().iterator();

            while(iterator.hasNext()) {
                UserMetadata metadata = iterator.next();

                if(metadata.removeOnGameStateChange(event)) iterator.remove();
            }
        }

        @EventHandler
        public void eventMapChange(MapChangedEvent<G> event) {
            Iterator<UserMetadata> iterator = metadataMap.values().iterator();

            while(iterator.hasNext()) {
                UserMetadata metadata = iterator.next();

                if(metadata.removeOnMapChange(event)) iterator.remove();
            }
        }

        @EventHandler
        public void eventInventoryClick(UserInventoryClickEvent<U> event) {
            if (!isViewingClickableInventory()) return;

            getClickableInventory().inventoryClick(event);
        }

        @EventHandler
        public void eventInventoryClose(UserInventoryCloseEvent<U> event) {
            if(!isViewingClickableInventory()) return;

            openInventory = null;
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void eventInteract(UserInteractEvent<U> event) {
            ItemStack item = getInventory().getItemInHand();
            int identifier = InventoryUtils.getIdentifier(item);
            if(identifier < 0) return;

            CustomItem customItem = gameGroup.getCustomItem(identifier);

            //If event is a UserAttackEvent this will call both event handler methods in CustomItem
            EventExecutor.executeEvent(event, customItem);
        }

        @EventHandler
        public void eventAbilityCooldown(UserAbilityCooldownEvent<U> event) {
            for(ItemStack item : getInventory()) {
                int identifier = InventoryUtils.getIdentifier(item);
                if(identifier < 0) continue;

                CustomItem customItem = gameGroup.getCustomItem(identifier);

                EventExecutor.executeEvent(event, customItem);
            }
        }
    }
}

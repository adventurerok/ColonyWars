package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.user.UserInGameChangeEvent;
import com.ithinkrok.minigames.event.user.UserTeleportEvent;
import com.ithinkrok.minigames.item.ClickableInventory;
import com.ithinkrok.minigames.lang.Messagable;
import com.ithinkrok.minigames.task.GameRunnable;
import com.ithinkrok.minigames.task.GameTask;
import com.ithinkrok.minigames.task.TaskList;
import com.ithinkrok.minigames.task.TaskScheduler;
import com.ithinkrok.minigames.util.playerstate.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Created by paul on 31/12/15.
 */
public abstract class User<U extends User<U, T, G, M>, T extends Team<U, T, G>, G extends GameGroup<U, T, G, M>, M extends Game<U, T, G, M>>
        implements Messagable, TaskScheduler {

    private M game;
    private G gameGroup;
    private T team;
    private UUID uuid;
    private LivingEntity entity;
    private PlayerState playerState;

    private UUID fireAttacker;
    private int fireAttackerTimer;

    private boolean isInGame = false;

    private String name;

    private TaskList userTaskList = new TaskList();
    private TaskList inGameTaskList = new TaskList();
    private ClickableInventory<U> openInventory;

    public User(M game, G gameGroup, T team, UUID uuid, LivingEntity entity) {
        this.game = game;
        this.gameGroup = gameGroup;
        this.team = team;
        this.uuid = uuid;
        this.entity = entity;

        this.name = entity.getName();
    }

    public G getGameGroup() {
        return gameGroup;
    }

    public UUID getUuid() {
        return uuid;
    }

    public U getOther(UUID uuid) {
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

    protected Player getPlayer() {
        if (!isPlayer()) throw new RuntimeException("You have no player");
        return (Player) entity;
    }

    public boolean isPlayer() {
        return entity instanceof Player;
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
    public void sendLocale(String locale, Object...args) {
        sendMessage(gameGroup.getLocale(locale, args));
    }

    @Override
    public void sendLocaleNoPrefix(String locale, Object...args) {
        sendMessageNoPrefix(gameGroup.getLocale(locale, args));
    }

    public PlayerInventory getInventory() {
        return isPlayer() ? getPlayer().getInventory() : playerState.getInventory();
    }

    public GameMode getGameMode() {
        return isPlayer() ? getPlayer().getGameMode() : playerState.getGameMode();
    }

    public void setGameMode(GameMode gameMode) {
        if(isPlayer()) getPlayer().setGameMode(gameMode);
        else playerState.setGameMode(gameMode);
    }

    public int getFireTicks() {
        return entity.getFireTicks();
    }

    public void setFireAttacker(U fireAttacker) {
        if(fireAttacker != null) {
            this.fireAttacker = fireAttacker.getUuid();
            this.fireAttackerTimer = 30; //TODO get from config
        } else {
            this.fireAttacker = null;
            this.fireAttackerTimer = 0;
        }
    }

    public void setFireTicks(U fireAttacker, int fireTicks) {
        setFireAttacker(fireAttacker);
        entity.setFireTicks(fireTicks);
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

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public void showInventory(ClickableInventory<U> inventory) {
        if(!isPlayer()) return;

        this.openInventory = inventory;
        getPlayer().openInventory(inventory.createInventory((U) this));
    }

    public String getFormattedName() {
        return getName();
    }

    public void bindTaskToInGame(GameTask task) {
        inGameTaskList.addTask(task);
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
}

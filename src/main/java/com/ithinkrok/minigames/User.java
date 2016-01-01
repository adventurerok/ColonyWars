package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.UserInGameChangeEvent;
import com.ithinkrok.minigames.event.UserTeleportEvent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Created by paul on 31/12/15.
 */
public abstract class User<U extends User<U, T, G, M>, T extends Team<U, T, G>, G extends GameGroup<U, T, G, M>, M
        extends Game<U, T, G, M>> implements Listener{

    private M game;
    private G gameGroup;
    private T team;
    private UUID uuid;
    private LivingEntity entity;

    private boolean isInGame = false;

    public User(M game, G gameGroup, T team, UUID uuid, LivingEntity entity) {
        this.game = game;
        this.gameGroup = gameGroup;
        this.team = team;
        this.uuid = uuid;
        this.entity = entity;
    }

    public G getGameGroup() {
        return gameGroup;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isInGame() {
        return isInGame;
    }

    @SuppressWarnings("unchecked")
    public void setInGame(boolean inGame) {
        isInGame = inGame;

        gameGroup.userEvent(new UserInGameChangeEvent<>((U) this));
    }

    public boolean isPlayer(){
        return entity instanceof Player;
    }

    protected Player getPlayer(){
        if(!isPlayer()) throw new RuntimeException("You have no player");
        return (Player) entity;
    }

    public void sendMessageNoPrefix(String message) {
        entity.sendMessage(message);
    }

    public void sendMessage(String message) {
        sendMessageNoPrefix(game.getChatPrefix() + message);
    }

    @SuppressWarnings("unchecked")
    public boolean teleport(Location location) {
        UserTeleportEvent<U> event = new UserTeleportEvent<>((U) this, getLocation(), location);

        gameGroup.userEvent(event);

        if(event.isCancelled()) return false;
        return entity.teleport(event.getTo());
    }

    public Location getLocation() {
        return entity.getLocation();
    }
}

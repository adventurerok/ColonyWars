package com.ithinkrok.minigames;

import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * Created by paul on 31/12/15.
 */
public abstract class User<U extends User, T extends Team, G extends GameGroup, M extends Game> {

    private M minigame;
    private G gameGroup;
    private T team;
    private UUID uuid;
    private LivingEntity entity;

    private boolean isInGame = false;

    public User(M minigame, G gameGroup, T team, UUID uuid, LivingEntity entity) {
        this.minigame = minigame;
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

    public void setInGame(boolean inGame) {
        isInGame = inGame;
    }
}

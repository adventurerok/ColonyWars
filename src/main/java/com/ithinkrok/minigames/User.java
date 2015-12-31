package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.UserInGameChangeEvent;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 * Created by paul on 31/12/15.
 */
public abstract class User<U extends User<U, T, G, M>, T extends Team<U, T, G>, G extends GameGroup<U, T, G, M>, M extends Game<U, T, G, M>> {

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

    @SuppressWarnings("unchecked")
    public void setInGame(boolean inGame) {
        isInGame = inGame;

        gameGroup.eventUserInGameChanged(new UserInGameChangeEvent<>((U) this));
    }
}

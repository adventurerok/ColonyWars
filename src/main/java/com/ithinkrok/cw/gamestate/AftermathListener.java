package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.GameState;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.minigames.base.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.user.UserEvent;
import com.ithinkrok.minigames.base.event.user.world.UserChatEvent;
import com.ithinkrok.minigames.base.task.GameTask;
import com.ithinkrok.minigames.base.util.MinigamesConfigs;
import com.ithinkrok.minigames.base.util.CountdownConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.Cancellable;

import java.util.Objects;

/**
 * Created by paul on 16/01/16.
 */
public class AftermathListener extends BaseGameStateListener {

    private CountdownConfig countdown;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        super.onListenerLoaded(event);
        ConfigurationSection config = event.getConfig();
        if (config == null) config = new MemoryConfiguration();

        countdown = MinigamesConfigs.getCountdown(config, "countdown", "aftermath", 15, "countdowns.aftermath");

    }

    @CustomEventHandler
    public void onGameStateChanged(GameStateChangedEvent event) {
        if (!Objects.equals(event.getNewGameState(), gameState)) return;


        //Remove user scoreboards
        for (User user : event.getGameGroup().getUsers()) {
            user.setScoreboardHandler(null);
        }

        event.getGameGroup().startCountdown(countdown);

        GameTask task = event.getGameGroup().repeatInFuture(t -> {
            if (t.getRunCount() > 5) t.finish();

            for (User user : event.getGameGroup().getUsers()) {
                if (!user.isInGame()) continue;

                user.launchVictoryFirework();
            }

        }, 20, 20);

        event.getGameGroup().bindTaskToCurrentGameState(task);
    }

    @CustomEventHandler
    public void onCountdownFinished(CountdownFinishedEvent event) {
        if (!event.getCountdown().getName().equals(countdown.getName())) return;

        //event.getGameGroup().changeGameState(lobbyGameState);
        event.getGameGroup().kill();
    }

    @CustomEventHandler
    public void onUserEvent(UserEvent event) {
        if (!(event instanceof Cancellable) || event instanceof UserChatEvent) return;

        ((Cancellable) event).setCancelled(true);
    }

}

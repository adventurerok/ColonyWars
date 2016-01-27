package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.GameState;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.event.user.world.UserChatEvent;
import com.ithinkrok.minigames.task.GameTask;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.Objects;

/**
 * Created by paul on 16/01/16.
 */
public class AftermathListener extends BaseGameStateListener {

    private String aftermathCountdownName;
    private String aftermathCountdownLocaleStub;
    private int aftermathCountdownSeconds;

    private String lobbyGameState;

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        super.onListenerLoaded(event);
        ConfigurationSection config = event.getConfig();
        if (config == null) config = new MemoryConfiguration();

        aftermathCountdownName = config.getString("countdown.name", "aftermath");
        aftermathCountdownLocaleStub = config.getString("countdown.locale_stub", "countdowns.aftermath");
        aftermathCountdownSeconds = config.getInt("countdown.seconds", 15);

        lobbyGameState = config.getString("lobby_gamestate", "lobby");
    }

    @MinigamesEventHandler
    public void onGameStateChanged(GameStateChangedEvent event) {
        if (!Objects.equals(event.getNewGameState(), gameState)) return;


        //Remove user scoreboards
        for(User user : event.getGameGroup().getUsers()) {
            user.setScoreboardHandler(null);
        }

        event.getGameGroup()
                .startCountdown(aftermathCountdownName, aftermathCountdownLocaleStub, aftermathCountdownSeconds);

        GameTask task = event.getGameGroup().repeatInFuture(t -> {
            if (t.getRunCount() > 5) t.finish();

            for (User user : event.getGameGroup().getUsers()) {
                if (!user.isInGame()) continue;

                Location loc = user.getLocation();

                Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

                Color color = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
                Color fade = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));

                firework.setVelocity(new Vector(0, 0.5f, 0));
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BURST).trail(true).withColor(color)
                        .withFade(fade).build());
                firework.setFireworkMeta(meta);
            }

        }, 20, 20);

        event.getGameGroup().bindTaskToCurrentGameState(task);
    }

    @MinigamesEventHandler
    public void onCountdownFinished(CountdownFinishedEvent event) {
        if (!event.getCountdown().getName().equals(aftermathCountdownName)) return;

        //event.getGameGroup().changeGameState(lobbyGameState);
        event.getGameGroup().kill();
    }

    @MinigamesEventHandler
    public void onUserEvent(UserEvent event) {
        if (!(event instanceof Cancellable) || event instanceof UserChatEvent) return;

        ((Cancellable) event).setCancelled(true);
    }

}

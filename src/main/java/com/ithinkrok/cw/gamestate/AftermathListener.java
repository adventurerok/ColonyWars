package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.user.UserEvent;
import com.ithinkrok.minigames.task.GameTask;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 16/01/16.
 */
public class AftermathListener extends BaseGameStateListener {

    private String aftermathCountdownName;
    private String aftermathCountdownLocaleStub;
    private int aftermathCountdownSeconds;

    private String lobbyGameState;

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?> event) {
        ConfigurationSection config = event.getConfig();
        if (config == null) config = new MemoryConfiguration();

        aftermathCountdownName = config.getString("aftermath_countdown.name", "aftermath");
        aftermathCountdownLocaleStub = config.getString("aftermath_countdown.locale_stub", "countdowns.aftermath");
        aftermathCountdownSeconds = config.getInt("aftermath_countdown.seconds", 15);

        lobbyGameState = config.getString("lobby_gamestate", "lobby");
    }

    @EventHandler
    public void onGameStateChanged(GameStateChangedEvent event) {
        if (!event.getNewGameState().isGameStateListener(this)) return;

        event.getGameGroup()
                .startCountdown(aftermathCountdownName, aftermathCountdownLocaleStub, aftermathCountdownSeconds);

        GameTask task = event.getGameGroup().repeatInFuture(t -> {
            if(t.getRunCount() > 5) t.finish();

            for(User user : event.getGameGroup().getUsers()) {
                if(!user.isInGame()) continue;

                Location loc = user.getLocation();

                Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);

                Color color = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));
                Color fade = Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255));

                firework.setVelocity(new Vector(0, 0.5f, 0));
                FireworkMeta meta = firework.getFireworkMeta();
                meta.addEffect(
                        FireworkEffect.builder().with(FireworkEffect.Type.BURST).trail(true).withColor(color).withFade(fade)
                                .build());
                firework.setFireworkMeta(meta);
            }

        }, 20, 20);

        event.getGameGroup().bindTaskToCurrentGameState(task);
    }

    @EventHandler
    public void onCountdownFinished(CountdownFinishedEvent event) {
        if(!event.getCountdown().getName().equals(aftermathCountdownName)) return;

        event.getGameGroup().changeGameState(lobbyGameState);
    }

    @EventHandler
    public void onUserEvent(UserEvent event) {
        if(!(event instanceof Cancellable)) return;

        ((Cancellable) event).setCancelled(true);
    }

}

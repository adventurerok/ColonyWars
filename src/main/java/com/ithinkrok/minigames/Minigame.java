package com.ithinkrok.minigames;

import com.ithinkrok.minigames.event.UserBreakBlockEvent;
import com.ithinkrok.minigames.event.UserJoinEvent;
import com.ithinkrok.minigames.event.UserPlaceBlockEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by paul on 31/12/15.
 */
public abstract class Minigame<U extends User<U, T, G, M>, T extends Team<U, T, G>, G extends GameGroup<U, T, G, M>,
        M extends Minigame<U, T, G, M>> implements Listener {

    private ConcurrentMap<UUID, U> usersInServer = new ConcurrentHashMap<>();
    private List<G> gameGroups = new ArrayList<>();

    private Constructor<G> gameGroupConstructor;
    private Constructor<T> teamConstructor;
    private Constructor<U> userConstructor;

    private Plugin plugin;

    private G spawnGameGroup;

    public Minigame(Plugin plugin, Class<G> gameGroupClass, Class<T> teamClass, Class<U> userClass) {
        this.plugin = plugin;

        try {
            teamConstructor = teamClass.getConstructor(TeamColor.class, gameGroupClass);
            userConstructor =
                    userClass.getConstructor(getClass(), gameGroupClass, teamClass, UUID.class, LivingEntity.class);
            gameGroupConstructor = gameGroupClass.getConstructor(getClass(), teamConstructor.getClass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to get constructors for data classes", e);
        }

        spawnGameGroup = createGameGroup();
        gameGroups.add(spawnGameGroup);
    }


    private G createGameGroup() {
        try {
            return gameGroupConstructor.newInstance(this, teamConstructor);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to construct GameGroup", e);
        }
    }

    private U createUser(G gameGroup, T team, UUID uuid, LivingEntity entity) {
        try {
            U user = userConstructor.newInstance(this, gameGroup, team, uuid, entity);
            usersInServer.put(uuid, user);
            return user;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to construct User", e);
        }
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void eventPlayerJoined(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        U user = getUser(player.getUniqueId());
        G gameGroup = spawnGameGroup;

        if(user != null) {
            gameGroup = user.getGameGroup();
        } else {
            user = createUser(gameGroup, null, player.getUniqueId(), player);
        }

        gameGroup.eventUserJoinedAsPlayer(new UserJoinEvent<>(user));
    }

    @EventHandler
    public void eventBlockBreak(BlockBreakEvent event) {
        event.setCancelled(true);

        U user = getUser(event.getPlayer().getUniqueId());
        user.getGameGroup().eventBlockBreak(new UserBreakBlockEvent<>(user, event));
    }

    @EventHandler
    public void eventBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(true);

        U user = getUser(event.getPlayer().getUniqueId());
        user.getGameGroup().eventBlockPlace(new UserPlaceBlockEvent<>(user, event));
    }

    private U getUser(UUID uuid) {
        return usersInServer.get(uuid);
    }


}

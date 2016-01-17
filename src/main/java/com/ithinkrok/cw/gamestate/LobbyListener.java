package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.event.user.state.UserDamagedEvent;
import com.ithinkrok.minigames.event.user.state.UserFoodLevelChangeEvent;
import com.ithinkrok.minigames.event.user.world.*;
import com.ithinkrok.minigames.listener.GiveCustomItemsOnJoin;
import com.ithinkrok.minigames.metadata.MapVote;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by paul on 31/12/15.
 */
public class LobbyListener extends BaseGameStateListener {

    private static final Random random = new Random();

    private String startCountdownName;
    private String startCountdownLocaleStub;
    private int startCountdownSeconds;
    private String needsMorePlayersLocale;
    private int minPlayersToStartGame;


    private String lobbyMapName;
    private String nextGameState;

    private String joinLobbyLocaleStub;

    private GiveCustomItemsOnJoin.CustomItemGiver giveOnJoin;

    @EventHandler
    public void eventListenerLoaded(ListenerLoadedEvent<?> event) {
        ConfigurationSection config = event.getConfig();

        nextGameState = config.getString("next_gamestate");

        configureCountdown(config.getConfigurationSection("start_countdown"));

        lobbyMapName = config.getString("lobby_map");

        giveOnJoin = new GiveCustomItemsOnJoin.CustomItemGiver(config.getConfigurationSection("give_on_join"));

        joinLobbyLocaleStub = config.getString("join_lobby_locale_stub", "lobby.info");
    }



    private void configureCountdown(ConfigurationSection config) {
        startCountdownName = config.getString("name");
        startCountdownLocaleStub = config.getString("locale_stub");
        startCountdownSeconds = config.getInt("seconds");
        minPlayersToStartGame = config.getInt("min_players");
        needsMorePlayersLocale = config.getString("needs_more_players_locale");
    }

    @EventHandler
    public void eventBlockBreak(UserBreakBlockEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventBlockPlace(UserPlaceBlockEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void eventUserJoin(UserJoinEvent event) {
        userJoinLobby(event.getUser());

        if(event.getUserGameGroup().hasActiveCountdown()) return;

        resetCountdown(event.getUserGameGroup());
    }

    private void userJoinLobby(User user) {
        user.setInGame(false);
        if(!user.isPlayer()) return;

        user.setGameMode(GameMode.ADVENTURE);
        user.setSpectator(false);
        user.resetUserStats(true);

        user.setDisplayName(user.getName());
        user.setTabListName(user.getName());

        user.getInventory().clear();
        user.clearArmor();

        giveOnJoin.giveToUser(user);

        user.teleport(user.getGameGroup().getCurrentMap().getSpawn());

        String message;

        for(int counter = 0; ; ++counter) {
            message = user.getGameGroup().getLocale(joinLobbyLocaleStub + "." + counter);
            if(message == null) break;

            user.sendMessage(message);

        }
    }

    @EventHandler
    public void eventGameStateChanged(GameStateChangedEvent event) {
        if(!event.getNewGameState().isGameStateListener(this)) return;

        event.getGameGroup().changeMap(lobbyMapName);

        resetCountdown(event.getGameGroup());

        for(User user : event.getGameGroup().getUsers()) {
            userJoinLobby(user);
        }
    }

    @EventHandler
    public void eventCountdownFinished(CountdownFinishedEvent event) {
        if(!event.getCountdown().getName().equals(startCountdownName)) return;

        int userCount = event.getGameGroup().getUserCount();
        if(userCount < 1) return;
        if(userCount < minPlayersToStartGame) {
            event.getGameGroup().sendLocale(needsMorePlayersLocale);
            resetCountdown(event.getGameGroup());
            return;
        }

        event.getGameGroup().changeGameState(nextGameState);
    }


    private void resetCountdown(GameGroup gameGroup) {
        gameGroup.startCountdown(startCountdownName, startCountdownLocaleStub, startCountdownSeconds);
    }

    @EventHandler
    public void eventUserDropItem(UserDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserPickupItem(UserPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserDamaged(UserDamagedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void eventUserInteract(UserInteractEvent event) {
        if(event.getInteractType() == UserInteractEvent.InteractType.REPRESENTING) return;
        if(event.hasItem() && event.getItem().getType() == Material.WRITTEN_BOOK) return;

        if(!event.hasBlock() || !isRedstoneControl(event.getClickedBlock().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void eventUserFoodLevelChange(UserFoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    private static boolean isRedstoneControl(Material type) {
        switch (type) {
            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case STONE_PLATE:
            case WOOD_PLATE:
            case GOLD_PLATE:
            case IRON_PLATE:
            case WOOD_DOOR:
            case TRAP_DOOR:
                return true;
            default:
                return false;
        }
    }


}

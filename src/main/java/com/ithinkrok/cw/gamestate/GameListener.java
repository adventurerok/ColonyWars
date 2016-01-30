package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.command.CWCommand;
import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.cw.scoreboard.CWScoreboardHandler;
import com.ithinkrok.minigames.base.GameGroup;
import com.ithinkrok.minigames.base.GameState;
import com.ithinkrok.minigames.base.Kit;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import com.ithinkrok.minigames.base.event.game.CountdownFinishedEvent;
import com.ithinkrok.minigames.base.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.base.event.user.game.UserChangeTeamEvent;
import com.ithinkrok.minigames.base.listener.GiveCustomItemsOnJoin;
import com.ithinkrok.minigames.base.metadata.MapVote;
import com.ithinkrok.minigames.base.team.Team;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by paul on 05/01/16.
 */
public class GameListener extends BaseGameListener {


    private String randomMapName;
    private List<String> mapList;
    private List<String> teamList;
    private List<String> kitList;

    private String teamInfoLocale, kitInfoLocale;

    private GiveCustomItemsOnJoin.CustomItemGiver customItemGiver;

    @MinigamesEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        super.onListenerLoaded(event);
        ConfigurationSection config = event.getConfig();

        teamList = config.getStringList("choosable_teams");
        kitList = config.getStringList("choosable_kits");

        configureMapVoting(config.getConfigurationSection("map_voting"));

        customItemGiver = new GiveCustomItemsOnJoin.CustomItemGiver(config.getConfigurationSection("start_items"));

        teamInfoLocale = config.getString("team_info_locale", "start_info.team");
        kitInfoLocale = config.getString("kit_info_locale", "start_info.kit");
    }

    private void configureMapVoting(ConfigurationSection config) {
        randomMapName = config.getString("random_map");

        mapList = new ArrayList<>(config.getStringList("map_list"));
        mapList.remove(randomMapName);

        if(mapList.size() < 1) throw new RuntimeException("The game requires at least one map!");
    }

    @MinigamesEventHandler
    public void onGameStateChange(GameStateChangedEvent event) {
        if (!Objects.equals(event.getNewGameState(), gameState)) return;
        if(event.getOldGameState() != null && event.getOldGameState().getName().equals(showdownGameState)) return;

        startGame(event.getGameGroup());

        GameGroup gameGroup = event.getGameGroup();
        gameGroup.getUsers().forEach(this::setupUser);

        checkVictory(event.getGameGroup(), false);
    }

    @MinigamesEventHandler
    public void onCountdownFinished(CountdownFinishedEvent event) {
        if(!event.getCountdown().getName().equals(showdownCountdownName)) return;

        event.getGameGroup().changeGameState(showdownGameState);
    }


    @MinigamesEventHandler
    public void onUserRejoin(CWCommand.UserRejoinEvent event) {
        event.setCancelled(false);

        setupUser(event.getUser());
    }

    private void setupUser(User user) {
        user.decloak();

        if(user.getTeam() == null) {
            user.setTeam(assignUserTeam(user.getGameGroup()));
        }

        if(user.getKit() == null) {
            user.setKit(assignUserKit(user.getGameGroup()));
        }

        user.setInGame(true);

        CWTeamStats teamStats = CWTeamStats.getOrCreate(user.getTeam());
        user.teleport(teamStats.getSpawnLocation());

        user.setGameMode(GameMode.SURVIVAL);
        user.setAllowFlight(false);
        user.setCollidesWithEntities(true);

        user.resetUserStats(true);

        //TODO give handbook (will be done from configs)
        customItemGiver.giveToUser(user);

        user.giveColoredArmor(user.getTeam().getArmorColor(), true);
        user.setDisplayName(user.getTeam().getChatColor() + user.getName());
        user.setTabListName(user.getTeam().getChatColor() + user.getName());

        user.setScoreboardHandler(new CWScoreboardHandler(user));
        user.updateScoreboard();

        user.sendLocale(teamInfoLocale, user.getTeamIdentifier().getFormattedName());
        user.sendLocale(kitInfoLocale, user.getKit().getFormattedName());

        StatsHolder statsHolder = StatsHolder.getOrCreate(user);
        statsHolder.addGame();
    }

    private Kit assignUserKit(GameGroup gameGroup) {
        String kitName = kitList.get(random.nextInt(kitList.size()));

        return gameGroup.getKit(kitName);
    }

    private Team assignUserTeam(GameGroup gameGroup) {
        ArrayList<Team> smallest = new ArrayList<>();
        int leastCount = Integer.MAX_VALUE;

        for(String teamName : teamList) {
            Team team = gameGroup.getTeam(teamName);
            if(team.getUserCount() < leastCount) {
                leastCount = team.getUserCount();
                smallest.clear();
            }

            if(team.getUserCount() == leastCount) smallest.add(team);
        }

        return smallest.get(random.nextInt(smallest.size()));
    }

    private void startGame(GameGroup gameGroup) {
        String winningVote = MapVote.getWinningVote(gameGroup.getUsers());

        if(winningVote == null || winningVote.equals(randomMapName)) {
            winningVote = mapList.get(random.nextInt(mapList.size()));
        }

        gameGroup.changeMap(winningVote);
    }

    @MinigamesEventHandler
    public void onUserChangeTeam(UserChangeTeamEvent event) {
        Color armorColor = event.getNewTeam() != null ? event.getNewTeam().getArmorColor() : null;
        event.getUser().giveColoredArmor(armorColor, true);
    }
}

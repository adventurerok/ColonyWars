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
import com.ithinkrok.minigames.base.gamestate.SimpleGameStartListener;
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
public class GameListener extends SimpleGameStartListener {

    @MinigamesEventHandler
    public void onUserRejoin(CWCommand.UserRejoinEvent event) {
        event.setCancelled(false);

        setupUser(event.getUser());
    }

    protected void setupUser(User user) {
        super.setupUser(user);

        CWTeamStats teamStats = CWTeamStats.getOrCreate(user.getTeam());
        user.teleport(teamStats.getSpawnLocation());

        user.setGameMode(GameMode.SURVIVAL);
        user.setAllowFlight(false);

        user.giveColoredArmor(user.getTeam().getArmorColor(), true);
        user.setDisplayName(user.getTeam().getChatColor() + user.getName());
        user.setTabListName(user.getTeam().getChatColor() + user.getName());

        user.setScoreboardHandler(new CWScoreboardHandler(user));
        user.updateScoreboard();


        StatsHolder statsHolder = StatsHolder.getOrCreate(user);
        statsHolder.addGame();
    }

    @MinigamesEventHandler
    public void onUserChangeTeam(UserChangeTeamEvent event) {
        Color armorColor = event.getNewTeam() != null ? event.getNewTeam().getArmorColor() : null;
        event.getUser().giveColoredArmor(armorColor, true);
    }
}

package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.command.CWCommand;
import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.cw.metadata.StatsHolder;
import com.ithinkrok.cw.scoreboard.CWScoreboardHandler;
import com.ithinkrok.minigames.api.event.user.game.UserChangeTeamEvent;
import com.ithinkrok.minigames.api.user.UserVariableHandler;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.util.gamestate.SimpleGameStartListener;
import com.ithinkrok.util.event.CustomEventHandler;
import org.bukkit.Color;
import org.bukkit.GameMode;

/**
 * Created by paul on 05/01/16.
 */
public class CWGameStartListener extends SimpleGameStartListener {

    @CustomEventHandler
    public void onUserRejoin(CWCommand.UserRejoinEvent event) {
        event.setCancelled(false);

        setupUser(event.getUser());

        event.getGameGroup().sendLocale("user.rejoin", event.getUser().getFormattedName());
    }

    protected void setupUser(User user) {
        super.setupUser(user);

        CWTeamStats teamStats = CWTeamStats.getOrCreate(user.getTeam());
        user.teleport(teamStats.getSpawnLocation());

        //Add team variable lookups
        UserVariableHandler upgradeLevels = user.getUserVariables();
        upgradeLevels.addCustomVariableHandler("built", teamStats.getBuildingCountVariablesObject());
        upgradeLevels.addCustomVariableHandler("building_now", teamStats.getBuildingNowCountVariablesObject());
        upgradeLevels.addCustomVariableHandler("buildings_in_inv", teamStats.getBuildingInventoryVariablesObject());

        upgradeLevels.addCustomVariableHandler("buildings", teamStats.getTotalBuildingsVariablesObject());

        user.setGameMode(GameMode.SURVIVAL);
        user.setAllowFlight(false);

        user.giveColoredArmor(user.getTeam().getArmorColor(), true);
        user.setDisplayName(user.getTeam().getChatColor() + user.getName());
        user.setTabListName(user.getTeam().getChatColor() + user.getName());

        user.setScoreboardHandler(new CWScoreboardHandler(user));
        user.updateScoreboard();


        StatsHolder statsHolder = StatsHolder.getOrCreate(user);
        statsHolder.addGame();

        user.showTitle(user.getTeam().getFormattedName(), user.getKit().getFormattedName(), 20, 20, 20);
    }

    @CustomEventHandler
    public void onUserChangeTeam(UserChangeTeamEvent event) {
        Color armorColor = event.getNewTeam() != null ? event.getNewTeam().getArmorColor() : null;
        event.getUser().giveColoredArmor(armorColor, true);
    }
}

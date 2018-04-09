package com.ithinkrok.cw.gamestate;

import com.ithinkrok.cw.command.CWCommand;
import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.GameState;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.game.UserJoinEvent;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;

public class CWTutorialGameListener extends CWInGameListener {


    protected String defaultKit;


    @Override
    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<GameGroup, GameState> event) {
        super.onListenerLoaded(event);

        Config config = event.getConfigOrEmpty();
        defaultKit = config.getString("default_kit", null);
    }


    @Override
    public void checkVictoryOrShowdown(GameGroup gameGroup) {
        //disable victory and showdown
    }


    @Override
    protected void checkShowdownStart(GameGroup gameGroup, int teamsInGame, int nonZombieUsersInGame) {
        //disable showdowns
    }


    @Override
    protected void eliminateTeam(Team team) {
        //do nothing
    }


    @Override
    protected void removeUserFromGame(User died) {
        Team team = died.getTeam();
        Kit kit = died.getKit();

        super.removeUserFromGame(died);

        died.setTeam(team);
        died.setKit(kit);
    }

    @CustomEventHandler
    @Override
    public void onUserJoined(UserJoinEvent event) {
        if(event.getUser().isInGame()) return;

        if(defaultKit != null) {
            event.getUser().setKit(event.getGameGroup().getKit(defaultKit));
        }

        CWCommand.UserRejoinEvent rejoin = new CWCommand.UserRejoinEvent(event.getUser());
        rejoin.setCancelled(true);
        rejoin.getGameGroup().userEvent(rejoin);

        super.onUserJoined(event);
    }
}

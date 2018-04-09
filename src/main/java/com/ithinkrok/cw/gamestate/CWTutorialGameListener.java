package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;

public class CWTutorialGameListener extends CWInGameListener {


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
}

package com.ithinkrok.cw.gamestate;

import com.ithinkrok.minigames.api.GameGroup;
import com.ithinkrok.minigames.api.team.Team;

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
}

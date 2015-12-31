package com.ithinkrok.minigames;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 31/12/15.
 */
public abstract class Team<U extends User, T extends Team, G extends GameGroup> {

    private TeamColor teamColor;
    private List<U> usersInTeam = new ArrayList<>();
    private G gameGroup;

}

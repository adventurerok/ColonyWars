package com.ithinkrok.cw.event;

import com.ithinkrok.cw.Building;
import com.ithinkrok.minigames.api.event.team.TeamEvent;
import com.ithinkrok.minigames.api.team.Team;

/**
 * Created by paul on 14/01/16.
 */
public class BuildingBuiltEvent extends TeamEvent {

    private final Building building;

    public BuildingBuiltEvent(Team team, Building building) {
        super(team);
        this.building = building;
    }

    public Building getBuilding() {
        return building;
    }
}

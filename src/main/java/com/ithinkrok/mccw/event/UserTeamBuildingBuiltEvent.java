package com.ithinkrok.mccw.event;

import com.ithinkrok.mccw.data.Building;
import com.ithinkrok.mccw.data.User;

/**
 * Created by paul on 18/11/15.
 * <p>
 * An event for when a building is built.
 * Called for every player in the team that the building was built in, in their respective class handlers.
 */
public class UserTeamBuildingBuiltEvent extends UserEvent {

    private Building building;

    public UserTeamBuildingBuiltEvent(User user, Building building) {
        super(user);
        this.building = building;
    }

    public Building getBuilding() {
        return building;
    }

}

package com.ithinkrok.minigames.metadata;

import com.ithinkrok.minigames.GameGroup;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.event.game.GameStateChangedEvent;
import com.ithinkrok.minigames.event.game.MapChangedEvent;
import com.ithinkrok.minigames.event.user.game.UserInGameChangeEvent;

/**
 * Created by paul on 04/01/16.
 */
public class MapVote extends UserMetadata {

    private String mapVote;
    private int voteWeight;

    public MapVote(User user, String vote) {
        mapVote = vote;

        voteWeight = 1;

        int next = 2;
        while(user.hasPermission("minigames.map_vote.weight." + next) && next <= 10) {
            voteWeight = next++;
        }

    }

    public int getVoteWeight() {
        return voteWeight;
    }

    public String getMapVote() {
        return mapVote;
    }

    @Override
    public boolean removeOnInGameChange(UserInGameChangeEvent event) {
        return false;
    }

    @Override
    public boolean removeOnGameStateChange(GameStateChangedEvent<? extends GameGroup> event) {
        return false;
    }

    @Override
    public boolean removeOnMapChange(MapChangedEvent<? extends GameGroup> event) {
        return true;
    }
}

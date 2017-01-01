package com.ithinkrok.cw.item;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.api.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.api.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.team.TeamIdentifier;
import com.ithinkrok.minigames.api.util.InventoryUtils;
import com.ithinkrok.util.config.Config;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

/**
 * Created by paul on 15/01/16.
 */
public class TeamCompass implements CustomListener {


    private String compassNameLocale, compassOrientedLocale;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        Config config = event.getConfigOrEmpty();

        compassNameLocale = config.getString("compass_name_locale", "team_compass.name");
        compassOrientedLocale = config.getString("compass_oriented_locale", "team_compass.oriented");
    }

    @CustomEventHandler
    public void onUserInteract(UserInteractEvent event) {
        Location currentLoc = event.getUser().getCompassTarget();

        Collection<TeamIdentifier> teamIdentifiers = event.getGameGroup().getTeamIdentifiers();

        TeamIdentifier nextIdentifier = teamIdentifiers.iterator().next();
        boolean found = false;

        for(TeamIdentifier identifier : teamIdentifiers) {
            if(found) {
                nextIdentifier = identifier;
                break;
            }

            Team team = event.getGameGroup().getTeam(identifier);
            if(team.getUsers().isEmpty()) continue;

            CWTeamStats teamStats = CWTeamStats.getOrCreate(team);
            if(teamStats == null) continue;

            if(teamStats.getBaseLocation().equals(currentLoc)) {
                found = true;
            }
        }

        Team nextTeam = event.getGameGroup().getTeam(nextIdentifier);
        CWTeamStats nextStats = CWTeamStats.getOrCreate(nextTeam);

        event.getUser().setCompassTarget(nextStats.getBaseLocation());

        String compassName = nextIdentifier.getChatColor() + event.getUser().getLanguageLookup().getLocale
                (compassNameLocale);
        String compassOriented = event.getUser().getLanguageLookup().getLocale(compassOrientedLocale, nextIdentifier
                .getFormattedName());

        ItemStack item = event.getItem();
        InventoryUtils.setItemNameAndLore(item, compassName, compassOriented);

        event.setStartCooldownAfterAction(true);
    }
}

package com.ithinkrok.cw.item;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.Team;
import com.ithinkrok.minigames.TeamIdentifier;
import com.ithinkrok.minigames.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.event.user.world.UserInteractEvent;
import com.ithinkrok.minigames.util.InventoryUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

/**
 * Created by paul on 15/01/16.
 */
public class TeamCompass implements Listener {


    private String compassNameLocale, compassOrientedLocale;

    @EventHandler
    public void onListenerLoaded(ListenerLoadedEvent event) {
        ConfigurationSection config;
        if(event.hasConfig()) config = event.getConfig();
        else config = new MemoryConfiguration();

        compassNameLocale = config.getString("compass_name_locale", "team_compass.name");
        compassOrientedLocale = config.getString("compass_oriented_locale", "team_compass.oriented");
    }

    @EventHandler
    public void onUserInteract(UserInteractEvent event) {
        Location currentLoc = event.getUser().getCompassTarget();

        Collection<TeamIdentifier> teamIdentifiers = event.getUserGameGroup().getTeamIdentifiers();

        TeamIdentifier nextIdentifier = teamIdentifiers.iterator().next();
        boolean found = false;

        for(TeamIdentifier identifier : teamIdentifiers) {
            if(found) {
                nextIdentifier = identifier;
                break;
            }

            Team team = event.getUserGameGroup().getTeam(identifier);
            CWTeamStats teamStats = CWTeamStats.getOrCreate(team);
            if(teamStats.getBaseLocation().equals(currentLoc)) {
                found = true;
            }
        }

        Team nextTeam = event.getUserGameGroup().getTeam(nextIdentifier);
        CWTeamStats nextStats = CWTeamStats.getOrCreate(nextTeam);

        event.getUser().setCompassTarget(nextStats.getBaseLocation());

        String compassName = nextIdentifier.getChatColor() + event.getUser().getLanguageLookup().getLocale
                (compassNameLocale);
        String compassOriented = event.getUser().getLanguageLookup().getLocale(compassOrientedLocale, nextIdentifier
                .getFormattedName());

        ItemStack item = event.getItem();
        InventoryUtils.setItemNameAndLore(item, compassName, compassOriented);
    }
}

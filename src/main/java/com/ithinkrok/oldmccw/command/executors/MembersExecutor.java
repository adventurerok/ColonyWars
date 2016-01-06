package com.ithinkrok.oldmccw.command.executors;

import com.ithinkrok.oldmccw.command.WarsCommandExecutor;
import com.ithinkrok.oldmccw.command.WarsCommandSender;
import com.ithinkrok.oldmccw.data.Team;
import com.ithinkrok.oldmccw.data.User;
import com.ithinkrok.oldmccw.data.TeamColor;
import org.bukkit.Location;
import org.bukkit.command.Command;

/**
 * Created by paul on 10/12/15.
 * <p>
 * Handles the /members command
 */
public class MembersExecutor implements WarsCommandExecutor {

    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        Team team;

        if (sender instanceof User) {
            User user = (User) sender;

            if (user.getTeamColor() == null) {
                user.sendLocale("commands.members.no-team");
                return true;
            }

            team = user.getTeam();
        } else {
            try {
                team = sender.getPlugin().getTeam(TeamColor.fromName(args[0]));
                if (team == null) throw new NullPointerException();
            } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                sender.sendLocale("commands.members.specify-team");
                return true;
            }
        }

        sender.sendLocale("commands.members.title");

        for (User other : team.getUsers()) {

            String name = other.getFormattedName();
            String playerClass = other.getPlayerClass() == null ? sender.getPlugin().getLocale("team.none") :
                    other.getPlayerClass().getFormattedName();

            String nearestBase = null;
            if (sender.getPlugin().isInGame()) {
                double smallestDistSquared = 99999999;

                for (TeamColor teamColor : TeamColor.values()) {
                    Location loc = sender.getPlugin().getMapSpawn(teamColor);
                    double distSquared = loc.distanceSquared(other.getLocation());

                    if (distSquared < smallestDistSquared) {
                        smallestDistSquared = distSquared;
                        nearestBase = teamColor.getFormattedName();
                    }
                }

                if (sender.getPlugin().getMapSpawn(null).distanceSquared(other.getLocation()) <
                        smallestDistSquared) {
                    nearestBase = sender.getPlugin().getLocale("commands.members.near-showdown", nearestBase);
                }
            } else {
                nearestBase = "Not in game";
            }

            //Send the player a message directly to avoid the chat prefix
            sender.sendLocaleDirect("commands.members.player-info", name, playerClass, nearestBase);
        }

        return true;
    }
}

package com.ithinkrok.mccw.command.executors;

import com.ithinkrok.mccw.command.WarsCommandExecutor;
import com.ithinkrok.mccw.command.WarsCommandSender;
import org.bukkit.command.Command;

/**
 * Created by paul on 19/12/15.
 */
public class TeamCountExecutor implements WarsCommandExecutor {


    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) return false;

        int teamCount;

        try {
            teamCount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            teamCount = -1;
        }

        if(teamCount < 2 || teamCount > 16) {
            sender.sendLocale("commands.teamcount.invalid");
            return true;
        }

        sender.getPlugin().changeTeamCount(teamCount);
        sender.sendLocale("commands.teamcount.changed", teamCount);

        return true;
    }
}

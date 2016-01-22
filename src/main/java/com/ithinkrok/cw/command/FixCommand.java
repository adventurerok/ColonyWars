package com.ithinkrok.cw.command;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;
import com.ithinkrok.minigames.command.GameCommandExecutor;

/**
 * Created by paul on 22/01/16.
 */
public class FixCommand implements GameCommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command) {
        if(!command.requireUser(sender) || !command.requireOthersPermission(sender, "mccw.fix.others")) return true;
        if(!command.getUser().isInGame()) return true;

        User user = command.getUser();

        if(user.isInsideVehicle()) return true;

        if(!user.startCoolDown("fix", 1, "command.fix.cooldown")) return true;

        boolean success = user.unstuck(10);

        if(!success) {
            user.teleport(CWTeamStats.getOrCreate(user.getTeam()).getSpawnLocation());
            user.sendLocale("command.fix.failed");
        } else user.sendLocale("command.fix.success");

        return true;
    }
}

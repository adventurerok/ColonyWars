package com.ithinkrok.cw.command;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.command.Command;
import com.ithinkrok.minigames.base.command.CommandSender;
import com.ithinkrok.minigames.base.event.CommandEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import org.bukkit.event.Listener;

/**
 * Created by paul on 22/01/16.
 */
public class FixCommand implements Listener {


    @MinigamesEventHandler
    public void onCommand(CommandEvent event) {
        CommandSender sender = event.getCommandSender();
        Command command = event.getCommand();

        if(!command.requireUser(sender) || !command.requireOthersPermission(sender, "mccw.fix.others")) return;
        if(!command.getUser().isInGame()) return;

        User user = command.getUser();

        if(user.isInsideVehicle()) return;

        if(!user.startCoolDown("fix", 1, "command.fix.cooldown")) return;

        boolean success = user.unstuck(10);

        if(!success) {
            user.teleport(CWTeamStats.getOrCreate(user.getTeam()).getSpawnLocation());
            user.sendLocale("command.fix.failed");
        } else user.sendLocale("command.fix.success");

        return;
    }
}

package com.ithinkrok.cw.command;

import com.ithinkrok.cw.metadata.CWTeamStats;
import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.command.MinigamesCommandSender;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 22/01/16.
 */
public class FixCommand implements CustomListener {


    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        MinigamesCommandSender sender = event.getCommandSender();
        MinigamesCommand command = event.getCommand();

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

    }
}

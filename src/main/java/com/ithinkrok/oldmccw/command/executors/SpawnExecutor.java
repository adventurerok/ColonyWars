package com.ithinkrok.oldmccw.command.executors;

import com.ithinkrok.oldmccw.command.CommandUtils;
import com.ithinkrok.oldmccw.command.WarsCommandExecutor;
import com.ithinkrok.oldmccw.command.WarsCommandSender;
import com.ithinkrok.oldmccw.data.User;
import org.bukkit.command.Command;

/**
 * Created by paul on 11/12/15.
 *
 * Handles the /spawn command
 */
public class SpawnExecutor implements WarsCommandExecutor {

    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        if(!CommandUtils.checkUser(sender)) return true;

        User user = (User) sender;

        if (user.isInGame()) {
            user.sendLocale("commands.spawn.not-in-game");
            return true;
        }

        user.teleport(user.getPlugin().getMapSpawn(null));
        return true;
    }
}

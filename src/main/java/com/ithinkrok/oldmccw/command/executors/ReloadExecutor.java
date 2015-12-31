package com.ithinkrok.oldmccw.command.executors;

import com.ithinkrok.oldmccw.command.WarsCommandExecutor;
import com.ithinkrok.oldmccw.command.WarsCommandSender;
import org.bukkit.command.Command;

/**
 * Created by paul on 21/12/15.
 */
public class ReloadExecutor implements WarsCommandExecutor {


    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        sender.sendLocale("commands.reload.done");

        sender.getPlugin().reload();
        return true;
    }
}

package com.ithinkrok.oldmccw.command.executors;

import com.ithinkrok.oldmccw.command.CommandUtils;
import com.ithinkrok.oldmccw.command.WarsCommandExecutor;
import com.ithinkrok.oldmccw.command.WarsCommandSender;
import com.ithinkrok.oldmccw.data.User;
import org.bukkit.command.Command;

/**
 * Created by paul on 15/12/15.
 */
public class ToggleMoneyMessageExecutor implements WarsCommandExecutor{

    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        if(!CommandUtils.checkUser(sender)) return true;

        User user = (User) sender;
        user.toggleMoneyMessagesEnabled();

        return true;
    }
}

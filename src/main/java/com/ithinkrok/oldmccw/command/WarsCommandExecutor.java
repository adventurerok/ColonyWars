package com.ithinkrok.oldmccw.command;

import org.bukkit.command.Command;

/**
 * Created by paul on 10/12/15.
 *
 * Executes commands with WarsCommandSenders
 */
public interface WarsCommandExecutor {

    boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args);
}

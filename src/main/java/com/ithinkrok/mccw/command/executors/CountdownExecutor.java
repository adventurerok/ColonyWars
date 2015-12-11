package com.ithinkrok.mccw.command.executors;

import com.ithinkrok.mccw.command.WarsCommandExecutor;
import com.ithinkrok.mccw.command.WarsCommandSender;
import com.ithinkrok.mccw.handler.CountdownHandler;
import org.bukkit.command.Command;

/**
 * Created by paul on 11/12/15.
 *
 * Handles the /countdown command
 */
public class CountdownExecutor implements WarsCommandExecutor {

    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) return false;

        CountdownHandler handler = sender.getPlugin().getCountdownHandler();
        if (!handler.isCountingDown()) {
            sender.sendLocale("commands.countdown.none");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[0]);

            int newTime = Math.max(handler.getCountDownTime() + amount, 1);
            handler.setCountDownTime(newTime);

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

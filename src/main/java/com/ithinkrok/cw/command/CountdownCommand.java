package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.Countdown;
import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;
import com.ithinkrok.minigames.command.GameCommandExecutor;

/**
 * Created by paul on 19/01/16.
 */
public class CountdownCommand implements GameCommandExecutor{


    @Override
    public boolean onCommand(CommandSender sender, Command command) {
        if(!command.requireGameGroup(sender)) return true;
        if(!command.requireArgumentCount(sender, 1)) return false;

        if("start".equals(command.getStringArg(0, null))) {
            if(!command.requireArgumentCount(sender, 4)) return false;
            String name = command.getStringArg(1, null);
            int seconds = command.getIntArg(2, 30);
            String localeStub = command.getStringArg(3, null);

            command.getGameGroup().startCountdown(name, localeStub, seconds);
            sender.sendLocale("command.countdown.started", name, seconds);
            return true;
        }

        if(!command.getGameGroup().hasActiveCountdown()) {
            sender.sendLocale("commands.countdown.none");
            return true;
        }

        Countdown countdown = command.getGameGroup().getCountdown();

        int amount = command.getIntArg(1, 1);

        switch(command.getStringArg(0, null)) {
            case "add":
                if(!command.requireArgumentCount(sender, 2)) return false;
                countdown.setSecondsRemaining(countdown.getSecondsRemaining() + amount);
                sender.sendLocale("command.countdown.added", amount);
                break;
            case "set":
                if(!command.requireArgumentCount(sender, 2)) return false;
                countdown.setSecondsRemaining(amount);
                sender.sendLocale("command.countdown.set", amount);
                break;
            case "finish":
                countdown.setSecondsRemaining(1);
                sender.sendLocale("command.countdown.finished");
                break;
            case "cancel":
            case "stop":
                command.getGameGroup().stopCountdown();
                sender.sendLocale("command.countdown.stopped");
                break;
            default:
                return false;
        }

        return true;
    }
}

package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;
import com.ithinkrok.minigames.command.GameCommandExecutor;
import com.ithinkrok.minigames.metadata.Money;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by paul on 13/01/16.
 */
public class CWCommand implements GameCommandExecutor {

    private Map<String, GameCommandExecutor> subExecutors = new HashMap<>();


    public CWCommand() {
        subExecutors.put("money", this::moneyCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command) {
        if(!command.requireArgumentCount(sender, 1)) return false;

        Command subCommand = command.subCommand();

        if(!subExecutors.containsKey(subCommand.getCommand())) {
            sender.sendLocale("command.cw.unknown", subCommand.getCommand());
            return true;
        }

        if(!Command.requirePermission(sender, "command.cw." + subCommand.getCommand())) return true;

        return subExecutors.get(subCommand.getCommand()).onCommand(sender, subCommand);
    }

    private boolean moneyCommand(CommandSender sender, Command command) {
        if(!command.requireUser(sender)) return true;

        int amount = command.getIntArg(0, 10000);

        Money userMoney = Money.getOrCreate(command.getUser());
        userMoney.addMoney(amount, true);

        if(command.getUser().getTeam() == null) return true;
        int teamAmount = command.getIntArg(1, amount);

        Money teamMoney = Money.getOrCreate(command.getUser().getTeam());
        teamMoney.addMoney(teamAmount, true);

        return true;
    }
}

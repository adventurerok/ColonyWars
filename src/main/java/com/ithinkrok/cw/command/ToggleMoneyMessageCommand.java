package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;
import com.ithinkrok.minigames.command.GameCommandExecutor;
import com.ithinkrok.minigames.metadata.Money;
import com.ithinkrok.minigames.metadata.UserMoney;

/**
 * Created by paul on 17/01/16.
 */
public class ToggleMoneyMessageCommand implements GameCommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command) {
        if(!command.requireUser(sender)) return true;

        if(sender != command.getUser() && !Command.requirePermission(sender, "mccw.tmm.others")) return true;

        int changeTo = command.getIntArg(0, -1);
        if(changeTo > 2 || changeTo < -1) changeTo = -1;

        UserMoney money = (UserMoney) Money.getOrCreate(command.getUser());
        if(changeTo == -1) changeTo = (money.getMessageLevel() + 1) % 3;

        money.setMessageLevel(changeTo);
        sender.sendLocale("command.tmm.change." + changeTo);

        return true;
    }
}

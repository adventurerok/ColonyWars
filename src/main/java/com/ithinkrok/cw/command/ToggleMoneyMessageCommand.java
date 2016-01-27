package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.base.command.Command;
import com.ithinkrok.minigames.base.command.CommandSender;
import com.ithinkrok.minigames.base.event.CommandEvent;
import com.ithinkrok.minigames.base.event.MinigamesEventHandler;
import com.ithinkrok.minigames.base.metadata.Money;
import com.ithinkrok.minigames.base.metadata.UserMoney;
import org.bukkit.event.Listener;

/**
 * Created by paul on 17/01/16.
 */
public class ToggleMoneyMessageCommand implements Listener {

    @MinigamesEventHandler
    public void onCommand(CommandEvent event) {
        CommandSender sender = event.getCommandSender();
        Command command = event.getCommand();

        if(!command.requireUser(sender)) return;

        if(!command.requireOthersPermission(sender, "mccw.tmm.others")) return;

        int changeTo = command.getIntArg(0, -1);
        if(changeTo > 2 || changeTo < -1) changeTo = -1;

        UserMoney money = (UserMoney) Money.getOrCreate(command.getUser());
        if(changeTo == -1) changeTo = (money.getMessageLevel() + 1) % 3;

        money.setMessageLevel(changeTo);
        sender.sendLocale("command.tmm.change." + changeTo);
    }
}

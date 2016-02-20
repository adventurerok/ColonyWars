package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.command.MinigamesCommandSender;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.minigames.util.metadata.UserMoney;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;

/**
 * Created by paul on 17/01/16.
 */
public class ToggleMoneyMessageCommand implements CustomListener {

    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        MinigamesCommandSender sender = event.getCommandSender();
        MinigamesCommand command = event.getCommand();

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

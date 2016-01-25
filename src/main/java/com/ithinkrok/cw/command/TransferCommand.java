package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;
import com.ithinkrok.minigames.command.GameCommandExecutor;
import com.ithinkrok.minigames.event.CommandEvent;
import com.ithinkrok.minigames.event.MinigamesEventHandler;
import com.ithinkrok.minigames.lang.LanguageLookup;
import com.ithinkrok.minigames.metadata.Metadata;
import com.ithinkrok.minigames.metadata.MetadataHolder;
import com.ithinkrok.minigames.metadata.Money;
import com.ithinkrok.minigames.team.Team;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by paul on 22/01/16.
 */
public class TransferCommand implements Listener {


    @MinigamesEventHandler
    public void onCommand(CommandEvent event) {
        CommandSender sender = event.getCommandSender();
        Command command = event.getCommand();

        if (!command.requireUser(sender)) return;
        if (!command.requireOthersPermission(sender, "mccw.transfer.others")) return;
        if (!command.requireArgumentCount(sender, 1)) {
            event.setValidCommand(false);
            return;
        }

        if (!command.getUser().isInGame()) {
            sender.sendLocale("command.transfer.not_in_game");
            return;
        }

        int amount = command.getIntArg(0, -1);
        Money userMoney = Money.getOrCreate(command.getUser());

        if (amount < 1) {
            if ("all".equalsIgnoreCase(command.getStringArg(0, null))) {
                amount = userMoney.getMoney();
            }
        }

        if (!userMoney.hasMoney(amount)) {
            sender.sendLocale("command.transfer.cannot_afford");
            return;
        }

        Set<Money> transferTo = new HashSet<>();

        if (!command.hasArg(1)) {
            transferTo.add(Money.getOrCreate(command.getUser().getTeam()));
        } else {
            for (int index = 1; command.hasArg(index); ++index) {
                String targetName = command.getStringArg(index, null);
                switch (targetName) {
                    case "team":
                        transferTo.add(Money.getOrCreate(command.getUser().getTeam()));
                        break;
                    default:
                        for (User user : command.getUser().getTeam().getUsers()) {
                            if (!"all".equals(targetName) && !user.getName().equals(targetName)) continue;
                            if (Objects.equals(user, command.getUser())) continue;
                            transferTo.add(Money.getOrCreate(user));
                        }
                }
            }
        }

        if (transferTo.isEmpty()) {
            sender.sendLocale("command.transfer.no_target");
            return;
        }

        //Change amount to the amount to pay for each transferTo member
        amount /= transferTo.size();

        if (amount < 1) {
            sender.sendLocale("command.transfer.invalid_amount");
            return;
        }

        userMoney.subtractMoney(amount * transferTo.size(), true);

        StringBuilder receivers = new StringBuilder();
        boolean addComma = false;
        LanguageLookup lookup = command.getUser().getLanguageLookup();

        for (Money money : transferTo) {
            if (addComma) receivers.append(lookup.getLocale("command.transfer.comma"));
            else addComma = true;

            MetadataHolder<? extends Metadata> holder = money.getOwner();
            if (holder instanceof User) {
                receivers.append(((User) holder).getFormattedName());
            } else if (holder instanceof Team) {
                receivers.append(lookup.getLocale("command.transfer.team"));
            }

            money.addMoney(amount, true);
        }

        String locale = transferTo.size() == 1 ? "command.transfer.single" : "command.transfer.multiple";

        command.getUser().getTeam().sendLocale(locale, command.getUser().getFormattedName(), amount, receivers);
    }
}

package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.command.MinigamesCommand;
import com.ithinkrok.minigames.base.command.MinigamesCommandSender;
import com.ithinkrok.minigames.base.event.ListenerLoadedEvent;
import com.ithinkrok.minigames.base.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.base.metadata.Metadata;
import com.ithinkrok.minigames.base.metadata.MetadataHolder;
import com.ithinkrok.minigames.base.metadata.Money;
import com.ithinkrok.minigames.base.team.Team;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.lang.LanguageLookup;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by paul on 22/01/16.
 */
public class TransferCommand implements CustomListener {


    private String defaultTarget;

    @CustomEventHandler
    public void onListenerLoaded(ListenerLoadedEvent<?, ?> event) {
        defaultTarget = event.getConfigOrEmpty().getString("default_target");
    }

    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        MinigamesCommandSender sender = event.getCommandSender();
        MinigamesCommand command = event.getCommand();

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
            addTransferTarget(command, transferTo, defaultTarget);
        } else {
            for (int index = 1; command.hasArg(index); ++index) {
                String targetName = command.getStringArg(index, null);
                addTransferTarget(command, transferTo, targetName);
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

    private void addTransferTarget(MinigamesCommand command, Set<Money> transferTo, String targetName) {
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

package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.base.User;
import com.ithinkrok.minigames.base.command.MinigamesCommand;
import com.ithinkrok.minigames.base.command.CommandSender;
import com.ithinkrok.minigames.base.event.CommandEvent;
import com.ithinkrok.minigames.base.team.TeamIdentifier;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by paul on 22/01/16.
 */
public class TeamChatCommand implements CustomListener {

    @CustomEventHandler
    public void onCommand(CommandEvent event) {
        CommandSender sender = event.getCommandSender();
        MinigamesCommand command = event.getCommand();

        if (!command.requireUser(sender)) return;
        if (!command.requireOthersPermission(sender, "mccw.teamchat.others")) return;
        if (!command.requireArgumentCount(sender, 1)) {
            event.setValidCommand(false);
            return;
        }

        StringBuilder message = new StringBuilder();
        for (Object s : command.getArgs()) {
            if (message.length() != 0) message.append(' ');
            message.append(s);
        }

        TeamIdentifier identifier = command.getTeamIdentifier();

        Set<Player> receivers = new HashSet<>();

        for (User other : command.getGameGroup().getUsers()) {
            if (!Objects.equals(identifier, other.getTeamIdentifier())) continue;
            if (!other.isPlayer()) continue;

            receivers.add(other.getPlayer());
        }

        String teamColorCode =
                identifier != null ? identifier.getChatColor().toString() : ChatColor.LIGHT_PURPLE.toString();

        String chatMessage =
                ChatColor.GRAY + "[" + teamColorCode + "Team" + ChatColor.GRAY + "] " + ChatColor.WHITE + message;

        AsyncPlayerChatEvent chatEvent =
                new AsyncPlayerChatEvent(false, command.getUser().getPlayer(), chatMessage, receivers);

        Bukkit.getServer().getPluginManager().callEvent(chatEvent);

        if (!chatEvent.isCancelled()) {
            String formatted =
                    String.format(chatEvent.getFormat(), command.getUser().getFormattedName(), chatEvent.getMessage());
            for (Player player : chatEvent.getRecipients()) {
                player.sendMessage(formatted);
            }
        }
    }
}

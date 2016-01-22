package com.ithinkrok.cw.command;

import com.ithinkrok.minigames.User;
import com.ithinkrok.minigames.command.Command;
import com.ithinkrok.minigames.command.CommandSender;
import com.ithinkrok.minigames.command.GameCommandExecutor;
import com.ithinkrok.minigames.team.TeamIdentifier;
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
public class TeamChatCommand implements GameCommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command) {
        if (!command.requireUser(sender)) return true;
        if (!command.requireOthersPermission(sender, "mccw.teamchat.others")) return true;
        if (!command.requireArgumentCount(sender, 1)) return false;

        StringBuilder message = new StringBuilder();
        for (Object s : command.getDefaultArgs()) {
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

        AsyncPlayerChatEvent event =
                new AsyncPlayerChatEvent(false, command.getUser().getPlayer(), chatMessage, receivers);

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            String formatted =
                    String.format(event.getFormat(), command.getUser().getFormattedName(), event.getMessage());
            for (Player player : event.getRecipients()) {
                player.sendMessage(formatted);
            }
        }

        return true;
    }
}

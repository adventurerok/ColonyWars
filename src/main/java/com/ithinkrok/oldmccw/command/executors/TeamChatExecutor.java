package com.ithinkrok.oldmccw.command.executors;

import com.ithinkrok.oldmccw.command.CommandUtils;
import com.ithinkrok.oldmccw.command.WarsCommandExecutor;
import com.ithinkrok.oldmccw.command.WarsCommandSender;
import com.ithinkrok.oldmccw.data.User;
import com.ithinkrok.minigames.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by paul on 11/12/15.
 *
 * Handles the /teamchat command
 */
public class TeamChatExecutor implements WarsCommandExecutor {

    @Override
    public boolean onCommand(WarsCommandSender sender, Command command, String label, String[] args) {
        if(!CommandUtils.checkUser(sender)) return true;

        if (args.length == 0) return false;

        StringBuilder message = new StringBuilder();
        for (String s : args) {
            if (message.length() != 0) message.append(' ');
            message.append(s);
        }

        User user = (User) sender;
        if(!user.isPlayer()) return true;

        TeamColor teamColor = user.getTeamColor();

        Set<Player> receivers = new HashSet<>();

        for (User other : user.getPlugin().getUsers()) {
            if (other.getTeamColor() != teamColor) continue;
            if(!other.isPlayer()) continue;

            receivers.add(other.getPlayer());
        }

        String teamColorCode =
                teamColor != null ? teamColor.getChatColor().toString() : ChatColor.LIGHT_PURPLE.toString();

        String chatMessage =
                ChatColor.GRAY + "[" + teamColorCode + "Team" + ChatColor.GRAY + "] " + ChatColor.WHITE + message;

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, user.getPlayer(), chatMessage, receivers);

        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            String formatted = String.format(event.getFormat(), user.getFormattedName(), event.getMessage());
            for (Player player : event.getRecipients()) {
                player.sendMessage(formatted);
            }
        }

        return true;
    }
}

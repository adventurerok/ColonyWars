package com.ithinkrok.mccw.listener;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.data.Team;
import com.ithinkrok.mccw.data.User;
import com.ithinkrok.mccw.enumeration.GameState;
import com.ithinkrok.mccw.enumeration.PlayerClass;
import com.ithinkrok.mccw.enumeration.TeamColor;
import com.ithinkrok.mccw.util.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by paul on 13/11/15.
 * <p>
 * Handles commands for Colony Wars
 */
public class CommandListener implements CommandExecutor {

    private WarsPlugin plugin;

    public CommandListener(WarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to execute Colony Wars commands");
            return true;
        }

        Player player = (Player) sender;
        User user = plugin.getUser(player);

        switch (command.getName().toLowerCase()) {
            case "transfer":
                return onTransferCommand(user, args);
            case "test":
                return args.length >= 1 && onTestCommand(user, args);
            case "gamestate":
                return onGameStateCommand(user, args);
            case "members":
                return onMembersCommand(user);
            case "spawn":
                return onSpawnCommand(user);
            default:
                return false;
        }

    }

    private boolean onSpawnCommand(User user) {
        if(user.isInGame()) {
            user.messageLocale("no-command-in-game");
            return true;
        }

        user.teleport(plugin.getMapSpawn(null));
        return true;
    }

    private boolean onMembersCommand(User user) {
        if(user.getTeamColor() == null){
            user.messageLocale("not-in-team");
            return true;
        }

        user.messageLocale("team-members");

        Team team = user.getTeam();

        for(Player player : team.getPlayers()){
            User other = plugin.getUser(player);

            String name = other.getFormattedName();
            String playerClass = other.getPlayerClass() == null ? "None" : other.getPlayerClass().getName();

            String nearestBase = null;
            if(plugin.isInGame()){
                double smallestDistSquared = 99999999;

                for(TeamColor teamColor : TeamColor.values()){
                    Location loc = plugin.getMapSpawn(teamColor);
                    double distSquared = loc.distanceSquared(player.getLocation());

                    if(distSquared < smallestDistSquared){
                        smallestDistSquared = distSquared;
                        nearestBase = teamColor.name;
                    }
                }

                if(plugin.getMapSpawn(null).distanceSquared(player.getLocation()) < smallestDistSquared){
                    nearestBase = nearestBase + " (Near showdown arena)";
                }
            } else {
                nearestBase = "Not in game";
            }

            //Send the player a message directly to avoid the chat prefix
            user.getPlayer().sendMessage(plugin.getLocale("player-info", name, playerClass, nearestBase));
        }

        return true;
    }

    private boolean onTransferCommand(User user, String[] args) {
        if (args.length < 1) return false;

        try {
            int amount = Integer.parseInt(args[0]);

            if (!user.subtractPlayerCash(amount)) {
                user.message(ChatColor.RED + "You do not have that amount of money");
                return true;
            }

            Team team = user.getTeam();
            team.addTeamCash(amount);

            team.message(user.getFormattedName() + ChatColor.DARK_AQUA + " transferred " +
                    ChatColor.GREEN + "$" + amount +
                    ChatColor.YELLOW + " to your team's account!");
            team.message("Your Team's new Balance is: " + ChatColor.GREEN + "$" + team.getTeamCash() +
                    ChatColor.YELLOW + "!");

            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean onGameStateCommand(User user, String[] args){
        try {
            GameState gameState = GameState.valueOf(args[0].toUpperCase());
            plugin.changeGameState(gameState);
            user.message("Changed gamestate to: " + gameState);
            return true;
        } catch (Exception e) {
            user.message("Invalid gamestate!");
            return false;
        }
    }

    private boolean onTestCommand(User user, String[] args) {

        switch (args[0]) {
            case "team":
                if (args.length < 2) return false;

                TeamColor teamColor = TeamColor.valueOf(args[1].toUpperCase());
                user.setTeamColor(teamColor);

                user.message("You were changed to team " + teamColor);

                break;
            case "class":
                if (args.length < 2) return false;

                PlayerClass playerClass = PlayerClass.valueOf(args[1].toUpperCase());
                user.setPlayerClass(playerClass);

                user.message("You were changed to class " + playerClass);

                break;
            case "money":

                user.addPlayerCash(10000);
                user.getTeam().addTeamCash(10000);

                user.message("10000 added to both you and your team's balance");
                break;
            case "build":
                if (args.length < 2) return false;

                user.getPlayerInventory()
                        .addItem(InventoryUtils.createItemWithNameAndLore(Material.LAPIS_ORE, 16, 0, args[1]));

                user.message("Added 16 " + args[1] + " build blocks to your inventory");
                break;
            case "base_location":
                Team team = user.getTeam();
                if (team == null) {
                    user.message("Your team is null");
                    break;
                }

                user.message("Base location: " + team.getBaseLocation());
                break;

        }

        return true;
    }
}

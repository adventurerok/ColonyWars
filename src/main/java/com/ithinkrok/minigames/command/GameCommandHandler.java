package com.ithinkrok.minigames.command;

import com.ithinkrok.minigames.Game;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 12/01/16.
 */
public class GameCommandHandler implements CommandExecutor {

    private Game game;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        List<String> correctedArgs = new ArrayList<>();

        StringBuilder currentArg = new StringBuilder();

        boolean inQuote = false;

        for (String arg : args) {
            currentArg.append(arg.replace("\"", ""));

            int quoteCount = StringUtils.countMatches(arg, "\"");
            if (((quoteCount & 1) == 1)) inQuote = !inQuote;

            if (!inQuote) {
                correctedArgs.add(currentArg.toString());
                currentArg = new StringBuilder();
            }
        }

        Map<String, Object> arguments = new HashMap<>();

        String key = null;
        StringBuilder value = new StringBuilder();

        for(String arg: correctedArgs) {
            if(arg.startsWith("-") && arg.length() > 1) {
                key = arg.substring(1);

                if(value.length() > 0) arguments.put(key, parse(value.toString()));
                value = new StringBuilder();
            } else {
                if(value.length() > 0) value.append(' ');
                value.append(arg);
            }
        }

        if(value.length() > 0) arguments.put(key, value.toString());



        return false;
    }

    private Object parse(String s) {
        try{
            return Integer.parseInt(s);
        } catch(NumberFormatException ignored){}

        try {
            return Double.parseDouble(s);
        } catch(NumberFormatException ignored) {}

        switch(s.toLowerCase()) {
            case "true":
            case "yes":
                return true;
            case "false":
            case "no":
                return false;
        }

        return s;
    }
}

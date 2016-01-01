package com.ithinkrok.oldmccw.util.item;

import com.ithinkrok.oldmccw.WarsPlugin;
import com.ithinkrok.minigames.util.io.ResourceHandler;

import java.io.*;

/**
 * Created by paul on 16/11/15.
 *
 * Handles the loading of the handbook item
 */
public class Handbook {

    public static String loadHandbookMeta(WarsPlugin plugin){
        File bookFile = ResourceHandler.getResource(plugin, "handbook.json");

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(bookFile)))){
            StringBuilder json = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null){
                json.append(line);
            }

            return json.toString();
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load Handbook item");
            e.printStackTrace();
            return null;
        }
    }
}

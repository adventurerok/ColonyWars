package com.ithinkrok.mccw.util;

import com.ithinkrok.mccw.WarsPlugin;

import java.io.*;

/**
 * Created by paul on 16/11/15.
 *
 * Handles the loading of the handbook item
 */
public class Handbook {

    public static String loadHandbookMeta(WarsPlugin plugin){
        File bookFile = new File(plugin.getDataFolder(), "handbook.json");
        if(!bookFile.exists()){
            plugin.saveResource("handbook.json", false);
        }

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

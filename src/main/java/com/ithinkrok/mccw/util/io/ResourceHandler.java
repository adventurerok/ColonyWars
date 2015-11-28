package com.ithinkrok.mccw.util.io;

import com.ithinkrok.mccw.WarsPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by paul on 20/11/15.
 *
 * Handles plugin resource files
 */
public class ResourceHandler {

    public static File getResource(WarsPlugin plugin, String name){
        File file = new File(plugin.getDataFolder(), name);
        if(!file.exists()){
            plugin.saveResource(name, false);
        }
        return file;
    }

    public static Properties getPropertiesResource(WarsPlugin plugin, String name){
        File file = getResource(plugin, name);

        Properties properties = new Properties();

        try(FileInputStream in = new FileInputStream(file)){
            properties.load(in);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load properties file: " + name);
            e.printStackTrace();
        }

        return properties;
    }
}

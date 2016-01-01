package com.ithinkrok.oldmccw.util.io;

import com.ithinkrok.minigames.util.ResourceHandler;
import com.ithinkrok.oldmccw.WarsPlugin;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by paul on 28/11/15.
 * <p>
 * Handles a map's configuration
 */
public class MapConfig implements ConfigurationSection {

    private YamlConfiguration mapConfig;
    private WarsPlugin plugin;

    public MapConfig(WarsPlugin plugin, String mapName) {
        this.plugin = plugin;
        mapConfig = ResourceHandler.getConfigResource(plugin, mapName + ".yml");
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        Set<String> keys = mapConfig.getKeys(deep);

        keys.addAll(plugin.getBaseConfig().getKeys(deep));

        return keys;
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        Map<String, Object> values = plugin.getBaseConfig().getValues(deep);

        values.putAll(mapConfig.getValues(deep));

        return values;
    }

    @Override
    public boolean contains(String path) {
        return mapConfig.contains(path) || plugin.getBaseConfig().contains(path);
    }

    @Override
    public boolean isSet(String path) {
        if (mapConfig.contains(path)) return mapConfig.isSet(path);
        else return plugin.getBaseConfig().isSet(path);
    }

    @Override
    public String getCurrentPath() {
        return mapConfig.getCurrentPath();
    }

    @Override
    public String getName() {
        return mapConfig.getName();
    }

    @Override
    public Configuration getRoot() {
        return null;
    }

    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    @Override
    public Object get(String path) {
        return mapConfig.get(path, null);
    }

    @Override
    public Object get(String path, Object def) {
        return mapConfig.get(path, plugin.getBaseConfig().get(path, def));
    }

    @Override
    public void set(String path, Object value) {
        throw new UnsupportedOperationException("Only supports reading from config");
    }

    @Override
    public ConfigurationSection createSection(String path) {
        throw new UnsupportedOperationException("Only supports reading from config");
    }

    @Override
    public ConfigurationSection createSection(String path, Map<?, ?> map) {
        throw new UnsupportedOperationException("Only supports reading from config");
    }

    @Override
    public String getString(String path) {
        return getString(path, null);
    }

    @Override
    public String getString(String path, String def) {
        return mapConfig.getString(path, plugin.getBaseConfig().getString(path, def));
    }

    @Override
    public boolean isString(String path) {
        if (mapConfig.contains(path)) return mapConfig.isString(path);
        else return plugin.getBaseConfig().isString(path);
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(String path, int def) {
        return mapConfig.getInt(path, plugin.getBaseConfig().getInt(path, def));
    }

    @Override
    public boolean isInt(String path) {
        if (mapConfig.contains(path)) return mapConfig.isInt(path);
        else return plugin.getBaseConfig().isInt(path);
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return mapConfig.getBoolean(path, plugin.getBaseConfig().getBoolean(path, def));
    }

    @Override
    public boolean isBoolean(String path) {
        if (mapConfig.contains(path)) return mapConfig.isBoolean(path);
        else return plugin.getBaseConfig().isBoolean(path);
    }

    @Override
    public double getDouble(String path) {
        return getDouble(path, 0);
    }

    @Override
    public double getDouble(String path, double def) {
        return mapConfig.getDouble(path, plugin.getBaseConfig().getDouble(path, def));
    }

    @Override
    public boolean isDouble(String path) {
        if (mapConfig.contains(path)) return mapConfig.isDouble(path);
        else return plugin.getBaseConfig().isDouble(path);
    }

    @Override
    public long getLong(String path) {
        return getLong(path, 0);
    }

    @Override
    public long getLong(String path, long def) {
        return mapConfig.getLong(path, plugin.getBaseConfig().getLong(path, def));
    }

    @Override
    public boolean isLong(String path) {
        if (mapConfig.contains(path)) return mapConfig.isLong(path);
        else return plugin.getBaseConfig().isLong(path);
    }

    @Override
    public List<?> getList(String path) {
        return getList(path, null);
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        return mapConfig.getList(path, plugin.getBaseConfig().getList(path, def));
    }

    @Override
    public boolean isList(String path) {
        if (mapConfig.contains(path)) return mapConfig.isList(path);
        else return plugin.getBaseConfig().isList(path);
    }

    @Override
    public List<String> getStringList(String path) {
        if(mapConfig.contains(path)) return mapConfig.getStringList(path);
        else return plugin.getBaseConfig().getStringList(path);
    }

    @Override
    public List<Integer> getIntegerList(String path) {
        if(mapConfig.contains(path)) return mapConfig.getIntegerList(path);
        else return plugin.getBaseConfig().getIntegerList(path);
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        if(mapConfig.contains(path)) return mapConfig.getBooleanList(path);
        else return plugin.getBaseConfig().getBooleanList(path);
    }

    @Override
    public List<Double> getDoubleList(String path) {
        if(mapConfig.contains(path)) return mapConfig.getDoubleList(path);
        else return plugin.getBaseConfig().getDoubleList(path);
    }

    @Override
    public List<Float> getFloatList(String path) {
        if(mapConfig.contains(path)) return mapConfig.getFloatList(path);
        else return plugin.getBaseConfig().getFloatList(path);
    }

    @Override
    public List<Long> getLongList(String path) {
        if(mapConfig.contains(path)) return mapConfig.getLongList(path);
        else return plugin.getBaseConfig().getLongList(path);
    }

    @Override
    public List<Byte> getByteList(String path) {
        if(mapConfig.contains(path)) return mapConfig.getByteList(path);
        else return plugin.getBaseConfig().getByteList(path);
    }

    @Override
    public List<Character> getCharacterList(String path) {
        if(mapConfig.contains(path)) return mapConfig.getCharacterList(path);
        else return plugin.getBaseConfig().getCharacterList(path);
    }

    @Override
    public List<Short> getShortList(String path) {
        if(mapConfig.contains(path)) return mapConfig.getShortList(path);
        else return plugin.getBaseConfig().getShortList(path);
    }

    @Override
    public List<Map<?, ?>> getMapList(String path) {
        if(mapConfig.contains(path)) return mapConfig.getMapList(path);
        else return plugin.getBaseConfig().getMapList(path);
    }

    @Override
    public Vector getVector(String path) {
        return getVector(path, null);
    }

    @Override
    public Vector getVector(String path, Vector def) {
        return mapConfig.getVector(path, plugin.getBaseConfig().getVector(path, def));
    }

    @Override
    public boolean isVector(String path) {
        if (mapConfig.contains(path)) return mapConfig.isVector(path);
        else return plugin.getBaseConfig().isVector(path);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String path) {
        return getOfflinePlayer(path, null);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String path, OfflinePlayer def) {
        return mapConfig.getOfflinePlayer(path, plugin.getBaseConfig().getOfflinePlayer(path, def));
    }

    @Override
    public boolean isOfflinePlayer(String path) {
        if (mapConfig.contains(path)) return mapConfig.isOfflinePlayer(path);
        else return plugin.getBaseConfig().isOfflinePlayer(path);
    }

    @Override
    public ItemStack getItemStack(String path) {
        return getItemStack(path, null);
    }

    @Override
    public ItemStack getItemStack(String path, ItemStack def) {
        return mapConfig.getItemStack(path, plugin.getBaseConfig().getItemStack(path, def));
    }

    @Override
    public boolean isItemStack(String path) {
        if (mapConfig.contains(path)) return mapConfig.isItemStack(path);
        else return plugin.getBaseConfig().isItemStack(path);
    }

    @Override
    public Color getColor(String path) {
        return getColor(path, null);
    }

    @Override
    public Color getColor(String path, Color def) {
        return mapConfig.getColor(path, plugin.getBaseConfig().getColor(path, def));
    }

    @Override
    public boolean isColor(String path) {
        if (mapConfig.contains(path)) return mapConfig.isColor(path);
        else return plugin.getBaseConfig().isColor(path);
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        if(mapConfig.contains(path)) return mapConfig.getConfigurationSection(path);
        else return plugin.getBaseConfig().getConfigurationSection(path);
    }

    @Override
    public boolean isConfigurationSection(String path) {
        if (mapConfig.contains(path)) return mapConfig.isConfigurationSection(path);
        else return plugin.getBaseConfig().isConfigurationSection(path);
    }

    @Override
    public ConfigurationSection getDefaultSection() {
        return mapConfig.getDefaultSection();
    }

    @Override
    public void addDefault(String path, Object value) {
        throw new UnsupportedOperationException("Only supports reading from config");
    }
}

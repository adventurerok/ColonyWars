package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.WarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by paul on 13/11/15.
 *
 * Tests the PlayerInfo class
 */
public class PlayerInfoTest {

    @Before
    public void setUp() throws Exception {
        Bukkit.setServer(mock(Server.class, RETURNS_DEEP_STUBS));

    }

    @Test
    public void testClearArmor() throws Exception {
        Player player = mock(Player.class, RETURNS_DEEP_STUBS);

        PlayerInfo playerInfo = new PlayerInfo(null, player);

        playerInfo.clearArmor();

        verify(player.getInventory(), times(1)).setHelmet(null);
        verify(player.getInventory(), times(1)).setChestplate(null);
        verify(player.getInventory(), times(1)).setLeggings(null);
        verify(player.getInventory(), times(1)).setBoots(null);
    }

    @Test
    public void testSetInGame() throws Exception {
        WarsPlugin plugin = mock(WarsPlugin.class, RETURNS_DEEP_STUBS);
        Player player = mock(Player.class, RETURNS_DEEP_STUBS);

        PlayerInfo playerInfo = new PlayerInfo(plugin, player);

        playerInfo.setInGame(true);
        assertTrue(playerInfo.isInGame());

        playerInfo.addPlayerCash(3000);
        playerInfo.setUpgradeLevel("test", 3);

        playerInfo.setInGame(false);

        assertEquals(playerInfo.getPlayerCash(), 0);
        assertEquals(playerInfo.getUpgradeLevel("test"), 0);
        assertFalse(playerInfo.isInGame());
    }
}
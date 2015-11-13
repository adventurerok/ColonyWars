package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by paul on 13/11/15.
 *
 * Tests the PlayerInfo class
 */
public class UserTest {

    @BeforeClass
    public static void setUp() throws Exception {
        try{
            Bukkit.setServer(mock(Server.class, RETURNS_DEEP_STUBS));
        } catch (UnsupportedOperationException ignored){}

    }

    @Test
    public void testClearArmor() throws Exception {
        Player player = mock(Player.class, RETURNS_DEEP_STUBS);

        User user = new User(null, player);

        user.clearArmor();

        verify(player.getInventory(), times(1)).setHelmet(null);
        verify(player.getInventory(), times(1)).setChestplate(null);
        verify(player.getInventory(), times(1)).setLeggings(null);
        verify(player.getInventory(), times(1)).setBoots(null);
    }

    @Test
    public void testSetInGame() throws Exception {
        WarsPlugin plugin = mock(WarsPlugin.class, RETURNS_DEEP_STUBS);
        Player player = mock(Player.class, RETURNS_DEEP_STUBS);

        User user = new User(plugin, player);

        user.setInGame(true);

        try {
            user.setTeamColor(TeamColor.RED);
        } catch(ClassCastException ignored) {} //when the armor tries to update. Cannot cast ItemMeta to
        // LeatherArmorMeta
        assertTrue(user.isInGame());

        user.addPlayerCash(3000);
        user.setUpgradeLevel("test", 3);

        user.setInGame(false);

        assertEquals(user.getPlayerCash(), 0);
        assertEquals(user.getUpgradeLevel("test"), 0);
        assertFalse(user.isInGame());
    }
}
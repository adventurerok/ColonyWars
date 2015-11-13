package com.ithinkrok.mccw.data;

import org.bukkit.entity.Player;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Created by paul on 13/11/15.
 *
 * Tests the PlayerInfo class
 */
public class PlayerInfoTest {



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
}
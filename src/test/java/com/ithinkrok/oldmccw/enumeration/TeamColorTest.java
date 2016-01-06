package com.ithinkrok.oldmccw.enumeration;

import com.ithinkrok.oldmccw.data.TeamColor;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by paul on 24/11/15.
 *
 * Tests the TeamColor class
 */
public class TeamColorTest {

    @Test
    public void testInitialise() throws Exception {
        initialiseTest(-3);
        initialiseTest(1);
        initialiseTest(17);
        initialiseTest(16);
        initialiseTest(12);
        initialiseTest(7);
    }

    private void initialiseTest(int teamCount) {
        boolean run = false;
        try{
            TeamColor.initialise(teamCount);
            run = true;

            assertTrue(TeamColor.values().size() == teamCount);
        } catch(IllegalArgumentException ignored) {
        }

        assertTrue(run == (teamCount >= 2 && teamCount <= 16));
    }

    @Test
    public void testFromWoolColor() throws Exception {
        TeamColor.initialise(16);

        for(TeamColor teamColor : TeamColor.values()){
            assertTrue(TeamColor.fromWoolColor(teamColor.getDyeColor().getWoolData()) == teamColor);
        }
    }

    @Test
    public void testFromName() throws Exception {
        TeamColor.initialise(16);

        for(TeamColor teamColor : TeamColor.values()){
            assertTrue(TeamColor.fromName(teamColor.getName()) == teamColor);
        }

        assertNull(TeamColor.fromName(null));
    }

    @Test
    public void testToString() throws Exception {
        TeamColor.initialise(16);

        for(TeamColor teamColor : TeamColor.values()){
            assertEquals(teamColor.getName(), teamColor.toString());
        }
    }
}
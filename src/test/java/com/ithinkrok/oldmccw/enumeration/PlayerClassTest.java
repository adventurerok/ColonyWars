package com.ithinkrok.oldmccw.enumeration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by paul on 24/11/15.
 *
 * Tests PlayerClass
 */
public class PlayerClassTest {

    @Test
    public void testFromChooserMaterial() throws Exception {
        for(PlayerClass playerClass : PlayerClass.values()){
            assertEquals(playerClass, PlayerClass.fromChooserMaterial(playerClass.getChooser()));
        }
    }

    @Test
    public void testFromName() throws Exception {
        for(PlayerClass playerClass : PlayerClass.values()){
            assertEquals(playerClass, PlayerClass.fromName(playerClass.getName()));
        }
    }

    @Test
    public void testToString() throws Exception {
        for(PlayerClass playerClass : PlayerClass.values()){
            assertEquals(playerClass.getName(), playerClass.toString());
        }
    }
}
package com.ithinkrok.mccw.data;

import com.ithinkrok.mccw.WarsPlugin;
import com.ithinkrok.mccw.enumeration.TeamColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Created by paul on 04/11/15.
 *
 * Represents info on a building built during the current game
 */
public class BuildingInfo {

    private String buildingName;
    private TeamColor teamColor;
    private WarsPlugin plugin;
    private List<Location> buildingBlocks;
    private Location centerBlock;
    private Vector minBB;
    private Vector maxBB;

    public BuildingInfo(WarsPlugin plugin, String buildingName, TeamColor teamColor, Location centerBlock,
                        List<Location> buildingBlocks) {
        this.plugin = plugin;
        this.buildingName = buildingName;
        this.teamColor = teamColor;
        this.centerBlock = centerBlock;
        this.buildingBlocks = buildingBlocks;
        
        Vector minBB = new Vector(centerBlock.getX(), centerBlock.getY(), centerBlock.getZ());
        Vector maxBB = new Vector(centerBlock.getX(), centerBlock.getY(), centerBlock.getZ());
        
        for(Location l : buildingBlocks){
            if(l.getX() < minBB.getX()) minBB.setX(l.getX());
            else if(l.getX() > maxBB.getX()) maxBB.setX(l.getX());

            if(l.getY() < minBB.getY()) minBB.setY(l.getY());
            else if(l.getY() > maxBB.getY()) maxBB.setY(l.getY());

            if(l.getZ() < minBB.getZ()) minBB.setZ(l.getZ());
            else if(l.getZ() > maxBB.getZ()) maxBB.setZ(l.getZ());
        }

        this.minBB = minBB;
        this.maxBB = maxBB;
    }

    public Vector getMinBB() {
        return minBB;
    }

    public Vector getMaxBB() {
        return maxBB;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public TeamColor getTeamColor() {
        return teamColor;
    }

    public List<Location> getBuildingBlocks() {
        return buildingBlocks;
    }

    public Location getCenterBlock() {
        return centerBlock;
    }

    public boolean canBuild(Vector minBB, Vector maxBB) {
        return maxBB.getX() < this.minBB.getX() || minBB.getX() > this.maxBB.getX() ||
                maxBB.getY() < this.minBB.getY() || minBB.getY() > this.maxBB.getY() ||
                maxBB.getZ() < this.minBB.getZ() || minBB.getZ() > this.maxBB.getZ();

    }
}

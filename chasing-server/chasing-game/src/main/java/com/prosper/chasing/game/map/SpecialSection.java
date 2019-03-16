package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.RoadPoint;
import com.prosper.chasing.game.util.Enums;

/**
 * Created by deacon on 2019/3/14.
 */
public class SpecialSection {

    public static int MIN_SIZE = 4;

    private short id;
    private Enums.TerrainType terrainType;
    private RoadPoint[] roadPoints;

    public SpecialSection(short id, Enums.TerrainType terrainType, RoadPoint[] roadPoints) {
        this.id = id;
        this.terrainType = terrainType;
        this.roadPoints = roadPoints;
    }

    public short getId() {
        return id;
    }

    public Enums.TerrainType getTerrainType() {
        return terrainType;
    }

    public RoadPoint[] getRoadPoints() {
        return roadPoints;
    }

    public boolean isSingle() {
        if (roadPoints.length == 1) return true;
        return false;
    }
}

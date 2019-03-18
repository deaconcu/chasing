package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2;
import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2018/11/23.
 */
public class BlockGroup {

    public static int MIN_SIZE = 4 * 50;

    private short id;
    private TerrainType terrainType;
    private Point3[] endPoints;
    private int distance;
    private int detourDistance;
    private boolean isSingle;
    private Point2 singlePoint;

    public BlockGroup(
            short id, TerrainType terrainType, Point3[] endPoints, int distance, int detourDistance) {
        this(id, terrainType, endPoints, distance, detourDistance, false, null);
    }

    public BlockGroup(
            short id, TerrainType terrainType, int startBlockId, int endBlockId, int distance,
            int detourDistance) {
        this(id, terrainType, null, distance, detourDistance, false, null);
    }

    public BlockGroup(short id, TerrainType terrainType, Point3[] endPoints,
            int distance, int detourDistance, boolean isSingle, Point2 singlePoint) {
        this.id = id;
        this.terrainType = terrainType;
        this.endPoints = endPoints;
        this.distance = distance;
        this.detourDistance = detourDistance;
        this.isSingle = isSingle;
        this.singlePoint = singlePoint;
    }

    public BlockGroup(short id, TerrainType terrainType, int startBlockId, int endBlockId,
                      int distance, int detourDistance, boolean isSingle) {
        this.id = id;
        this.terrainType = terrainType;
        this.endPoints = endPoints;
        this.distance = distance;
        this.detourDistance = detourDistance;
        this.isSingle = isSingle;
        this.singlePoint = singlePoint;
    }

    @Override
    public String toString() {
        return "[objectId:" + id + ", terrainType:" +  terrainType;
    }

    public byte[] getBytes() {
        ByteBuilder byteBuilder  = new ByteBuilder();
        byteBuilder.append(id);
        byteBuilder.append(terrainType.getValue());
        return byteBuilder.getBytes();
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public int getId() {
        return id;
    }

    public Point2 getSinglePoint() {
        return singlePoint;
    }

    public Point3[] getEndPoints() {
        return endPoints;
    }

    public int getStartBlockId() {
        return 0;
    }

    public int getEndBlockId() {
        return 0;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public int getDetourDistance() {
        return detourDistance;
    }
}

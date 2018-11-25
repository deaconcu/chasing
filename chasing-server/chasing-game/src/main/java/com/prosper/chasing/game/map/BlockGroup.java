package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2018/11/23.
 */
public class BlockGroup {

    private short id;
    private TerrainType terrainType;
    private int startBlockId;
    private int endBlockId;
    private int length;

    public BlockGroup(short id, TerrainType terrainType, int startBlockId, int endBlockId, int length) {
        this.id = id;
        this.terrainType = terrainType;
        this.startBlockId = startBlockId;
        this.endBlockId = endBlockId;
        this.length = length;
    }

    @Override
    public String toString() {
        return "[id:" + id + ", terrainType:" +  terrainType +
                ", start block:" + startBlockId + ", end block:" + endBlockId + "]";
    }

    public byte[] getBytes() {
        ByteBuilder byteBuilder  = new ByteBuilder();
        byteBuilder.append(id);
        byteBuilder.append(terrainType.getValue());
        byteBuilder.append(startBlockId);
        byteBuilder.append(endBlockId);

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

    public int getStartBlockId() {
        return startBlockId;
    }

    public void setStartBlockId(int startBlockId) {
        this.startBlockId = startBlockId;
    }

    public int getEndBlockId() {
        return endBlockId;
    }

    public void setEndBlockId(int endBlockId) {
        this.endBlockId = endBlockId;
    }

    public int getLength() {
        return length;
    }
}

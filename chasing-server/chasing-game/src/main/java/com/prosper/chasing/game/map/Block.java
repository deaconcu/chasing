package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;
import static com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2018/4/28.
 */
public class Block {

    public Point2D position;

    public int blockId;

    public BlockType type; // 0:wall 1:block area 2: road

    public TerrainType terrainType;

    public int distanceToFinish;

    public Block previous;

    public Block next;

    // 道路在当前block处的方向
    public RoadDirection roadDirection;

    public Block(Point2D position, int blockId, BlockType type) {
        this.position = position;
        this.blockId = blockId;
        this.type = type;
        this.distanceToFinish = 0;
        this.roadDirection = RoadDirection.NONE;
    }

    public Block(Point2D position, int blockId, BlockType type, TerrainType terrainType, RoadDirection roadDirection) {
        this(position, blockId, type);
        this.terrainType = terrainType;
        this.roadDirection = roadDirection;
    }

    public int getBlockId() {
        return blockId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Block)) return false;
        Block block = (Block) o;
        if (block == null || block.blockId != blockId) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return blockId;
    }

    @Override
    public String toString() {
        return "block id: " + blockId + ", type: " + type + ", position x: " + position.x + ", position y: " +
                position.y + ", distance to finish: " + distanceToFinish;
    }

}


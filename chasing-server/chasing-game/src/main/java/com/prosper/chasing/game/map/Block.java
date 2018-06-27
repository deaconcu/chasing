package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;
import static com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2018/4/28.
 */
public class Block {

    public Point2D position;

    public int blockId;

    public BlockType type;

    public short groupId;

    public TerrainType terrainType;

    public int distanceToFinish;

    public int distanceAwayFromRoad;  // 如果block类型为道路扩展，该值表示block距离道路中心的距离，否则为-1

    public int distanceAwayFromRoadCrossPoint;  // 如果block类型为道路，该值表示block距离道路转折点的距离，否则为-1

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
        this.distanceAwayFromRoadCrossPoint = -1;
        this.distanceAwayFromRoad = -1;
    }

    public int getBlockId() {
        return blockId;
    }

    public byte[] getBlockBytes() {
        if (distanceAwayFromRoad > 14) throw new RuntimeException("distance exceed limit");
        if (distanceAwayFromRoadCrossPoint > 14) throw new RuntimeException("distance exceed limit");

        byte blockInfo = (byte)(((distanceAwayFromRoad + 1) << 4) | (distanceAwayFromRoadCrossPoint + 1));
        if (type == BlockType.MOUNTAIN_L1 || type == BlockType.MOUNTAIN_L2 ||
                type == BlockType.MOUNTAIN_L3 || type == BlockType.SEA_L1 || type == BlockType.SEA_L2) {
            return new byte[]{type.getValue()};
        } else {
            return new byte[]{type.getValue(), blockInfo, (byte) (groupId >> 8), (byte)groupId};
        }
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


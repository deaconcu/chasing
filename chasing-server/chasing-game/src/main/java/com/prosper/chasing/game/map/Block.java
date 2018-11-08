package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;
import com.prosper.chasing.game.base.Position;

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

    public byte distanceAwayFromRoadExtention;  // 该值表示block距离道路(扩展块)的距离，否则为-1

    public int distanceAwayFromRoad;  // 如果block类型为道路扩展，该值表示block距离道路中心的距离，否则为-1

    public int distanceAwayFromRoadCrossPoint;  // 如果block类型为道路，该值表示block距离道路转折点的距离，否则为-1

    public byte height; // 高度数据

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

    /**
     * 获取block在地图上的实际坐标
     * 为了计算方便，在客户端block的position位置是block左下角点的位置
     * @return
     */
    public double[] getRealPosition() {
        return new double[]{position.x + 0.5, position.y + 0.5};
    }

    public int getDepth() {
        if (type == BlockType.SEA_L1) return -1;
        if (type == BlockType.SEA_L2) return -2;
        if (type == BlockType.SEA_L3) return -3;
        if (type == BlockType.SEA_L4) return -4;
        if (type == BlockType.SEA_L5) return -5;
        if (type == BlockType.SEA_L6) return -6;
        return 0;
    }

    public byte[] getBlockBytesV2() {
        if (distanceAwayFromRoad > 255) throw new RuntimeException("distance exceed limit");
        if (distanceAwayFromRoadCrossPoint > 255) throw new RuntimeException("distance exceed limit");

        return new byte[]{
                (byte)(blockId >> 24), (byte)(blockId >> 16), (byte)(blockId >> 8), (byte)(blockId),
                type.getValue(), (byte)distanceAwayFromRoad, (byte)distanceAwayFromRoadCrossPoint,
                (byte) (groupId >> 8), (byte)groupId};
    }

    public byte[] getBlockBytesV3() {
        if (distanceAwayFromRoad > 255) throw new RuntimeException("distance exceed limit");
        if (distanceAwayFromRoadCrossPoint > 255) throw new RuntimeException("distance exceed limit");

        return new byte[]{
                type.getValue(), (byte)distanceAwayFromRoad, (byte)distanceAwayFromRoadCrossPoint,
                (byte) (groupId >> 8), (byte)groupId};
    }

    public byte[] getBlockBytes() {
        if (distanceAwayFromRoad > 14) throw new RuntimeException("distance exceed limit");
        if (distanceAwayFromRoadCrossPoint > 14) throw new RuntimeException("distance exceed limit");

        byte blockInfo = (byte)(((distanceAwayFromRoad + 1) << 4) | (distanceAwayFromRoadCrossPoint + 1));
        if (type == BlockType.MOUNTAIN_L1 ||
                type == BlockType.MOUNTAIN_L2 ||
                type == BlockType.MOUNTAIN_L3 ||
                type == BlockType.MOUNTAIN_SLOP ||
                type == BlockType.MOUNTAIN_ROCK ||
                type == BlockType.BUILDING ||
                type == BlockType.SEA_L1 ||
                type == BlockType.SEA_L2 ||
                type == BlockType.SEA_L3 ||
                type == BlockType.SEA_L4 ||
                type == BlockType.SEA_L5 ||
                type == BlockType.SEA_L6) {
            return new byte[]{type.getValue(), height};
        } if (type == BlockType.WOODS || type == BlockType.HILL){
            return new byte[]{type.getValue(), height, distanceAwayFromRoadExtention};
        } else {
            return new byte[]{type.getValue(), height, blockInfo, (byte) (groupId >> 8), (byte)groupId};
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


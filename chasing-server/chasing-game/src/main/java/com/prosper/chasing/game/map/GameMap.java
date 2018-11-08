package com.prosper.chasing.game.map;

import com.prosper.chasing.common.util.Pair;
import com.prosper.chasing.game.base.Point;
import com.prosper.chasing.game.base.Point2D;

import static com.prosper.chasing.game.util.Enums.*;

import com.prosper.chasing.game.base.Position;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPOutputStream;

/**
 * Created by deacon on 2018/4/28.
 */
public class GameMap {

    public static Logger log = LoggerFactory.getLogger(GameMap.class);

    public static Map<BuildingType, Byte> buildingBlockCount = new HashMap<>();

    /**
     * 预定义的group id
     */
    public static short ROAD_GROUP_ID = 1;
    public static short MOUNTAIN_LEVEL_1_GROUP_ID = 2;
    public static short MOUNTAIN_LEVEL_2_GROUP_ID = 3;
    public static short MOUNTAIN_LEVEL_3_GROUP_ID = 4;

    static {
        buildingBlockCount.put(BuildingType.WAREHOUSE, (byte) 4);
        buildingBlockCount.put(BuildingType.STORE, (byte) 8);
        buildingBlockCount.put(BuildingType.WELL, (byte) 8);
        buildingBlockCount.put(BuildingType.TALL_TREE, (byte) 4);
        buildingBlockCount.put(BuildingType.GRAVEYARD, (byte) 4);
        buildingBlockCount.put(BuildingType.JACKSTRAW, (byte) 4);
    }

    public int boundX;

    public int boundY;

    public int bridgeWidth;

    /**
     * 主路
     */
    public Branch mainRoad;

    /**
     * 近路
     */
    public List<Branch> shortcutList;

    /**
     * 资源地块
     */
    public Map<Integer, TerrainType> resourceMap;

    /**
     * 建筑物
     */
    public Map<Integer, Building> buildingMap;

    /**
     * 地图上灯的集合
     */
    public Map<Integer, Lamp> lampMap;

    /**
     * 使用过的地块
     */
    public Map<Integer, Block> occupiedBlockMap;

    public Set<Integer> unoccupiedBlockSet;

    /**
     * 下一个group id值
     */
    private short nextGroupId;

    /**
     * 地图的bytes数据
     */
    private byte[] mapBytes;

    public Map<Short, TerrainType> TerrainTypeMapOfGroup;

    public GameMap(int boundX, int boundY, int bridgeWidth) {
        this.boundX = boundX;
        this.boundY = boundY;
        this.bridgeWidth = bridgeWidth;

        resourceMap = new HashMap<>();
        occupiedBlockMap = new HashMap<>();
        unoccupiedBlockSet = new HashSet<>();
        buildingMap = new HashMap<>();
        lampMap = new HashMap<>();

        for (int x = 0; x < boundX; x ++) {
            for (int y = 0; y < boundY; y ++) {
                int blockId = getBlockId(x, y);
                if (!isOccupied(blockId)) {
                    unoccupiedBlockSet.add(blockId);
                }
            }
        }
        nextGroupId = 5;
        TerrainTypeMapOfGroup = new HashMap<>();
        TerrainTypeMapOfGroup.put(ROAD_GROUP_ID, TerrainType.PAVEMENT);
        TerrainTypeMapOfGroup.put(MOUNTAIN_LEVEL_1_GROUP_ID, TerrainType.MOUNTAIN_L1);
        TerrainTypeMapOfGroup.put(MOUNTAIN_LEVEL_2_GROUP_ID, TerrainType.MOUNTAIN_L2);
        TerrainTypeMapOfGroup.put(MOUNTAIN_LEVEL_3_GROUP_ID, TerrainType.MOUNTAIN_L3);
    }

    public void setMainroad(Branch mainRoad) {
        this.mainRoad = mainRoad;
        generateSingleRouteInfo();
    }

    public void setShortcutList(List<Branch> shortcutList) {
        this.shortcutList = shortcutList;
    }

    public short getNextGroupId() {
        if (nextGroupId == Short.MAX_VALUE) {
            throw new RuntimeException("invalid group id");
        }
        return nextGroupId ++;
    }

    public void generateSingleRouteInfo() {
        Block block = mainRoad.blockList.get(mainRoad.blockList.size() - 1);
        int distance = 0;

        while (block != null) {
            block.distanceToFinish = distance ++;
            block = block.previous;
        }
    }

    public Block addBlock(int blockId, BlockType type, TerrainType terrainType, short groupId) {
        return addBlock(blockId, type, terrainType, RoadDirection.NONE, groupId, -1, -1);
    }

    public Block addBlock(int blockId, BlockType type, TerrainType terrainType) {
        return addBlock(blockId, type, terrainType, RoadDirection.NONE, ROAD_GROUP_ID, -1, -1);
    }

    public Block addBlock(int blockId, BlockType type, TerrainType terrainType,
                          int distanceAwayFromRoad, int distanceAwayFromRoadCrossPoint) {
        return addBlock(blockId, type, terrainType, RoadDirection.NONE, ROAD_GROUP_ID,
                distanceAwayFromRoad, distanceAwayFromRoadCrossPoint);
    }

    public Block addBlock(
            int blockId, BlockType type, TerrainType terrainType, RoadDirection roadDirection, short groupId,
            int distanceAwayFromRoad, int distanceAwayFromRoadCrossPoint) {
        if (isOccupied(blockId)) {
            return occupiedBlockMap.get(blockId);
        }
        if (terrainType == TerrainType.BUILDING) {
            //buildingMap.put(blockId, secondTerrainTypeId);
        } else if (terrainType == TerrainType.VIEW) {
            resourceMap.put(blockId, terrainType);
        } else {
            resourceMap.put(blockId, terrainType);
        }

        Block block = new Block(new Point2D(getX(blockId), getY(blockId)), blockId, type, terrainType, roadDirection);
        block.groupId = groupId;
        block.distanceAwayFromRoad = distanceAwayFromRoad;
        block.distanceAwayFromRoadCrossPoint = distanceAwayFromRoadCrossPoint;
        occupiedBlockMap.put(blockId, block);
        unoccupiedBlockSet.remove(blockId);
        return block;
    }

    public boolean isOccupied(int blockId) {
        if (!isInBound(getX(blockId), getY(blockId))) return true;
        return occupiedBlockMap.containsKey(blockId);
    }

    public boolean isOccupiedAround(int blockId, int aroundDistance) {
        int x = getX(blockId);
        int y = getY(blockId);

        for (int i = -aroundDistance; i <= aroundDistance; i ++) {
            for (int j = -aroundDistance; j <= aroundDistance; j ++) {
                if (i == 0 || j == 0) continue;
                if (isOccupied(getBlockId(x + i, y + j))) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getRandomMainRoadBlockId() {
        return mainRoad.blockList.get(ThreadLocalRandom.current().nextInt(mainRoad.blockList.size())).blockId;
    }

    /**
     * 获得地图道路上的一个随机位置
     * @param isMainRoad 该位置是否在主路上
     * @return
     */
    public Point getRandomRoadPosition(boolean isMainRoad) {
        if (isMainRoad) {
            int blockId = getRandomMainRoadBlockId();
            return new Point(getX(blockId), getY(blockId), 0);
        }
        return null;
    }

    public Block getBlock(int x, int y) {
        int blockId = getBlockId(x, y);
        return occupiedBlockMap.get(blockId);
    }

    protected int getBlockId(int x, int y) {
        return boundX * y + x + 1;
    }

    public int getX(int id) {
        return (id - 1) % boundX;
    }

    public int getY(int id) {
        return (id - 1) / boundX;
    }

    public boolean isInBound(int x, int y) {
        if (x < 0 || x >= boundX) return false;
        if (y < 0 || y >= boundY) return false;
        return true;
    }

    public void printMap() {
        char[][] terrainBytes = new char[boundX][boundY];
        for(int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                terrainBytes[i][j] = ' ';
            }
        }

        for (Block block: occupiedBlockMap.values()) {
            int x = block.position.x;
            int y = block.position.y;

            if (block.type == BlockType.ARTERY) terrainBytes[x][y] = 'm';
            if (block.type == BlockType.SHORTCUT) terrainBytes[x][y] = 'n';
            if (block.type == BlockType.BRANCH) terrainBytes[x][y] = 'o';
            if (block.type == BlockType.ROAD_EXTENSION) terrainBytes[x][y] = 'p';
            if (block.type == BlockType.BUILDING) terrainBytes[x][y] = 'r';
            else if (block.type == BlockType.MOUNTAIN_L1) terrainBytes[x][y] = 'b';
            else if (block.type == BlockType.MOUNTAIN_L2) terrainBytes[x][y] = 'c';
            else if (block.type == BlockType.MOUNTAIN_L3) terrainBytes[x][y] = 'd';
            else if (block.type == BlockType.MOUNTAIN_SLOP) terrainBytes[x][y] = 'j';
            else if (block.type == BlockType.MOUNTAIN_ROCK) terrainBytes[x][y] = 'e';
            else if (block.type == BlockType.SEA_L1) terrainBytes[x][y] = 'i';
            else if (block.type == BlockType.SEA_L2) terrainBytes[x][y] = 'i';
            else if (block.type == BlockType.SEA_L3) terrainBytes[x][y] = 'i';
            else if (block.type == BlockType.SEA_L4) terrainBytes[x][y] = 'i';
            else if (block.type == BlockType.SEA_L5) terrainBytes[x][y] = 'i';
            else if (block.type == BlockType.SEA_L6) terrainBytes[x][y] = 'k';
            else if (block.type == BlockType.WOODS) terrainBytes[x][y] = 's';
            else if (block.type == BlockType.HILL) terrainBytes[x][y] = 't';

            if (block.terrainType == TerrainType.ANIMAL) terrainBytes[x][y] = 'A';
            else if (block.terrainType == TerrainType.BLANK) terrainBytes[x][y] = 'B';
            else if (block.terrainType == TerrainType.FOG) terrainBytes[x][y] = 'C';
            else if (block.terrainType == TerrainType.FOREST) terrainBytes[x][y] = 'D';
            else if (block.terrainType == TerrainType.GRASS) terrainBytes[x][y] = 'E';
            else if (block.terrainType == TerrainType.LAVA) terrainBytes[x][y] = 'F';
            else if (block.terrainType == TerrainType.RAIN) terrainBytes[x][y] = 'G';
            //else if (block.terrainType == TerrainType.PAVEMENT) terrainBytes[x][y] = 'V';
            else if (block.terrainType == TerrainType.ROCK) terrainBytes[x][y] = 'I';
            else if (block.terrainType == TerrainType.SAND) terrainBytes[x][y] = 'J';
            else if (block.terrainType == TerrainType.SNOW) terrainBytes[x][y] = 'K';
            else if (block.terrainType == TerrainType.SWAMP) terrainBytes[x][y] = 'L';
            else if (block.terrainType == TerrainType.VEGETABLE) terrainBytes[x][y] = 'N';
            else if (block.terrainType == TerrainType.WATER) terrainBytes[x][y] = 'O';
            else if (block.terrainType == TerrainType.WIND) terrainBytes[x][y] = 'P';
            else if (block.terrainType == TerrainType.WILD_WIND) terrainBytes[x][y] = 'Q';
            else if (block.terrainType == TerrainType.WHEAT) terrainBytes[x][y] = 'R';
            else if (block.terrainType == TerrainType.VIEW) terrainBytes[x][y] = 'S';
            else if (block.terrainType == TerrainType.BUILDING) terrainBytes[x][y] = 'T';
            else if (block.terrainType == TerrainType.WALL) terrainBytes[x][y] = 'U';
            //else if (block.terrainType == TerrainType.MOUNTAIN_L1) terrainBytes[x][y] = 'V';
            //else if (block.terrainType == TerrainType.MOUNTAIN_L2) terrainBytes[x][y] = 'W';
            //else if (block.terrainType == TerrainType.MOUNTAIN_L3) terrainBytes[x][y] = 'X';
            //else terrainBytes[x][y] = 'Z';
        }

        for (Building building: buildingMap.values()) {
            int x = building.point2D.x;
            int y = building.point2D.y;

            if (building.direction == Direction.UP) terrainBytes[x][y] = '>';
            if (building.direction == Direction.RIGHT) terrainBytes[x][y] = 'v';
            if (building.direction == Direction.DOWN) terrainBytes[x][y] = '<';
            if (building.direction == Direction.LEFT) terrainBytes[x][y] = '^';

            /*
            if (building.buildingType == BuildingType.GRAVEYARD) terrainBytes[x][y] = '^';
            if (building.buildingType == BuildingType.JACKSTRAW) terrainBytes[x][y] = '&';
            if (building.buildingType == BuildingType.STORE) terrainBytes[x][y] = '*';
            if (building.buildingType == BuildingType.TALL_TREE) terrainBytes[x][y] = '<';
            if (building.buildingType == BuildingType.WAREHOUSE) terrainBytes[x][y] = '>';
            if (building.buildingType == BuildingType.WELL) terrainBytes[x][y] = '?';
            */
        }

        for(int i = 0; i < boundX; i ++) {
            printLineNo(i);
            for (int j = 0; j < boundY; j ++) {
                System.out.print(terrainBytes[i][j]);
            }
            System.out.println();
        }
    }

    public void printGroupId() {
        char[][] terrainBytes = new char[boundX][boundY];
        for(int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                terrainBytes[i][j] = ' ';
            }
        }

        for (Block block: occupiedBlockMap.values()) {
            int x = block.position.x;
            int y = block.position.y;

            terrainBytes[x][y] = (char) (block.groupId % 26 + 64);
        }

        for(int i = 0; i < boundX; i ++) {
            printLineNo(i);
            for (int j = 0; j < boundY; j ++) {
                System.out.print(terrainBytes[i][j]);
            }
            System.out.println();
        }
    }

    public void printDistances() {
        char[][] terrainBytes = new char[boundX][boundY];
        for(int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                terrainBytes[i][j] = ' ';
            }
        }

        for (Block block: occupiedBlockMap.values()) {
            int x = block.position.x;
            int y = block.position.y;

            if (block.distanceAwayFromRoadCrossPoint == 0) terrainBytes[x][y] = 'A';
            if (block.distanceAwayFromRoadCrossPoint == 1) terrainBytes[x][y] = 'B';
            if (block.distanceAwayFromRoadCrossPoint == 2) terrainBytes[x][y] = 'C';
            if (block.distanceAwayFromRoadCrossPoint == 3) terrainBytes[x][y] = 'D';
            if (block.distanceAwayFromRoadCrossPoint == 4) terrainBytes[x][y] = 'E';
            if (block.distanceAwayFromRoadCrossPoint == 5) terrainBytes[x][y] = 'F';
            if (block.distanceAwayFromRoadCrossPoint == 6) terrainBytes[x][y] = 'G';

            if (block.distanceAwayFromRoad == 1) terrainBytes[x][y] = 'a';
            if (block.distanceAwayFromRoad == 2) terrainBytes[x][y] = 'b';
            if (block.distanceAwayFromRoad == 3) terrainBytes[x][y] = 'c';
            if (block.distanceAwayFromRoad == 4) terrainBytes[x][y] = 'd';
            if (block.distanceAwayFromRoad == 5) terrainBytes[x][y] = 'e';
            if (block.distanceAwayFromRoad == 6) terrainBytes[x][y] = 'f';
        }

        for(int i = 0; i < boundX; i ++) {
            printLineNo(i);
            for (int j = 0; j < boundY; j ++) {
                System.out.print(terrainBytes[i][j]);
            }
            System.out.println();
        }
    }

    public void printHeight() {
        char[][] terrainBytes = new char[boundX][boundY];
        for(int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                terrainBytes[i][j] = ' ';
            }
        }

        for (Block block: occupiedBlockMap.values()) {
            int x = block.position.x;
            int y = block.position.y;

            terrainBytes[x][y] = (char)(block.height + 48);
        }

        for(int i = 0; i < boundX; i ++) {
            printLineNo(i);
            for (int j = 0; j < boundY; j ++) {
                System.out.print(terrainBytes[i][j]);
            }
            System.out.println();
        }
    }

    private void printLineNo(int i) {
        if (i < 10) System.out.print("000" + i + ": ");
        else if (i < 100) System.out.print("00" + i + ": ");
        else if (i < 1000) System.out.print("0" + i + ": ");
        else System.out.print(i + ": ");
    }

    public List<Integer> getBorderPoints(int x, int y, int width, int height, boolean partly) {
        if (width <= 0 || height <= 0) return null;

        if (!partly) {
            if (!isInBound(x, y)) return null;
            if (!isInBound(x + width, y)) return null;
            if (!isInBound(x, y + height)) return null;
            if (!isInBound(x + width, y + height)) return null;
        }

        List<Integer> borderPointList = new LinkedList<>();
        for (int i = 0; i <= width; i ++) {
            if (isInBound(x + i, y)) borderPointList.add(getBlockId(x + i, y));
        }
        for (int i = 1; i <= height; i ++) {
            if (isInBound(x, y + i)) borderPointList.add(getBlockId(x, y + i));
        }
        for (int i = 1; i <= width; i ++) {
            if (isInBound(x + i, y + height)) borderPointList.add(getBlockId(x + i, y + height));
        }
        for (int i = 1; i < height; i ++) {
            if (isInBound(x + width, y + i)) borderPointList.add(getBlockId(x + width, y + i));
        }
        return borderPointList;
    }

   public void addBranch(int blockIdA, int blockIdB) {
        int ax = getX(blockIdA);
        int ay = getY(blockIdA);
        int bx = getX(blockIdB);
        int by = getY(blockIdB);

        if (!isInBound(ax, ay)) throw new RuntimeException("block id is not in bound");
        if (!isInBound(bx, by)) throw new RuntimeException("block id is not in bound");

        if (ax != bx && ay != by) throw new RuntimeException("must in a line");

        if (ax > bx) {
            for (int i = bx + 1; i < ax; i ++) {
                int blockId = getBlockId(i, ay);
                if (isOccupied(blockId)) throw new RuntimeException("is occupied");
                addBlock(blockId, BlockType.BRANCH, TerrainType.PAVEMENT, RoadDirection.NONE, ROAD_GROUP_ID, -1, -1);
            }
        } else if (ax < bx) {
            for (int i = ax + 1; i < bx; i ++) {
                int blockId = getBlockId(i, ay);
                if (isOccupied(blockId)) throw new RuntimeException("is occupied");
                addBlock(blockId, BlockType.BRANCH, TerrainType.PAVEMENT, RoadDirection.NONE, ROAD_GROUP_ID, -1, -1);
            }
        } else if (ay > by) {
            for (int i = by + 1; i < ay; i ++) {
                int blockId = getBlockId(ax, i);
                if (isOccupied(blockId)) throw new RuntimeException("is occupied");
                addBlock(blockId, BlockType.BRANCH, TerrainType.PAVEMENT, RoadDirection.NONE, ROAD_GROUP_ID, -1, -1);
            }
        } else if (ay < by) {
            for (int i = ay + 1; i < by; i ++) {
                int blockId = getBlockId(ax, i);
                if (isOccupied(blockId)) throw new RuntimeException("is occupied");
                addBlock(blockId, BlockType.BRANCH, TerrainType.PAVEMENT, RoadDirection.NONE, ROAD_GROUP_ID, -1, -1);
            }
        }
    }

    /**
     * 判断某一个block在给定距离的周边上是否有临近的给定种类的block
     * @param distance 给定距离
     * @param blockTypes 给定block type
     */
    public boolean isNear(int blockId, int distance, BlockType... blockTypes) {
        int x = getX(blockId);
        int y = getY(blockId);

        List<Integer> borderBlockIdList = getBorderPoints(
                x - distance, y - distance, 2 * distance, 2 * distance, true);
        for (int borderBlockId: borderBlockIdList) {
            Block block = occupiedBlockMap.get(borderBlockId);
            if (block != null && Util.arrayContains(blockTypes, block.type)) return true;
        }
        return false;
    }

    /**
     * 判断当前节点所在线路的方向类型
     * @return
     */
    public RoadDirection getRoadDirection(int blockId, BlockType... blockTypes) {
        if (occupiedBlockMap.get(blockId) == null) return RoadDirection.NONE;

        int x = getX(blockId);
        int y = getY(blockId);

        List<Block> blockList = new LinkedList<>();
        for (int i = -1; i <= 1; i = i + 2) {
            if (isInBound(x + i, y)) {
                Block block = occupiedBlockMap.get(getBlockId(x + i, y));
                if (block != null && Util.arrayContains(blockTypes, block.type)) blockList.add(block);
            }
        }
        for (int i = -1; i <= 1; i = i + 2) {
            if (isInBound(x, y + i)) {
                Block block = occupiedBlockMap.get(getBlockId(x, y + i));
                if (block != null && Util.arrayContains(blockTypes, block.type)) blockList.add(block);
            }
        }

        if (blockList.size() > 2) {
            return RoadDirection.CROSS;
        } else if (blockList.size() == 2) {
            if (blockList.get(0).position.x == blockList.get(1).position.x) {
                return RoadDirection.VERTICAL;
            } else if (blockList.get(0).position.y == blockList.get(1).position.y) {
                return RoadDirection.HORIZONTAL;
            } else {
                return RoadDirection.TURNING;
            }
        } else if (blockList.size() == 1) {
            if (blockList.get(0).position.x == x) {
                return RoadDirection.VERTICAL_END;
            } else if (blockList.get(0).position.y == y) {
                return RoadDirection.HORIZONTAL_END;
            }
        }
        return RoadDirection.STAND_ALONE;
    }

    public void addBuilding(BuildingType buildingType, int x, int y, Direction direction) {
        int id = getBlockId(x, y);
        Building building = new Building(id, buildingType, new Point2D(x, y), direction);
        buildingMap.put(id, building);
    }

    public List<Block> getAdjacent(int blockId, int distance, boolean corner, BlockType... blockTypes) {
        if (distance == 1) return getAdjacentInDistance(blockId, distance, corner, blockTypes);

        List<Block> blockList = new LinkedList<>();
        for (int i = 1; i <= distance; i ++) {
            blockList.addAll(getAdjacentInDistance(blockId, distance, corner, blockTypes));
        }
        return blockList;
    }

    public List<Block> getAdjacentInDistance(int blockId, int distance, boolean corner, BlockType... blockTypes) {
        int x = getX(blockId);
        int y = getY(blockId);

        List<Integer> borderBlockIdList = new LinkedList<>();
        if (corner) {
            borderBlockIdList.add(getBlockId(x - distance, y + distance));
            borderBlockIdList.add(getBlockId(x, y + distance));
            borderBlockIdList.add(getBlockId(x + distance, y + distance));
            borderBlockIdList.add(getBlockId(x + distance, y));
            borderBlockIdList.add(getBlockId(x, y));
            borderBlockIdList.add(getBlockId(x + distance, y - distance));
            borderBlockIdList.add(getBlockId(x, y - distance));
            borderBlockIdList.add(getBlockId(x - distance, y - distance));
            borderBlockIdList.add(getBlockId(x - distance, y));
        } else {
            borderBlockIdList.add(getBlockId(x, y + distance));
            borderBlockIdList.add(getBlockId(x + distance, y));
            borderBlockIdList.add(getBlockId(x, y - distance));
            borderBlockIdList.add(getBlockId(x - distance, y));
        }

        List<Block> blockList = new LinkedList<>();
        for (int borderBlockId: borderBlockIdList) {
            Block block = occupiedBlockMap.get(borderBlockId);
            if (block != null && Util.arrayContains(blockTypes, block.type)) {
                blockList.add(block);
            }
        }
        return blockList;
    }

    public Map<Block, Direction> getAdjacentWithDirInDistance(int blockId, int distance, BlockType... blockTypes) {
        int x = getX(blockId);
        int y = getY(blockId);

        Block upBlock = occupiedBlockMap.get(getBlockId(x, y + distance));
        Block rightBlock = occupiedBlockMap.get(getBlockId(x + distance, y));
        Block downBlock = occupiedBlockMap.get(getBlockId(x, y - distance));
        Block leftBlock = occupiedBlockMap.get(getBlockId(x - distance, y));

        Map<Block, Direction> blockMap = new HashMap<>();
        if (upBlock != null && Util.arrayContains(blockTypes, upBlock.type)) {
            blockMap.put(upBlock, Direction.UP);
        }
        if (rightBlock != null && Util.arrayContains(blockTypes, rightBlock.type)) {
            blockMap.put(rightBlock, Direction.RIGHT);
        }
        if (downBlock != null && Util.arrayContains(blockTypes, downBlock.type)) {
            blockMap.put(downBlock, Direction.DOWN);
        }
        if (leftBlock != null && Util.arrayContains(blockTypes, leftBlock.type)) {
            blockMap.put(leftBlock, Direction.LEFT);
        }
        return blockMap;
    }

    public boolean isAdjacent(int blockId, int distance, BlockType... blockTypes) {
        int x = getX(blockId);
        int y = getY(blockId);

        List<Integer> borderBlockIdList = new LinkedList<>();
        borderBlockIdList.add(getBlockId(x - distance, y));
        borderBlockIdList.add(getBlockId(x + distance, y));
        borderBlockIdList.add(getBlockId(x, y - distance));
        borderBlockIdList.add(getBlockId(x, y + distance));

        for (int borderBlockId: borderBlockIdList) {
            Block block = occupiedBlockMap.get(borderBlockId);
            if (block != null && Util.arrayContains(blockTypes, block.type)) return true;
        }
        return false;
    }

    /**
     * 获取在某一个block的十字交叉线上指定距离和指定类型的block集合
     */
    public List<Block> getBlocksInRangeOfCross(int blockId, int range, BlockType... blockTypes) {
        int x = getX(blockId);
        int y = getY(blockId);

        List<Integer> blockIdList = new LinkedList<>();
        for (int i = 1; i <= range; i ++) {
            if (isInBound(x - i, y)) blockIdList.add(getBlockId(x - i, y));
            if (isInBound(x + i, y)) blockIdList.add(getBlockId(x + i, y));
            if (isInBound(x, y - i)) blockIdList.add(getBlockId(x, y - i));
            if (isInBound(x, y + i)) blockIdList.add(getBlockId(x, y + i));
        }

        List<Block> blockList = new LinkedList<>();
        for (int blockIdInRange: blockIdList) {
            Block block = occupiedBlockMap.get(blockIdInRange);
            if (block != null && Util.arrayContains(blockTypes, block.type)) {
                blockList.add(block);
            }
        }
        return blockList;
    }

    /**
     * 获取在指定距离内，给定block与指定类型最近的距离
     * @param blockId 给定的blockId
     * @param range 给定的距离
     */
    public Pair<Integer, Block> getNearestBlockOfCross(int blockId, int range, BlockType... blockTypes) {
        int x = getX(blockId);
        int y = getY(blockId);

        Block block = occupiedBlockMap.get(blockId);
        if (block != null && Util.arrayContains(blockTypes, block.type)) return null;

        List<Integer> blockIdList = new LinkedList<>();
        for (int i = 1; i <= range; i ++) {
            blockIdList.clear();
            if (isInBound(x - i, y)) blockIdList.add(getBlockId(x - i, y));
            if (isInBound(x + i, y)) blockIdList.add(getBlockId(x + i, y));
            if (isInBound(x, y - i)) blockIdList.add(getBlockId(x, y - i));
            if (isInBound(x, y + i)) blockIdList.add(getBlockId(x, y + i));

            for (int blockIdInRange: blockIdList) {
                Block nearBlock = occupiedBlockMap.get(blockIdInRange);
                if (nearBlock != null && Util.arrayContains(blockTypes, nearBlock.type)) {
                    return new Pair<>(i, nearBlock);
                }
            }
        }
        return null;
    }

    /**
     * 获取在某一个block的四个对角线方向, 指定距离和指定类型的block集合
     */
    public List<Block> getBlocksInRangeOfCorner(int blockId, int range, BlockType... blockTypes) {
        int x = getX(blockId);
        int y = getY(blockId);

        List<Integer> blockIdList = new LinkedList<>();
        if (isInBound(x - range, y + range)) blockIdList.add(getBlockId(x - range, y + range));
        if (isInBound(x + range, y + range)) blockIdList.add(getBlockId(x + range, y + range));
        if (isInBound(x + range, y - range)) blockIdList.add(getBlockId(x + range, y - range));
        if (isInBound(x - range, y - range)) blockIdList.add(getBlockId(x - range, y - range));

        List<Block> blockList = new LinkedList<>();
        for (int blockIdInRange: blockIdList) {
            Block block = occupiedBlockMap.get(blockIdInRange);
            if (block != null && Util.arrayContains(blockTypes, block.type)) {
                blockList.add(block);
            }
        }
        return blockList;
    }

    /**
     * 获取在某一个block周围, 指定距离和指定类型的block集合
     */
    public List<Block> getBlocksInRange(int blockId, int range, BlockType... blockTypes) {
        int x = getX(blockId);
        int y = getY(blockId);

        List<Integer> blockIdList = new LinkedList<>();
        for (int i = -range ; i <= range; i ++) {
            for (int j = -range; j <= range; j ++) {
                if (i == 0 && j == 0) continue;
                if (isInBound(x + i, y + i)) blockIdList.add(getBlockId(x + i, y + j));
            }
        }

        List<Block> blockList = new LinkedList<>();
        for (int blockIdInRange: blockIdList) {
            Block block = occupiedBlockMap.get(blockIdInRange);
            if (block != null && Util.arrayContains(blockTypes, block.type)) {
                blockList.add(block);
            }
        }
        return blockList;
    }

    public BlockType getBlockTypeByDepth(int depth) {
        if (depth <= -6) return BlockType.SEA_L6;
        if (depth == -5) return BlockType.SEA_L5;
        if (depth == -4) return BlockType.SEA_L4;
        if (depth == -3) return BlockType.SEA_L3;
        if (depth == -2) return BlockType.SEA_L2;
        if (depth == -1) return BlockType.SEA_L1;
        return null;
    }

    public int getNearestBlockDistanceOfAround(int blockId, int distance, BlockType... blockTypes) {
        if (distance < 1) return -1;
        for (int i = 1; i <= distance; i ++ ) {
            if (getBlocksAroundInDistance(blockId, i, blockTypes).size() > 0) return i;
        }
        return -1;
    }

    public List<Block> getNearestBlocksOfAround(int blockId, int distance, BlockType... blockTypes) {
        if (distance < 1) return null;
        for (int i = 1; i <= distance; i ++ ) {
            List<Block> blockList = getBlocksAroundInDistance(blockId, i, blockTypes);
            if (blockList.size() > 0) return blockList;
        }
        return null;
    }

    public Map<Integer, List<Block>> getBlocksAroundInDistances(int blockId, int distance, BlockType... blockTypes) {
        if (distance < 1) return null;
        Map<Integer, List<Block>> blockMap = new HashMap<>();
        for (int i = 1; i <= distance; i ++ ) {
            blockMap.put(i, getBlocksAroundInDistance(blockId, i, blockTypes));
        }
        return blockMap;
    }

    public List<Block> getBlocksAroundInDistance(int blockId, int distance, BlockType... blockTypes) {
        int x = getX(blockId);
        int y = getY(blockId);

        List<Integer> blockIdList = new LinkedList<>();
        for (int i = -distance ; i <= distance; i += distance * 2) {
            for (int j = -distance; j <= distance; j ++) {
                if (isInBound(x + i, y + j)) blockIdList.add(getBlockId(x + i, y + j));
            }
        }

        for (int j = -distance ; j <= distance; j += distance * 2) {
            for (int i = -distance; i <= distance; i++) {
                if (isInBound(x + i, y + j)) blockIdList.add(getBlockId(x + i, y + j));
            }
        }

        List<Block> blockList = new LinkedList<>();
        for (int blockIdInRange: blockIdList) {
            Block block = occupiedBlockMap.get(blockIdInRange);
            if (block != null && Util.arrayContains(blockTypes, block.type)) {
                blockList.add(block);
            }
        }
        return blockList;
    }

    /**
     * 返回block类型，如果block没有被使用，返回null
     *
     */
    public BlockType getBlockType(int x, int y) {
        int blockId = getBlockId(x, y);
        if (!occupiedBlockMap.containsKey(blockId)) {
            return null;
        }
        return occupiedBlockMap.get(blockId).type;
    }

    public Direction getDirection(int blockIdA, int blockIdB) {
        int ax = getX(blockIdA);
        int ay = getY(blockIdA);

        int bx = getX(blockIdB);
        int by = getY(blockIdB);

        if (ax == bx && by > ay) return Direction.UP;
        if (ax == bx && by < ay) return Direction.DOWN;
        if (ay == by && bx > ax) return Direction.RIGHT;
        if (ay == by && bx < ax) return Direction.LEFT;

        if (bx < ax && by > ay) return Direction.UP_LEFT;
        if (bx < ax && by < ay) return Direction.DOWN_LEFT;
        if (bx > ax && by > ay) return Direction.UP_RIGHT;
        if (bx > ax && by < ay) return Direction.DOWN_RIGHT;

        return Direction.FREE;
    }

    public void generateMapBytes() {
        ByteBuilder byteBuilder = new ByteBuilder();
        for (int i = 1; i <= boundX * boundY; i ++) {
            Block block = occupiedBlockMap.get(i);
            if (block == null) {
                byteBuilder.append((byte)0);
            } else {
                byte[] blockBytes = block.getBlockBytesV3();
                byteBuilder.append(blockBytes);
            }
        }

        byteBuilder.append(lampMap.size());
        for (Lamp lamp: lampMap.values()) {
            lamp.getBytes(byteBuilder);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(byteBuilder.getBytes());
            gzip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapBytes = out.toByteArray();

        System.out.print("\n");
        int line = 1;
        for (byte blockByte: mapBytes) {
            System.out.print(blockByte & 0xFF);
            System.out.print(",");

            if (line ++ % 100 == 0) {
                System.out.print("\n");
            }
        }

        System.out.print("\n");

        log.info("map bytes length: " + byteBuilder.getBytes().length);
        log.info("compressed map bytes length: " + mapBytes.length);
    }

    public byte[] getMapBytes() {
        return mapBytes;
    }
}

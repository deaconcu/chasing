package com.prosper.chasing.game.map;

import com.prosper.chasing.common.util.Pair;
import com.prosper.chasing.game.base.Point;
import com.prosper.chasing.game.base.Point2D;

import static com.prosper.chasing.game.util.Enums.*;

import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Graph;
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
    public static short ROAD_GROUP_ID = 0;

    static {
        buildingBlockCount.put(BuildingType.WAREHOUSE, (byte) 4);
        buildingBlockCount.put(BuildingType.STORE, (byte) 8);
        buildingBlockCount.put(BuildingType.WELL, (byte) 8);
        buildingBlockCount.put(BuildingType.TALL_TREE, (byte) 4);
        buildingBlockCount.put(BuildingType.GRAVEYARD, (byte) 4);
        buildingBlockCount.put(BuildingType.JACKSTRAW, (byte) 4);
    }

    public int startBlockId;
    public int endBlockId;

    public int boundX;

    public int boundY;

    public int bridgeWidth;

    public int expandWidth;

    /**
     * 主路
     */
    public SegmentOld mainRoad;

    /**
     * 近路
     */
    public List<SegmentOld> shortcutList;

    /**
     * segment集合
     */
    public List<SegmentOld> segmentList;

    /**
     * 路径集合
     */
    Map<Integer, Map<Integer, List<Integer>>> pathMap;

    /**
     * 交叉点id集合
     */
    public Set<Integer> crossBlockIdSet;
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

    public Map<Short, BlockGroup> blockGroupMap;

    public GameMap(int boundX, int boundY, int bridgeWidth) {
        this.boundX = boundX;
        this.boundY = boundY;
        this.bridgeWidth = bridgeWidth;

        resourceMap = new HashMap<>();
        occupiedBlockMap = new HashMap<>();
        unoccupiedBlockSet = new HashSet<>();
        buildingMap = new HashMap<>();
        lampMap = new HashMap<>();
        crossBlockIdSet = new HashSet<>();

        for (int x = 0; x < boundX; x ++) {
            for (int y = 0; y < boundY; y ++) {
                int blockId = getBlockId(x, y);
                if (!isOccupied(blockId)) {
                    unoccupiedBlockSet.add(blockId);
                }
            }
        }
        blockGroupMap = new HashMap<>();
        //blockGroupMap.put(ROAD_GROUP_ID, TerrainType.PAVEMENT);
        nextGroupId = 1;
    }

    public void setMainRoad(SegmentOld mainRoad) {
        this.mainRoad = mainRoad;
        generateSingleRouteInfo();
    }

    public void setShortcutList(List<SegmentOld> shortcutList) {
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

    public Block addBlock(int blockId, BlockType type, short groupId) {
        return addBlock(blockId, type, RoadDirection.NONE, groupId, -1, -1);
    }

    public Block addBlock(int blockId, BlockType type) {
        return addBlock(blockId, type, RoadDirection.NONE, ROAD_GROUP_ID, -1, -1);
    }

    public Block addBlock(int blockId, BlockType type, int distanceAwayFromRoad, int distanceAwayFromRoadCrossPoint) {
        return addBlock(blockId, type, RoadDirection.NONE, ROAD_GROUP_ID,
                distanceAwayFromRoad, distanceAwayFromRoadCrossPoint);
    }

    public Block addBlock(
            int blockId, BlockType type, RoadDirection roadDirection, short groupId,
            int distanceAwayFromRoad, int distanceAwayFromRoadCrossPoint) {
        if (isOccupied(blockId)) {
            return occupiedBlockMap.get(blockId);
        }

        /*
        if (terrainType == TerrainType.BUILDING) {
            //buildingMap.put(hexagonId, secondTerrainTypeId);
        } else if (terrainType == TerrainType.VIEW) {
            resourceMap.put(hexagonId, terrainType);
        } else {
            resourceMap.put(hexagonId, terrainType);
        }
        */

        Block block = new Block(new Point2D(getX(blockId), getY(blockId)), blockId, type, roadDirection);
        block.blockGroupId = groupId;
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

    public Block getBlock(int blockId) {
        if (!isOccupied(blockId)) return null;
        return occupiedBlockMap.get(blockId);
    }

    public Block getStartBlock() {
        return occupiedBlockMap.get(startBlockId);
    }

    public Block getEndBlock() {
        return occupiedBlockMap.get(endBlockId);
    }

    public boolean isArtery(int blockId) {
        if (!isOccupied(blockId) || occupiedBlockMap.get(blockId).distanceAwayFromRoad != 0) return false;
        return true;
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

    public int getRandomRoadBlockId() {
        // FOR TEST
        //return mainRoad.hexagonList.get(ThreadLocalRandom.current().nextInt(100)).hexagonId;
        //return mainRoad.hexagonList.get(ThreadLocalRandom.current().nextInt(mainRoad.hexagonList.size())).hexagonId;
        SegmentOld segment = segmentList.get(ThreadLocalRandom.current().nextInt(segmentList.size()));
        return segment.blockList.get(ThreadLocalRandom.current().nextInt(segment.blockList.size())).blockId;
    }

    /**
     * 获得地图道路上的一个随机位置
     * @param isMainRoad 该位置是否在主路上
     * @return
     */
    public Point getRandomRoadPosition(boolean isMainRoad) {
        if (isMainRoad) {
            int blockId = getRandomRoadBlockId();
            return new Point(getX(blockId), getY(blockId), 0);
        }
        return null;
    }

    /**
     * 随机获取部分交叉点
     * @percent 百分制 需要获取的交叉点比例
     */
    public List<Integer> getRandomRoadCrossPosition(int percent) {
        List<Integer> crossPointList = new LinkedList<>();
        for (int blockId: crossBlockIdSet) {
            if (ThreadLocalRandom.current().nextInt(100) < percent) {
                crossPointList.add(blockId);
            }
        }
        return crossPointList;
    }

    public Block getBlock(int x, int y) {
        int blockId = getBlockId(x, y);
        return occupiedBlockMap.get(blockId);
    }

    public int getBlockId(int x, int y) {
        return boundX * y + x + 1;
    }

    public int getBlockId(Point point) {
        return getBlockId(point.x, point.z);
    }

    public int getX(int id) {
        return (id - 1) % boundX;
    }

    public int getY(int id) {
        return (id - 1) / boundX;
    }

    public Point getPoint(int blockId) {
        return new Point(getX(blockId), 0, getY(blockId));
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

            if (block.type == BlockType.MAIN_ROAD) terrainBytes[x][y] = 'm';
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

            if (getTerrainType(block) == TerrainType.ANIMAL) terrainBytes[x][y] = 'A';
            else if (getTerrainType(block) == TerrainType.BLANK) terrainBytes[x][y] = 'B';

            else if (getTerrainType(block) == TerrainType.FOG) terrainBytes[x][y] = 'C';
            else if (getTerrainType(block) == TerrainType.RAIN) terrainBytes[x][y] = 'G';
            else if (getTerrainType(block) == TerrainType.SNOW) terrainBytes[x][y] = 'K';
            else if (getTerrainType(block) == TerrainType.DREAM_L1) terrainBytes[x][y] = 'E';
            else if (getTerrainType(block) == TerrainType.DREAM_L2) terrainBytes[x][y] = 'F';

            else if (getTerrainType(block) == TerrainType.STONE) terrainBytes[x][y] = '.';
            else if (getTerrainType(block) == TerrainType.GATE) terrainBytes[x][y] = '.';
            else if (getTerrainType(block) == TerrainType.WATER) terrainBytes[x][y] = '.';
            else if (getTerrainType(block) == TerrainType.RIVER) terrainBytes[x][y] = '.';

            else if (getTerrainType(block) == TerrainType.FOREST) terrainBytes[x][y] = 'D';
            //else if (block.terrainType == TerrainType.PAVEMENT) terrainBytes[x][y] = 'V';
            else if (getTerrainType(block) == TerrainType.ANIMAL_OSTRICH) terrainBytes[x][y] = 'I';
            else if (getTerrainType(block) == TerrainType.SAND) terrainBytes[x][y] = 'J';
            else if (getTerrainType(block) == TerrainType.SWAMP) terrainBytes[x][y] = 'L';
            else if (getTerrainType(block) == TerrainType.VEGETABLE) terrainBytes[x][y] = 'N';
            else if (getTerrainType(block) == TerrainType.WATER) terrainBytes[x][y] = 'O';
            else if (getTerrainType(block) == TerrainType.WIND) terrainBytes[x][y] = 'P';
            else if (getTerrainType(block) == TerrainType.WILD_WIND) terrainBytes[x][y] = 'Q';
            else if (getTerrainType(block) == TerrainType.WHEAT) terrainBytes[x][y] = 'R';
            else if (getTerrainType(block) == TerrainType.VIEW) terrainBytes[x][y] = 'S';
            else if (getTerrainType(block) == TerrainType.BUILDING) terrainBytes[x][y] = 'T';
            else if (getTerrainType(block) == TerrainType.WALL) terrainBytes[x][y] = 'U';
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

            terrainBytes[x][y] = (char) (block.blockGroupId % 26 + 64);
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

    /**
     * 获取在给定block周围，指定距离的菱形范围内，离已占用block的最小距离
     * @param blockId 给定的blockId
     * @param distance 给定的距离
     */
    public Pair<Integer, Short> getNearestBlockInfo(int blockId, int distance) {
        Block block = occupiedBlockMap.get(blockId);
        if (block != null) return new Pair(0, block.blockGroupId);

        int x = getX(blockId);
        int y = getY(blockId);

        for (int m = 1; m <= distance; m ++) {
            boolean found = false;
            short groupId = Short.MAX_VALUE;
            for (int i = - m; i <= m; i++) {
                int[] blockIds = new int[] {
                        getBlockId(x + i, y + (m - Math.abs(i))), getBlockId(x + i, y - (m - Math.abs(i)))};
                for (int currBlockId: blockIds) {
                    block = occupiedBlockMap.get(currBlockId);
                    if (block != null) {
                        found = true;
                        if (block.blockGroupId < groupId) groupId = block.blockGroupId;
                    }
                }
            }
            if (found) {
                return new Pair(m, groupId);
            }
        }
        return null;
    }

    /**
     * 获取离中心点距离为 distance 的一个正方形区域内的全部block
     */
    public Map<Integer, List<Block>> getBlocksInDistances(int blockId, int distance, BlockType... blockTypes) {
        if (distance < 1) return null;
        Map<Integer, List<Block>> blockMap = new HashMap<>();
        for (int i = 1; i <= distance; i ++ ) {
            blockMap.put(i, getBlocksInDistance(blockId, i, blockTypes));
        }
        return blockMap;
    }

    /**
     * 获取离中心点距离为 distance 的一个正方形边的全部block
     */
    public List<Block> getBlocksInDistance(int blockId, int distance, BlockType... blockTypes) {
        int x = getX(blockId);
        int y = getY(blockId);

        //List<Integer> blockIdList = new LinkedList<>();
        List<Block> blockList = new LinkedList<>();
        for (int i = -distance ; i <= distance; i += distance * 2) {
            for (int j = -distance; j <= distance; j ++) {
                if (isInBound(x + i, y + j)) {
                    Block block = occupiedBlockMap.get(getBlockId(x + i, y + j));
                    if (block != null && Util.arrayContains(blockTypes, block.type)) {
                        blockList.add(block);
                    }
                }
            }
        }

        for (int j = -distance ; j <= distance; j += distance * 2) {
            for (int i = -distance + 1; i <= distance - 1; i++) {
                if (isInBound(x + i, y + j)) {
                    Block block = occupiedBlockMap.get(getBlockId(x + i, y + j));
                    if (block != null && Util.arrayContains(blockTypes, block.type)) {
                        blockList.add(block);
                    }
                }
            }
        }
        return blockList;
    }

    public List<Block> getBlocksInDistance(Point position, int distance, BlockType... blockTypes) {
        return getBlocksInDistance(getBlockId(position.x, position.z), distance, blockTypes);
    }

    /**
     * 获取某一个block周围指定距离为distance的非道路块
     */
    public List<Integer> getUnoccupiedBlocksInDistance(int blockId, int distance) {
        int x = getX(blockId);
        int y = getY(blockId);

        //List<Integer> blockIdList = new LinkedList<>();
        List<Integer> blockList = new LinkedList<>();
        for (int i = -distance ; i <= distance; i += distance * 2) {
            for (int j = -distance; j <= distance; j ++) {
                if (isInBound(x + i, y + j)) {
                    int distanceBlockId = getBlockId(x + i, y + j);
                    Block block = occupiedBlockMap.get(distanceBlockId);
                    if (block == null && unoccupiedBlockSet.contains(distanceBlockId)) {
                        blockList.add(distanceBlockId);
                    }
                }
            }
        }

        for (int j = -distance ; j <= distance; j += distance * 2) {
            for (int i = -distance + 1; i <= distance - 1; i++) {
                if (isInBound(x + i, y + j)) {
                    int distanceBlockId = getBlockId(x + i, y + j);
                    Block block = occupiedBlockMap.get(distanceBlockId);
                    if (block == null && unoccupiedBlockSet.contains(distanceBlockId)) {
                        blockList.add(distanceBlockId);
                    }
                }
            }
        }
        return blockList;
    }

    public TerrainType getTerrainType(Block block) {
        BlockGroup blockGroup = blockGroupMap.get(block.blockGroupId);
        if (blockGroup == null) return TerrainType.PAVEMENT;
        else return blockGroup.getTerrainType();
    }

    /**
     * 获取与给定点临近的block group的起点或者终点
     */
    public int getNearestEndPoint(Point point, BlockGroup blockGroup) {
        int distance1 = point.distance(getX(blockGroup.getStartBlockId()) * 1000, 0, getY(blockGroup.getStartBlockId()) * 1000);
        int distance2 = point.distance(getX(blockGroup.getEndBlockId()) * 1000, 0, getY(blockGroup.getEndBlockId()) * 1000);

        if (distance1 < distance2) return blockGroup.getStartBlockId();
        return blockGroup.getEndBlockId();
    }

    public int getNearestArteryBlockId(int blockId, int distance) {
        for (int d = 1; d <= distance; d ++) {
            List<Block> blockList = getBlocksInDistance(
                    blockId, d, BlockType.MAIN_ROAD, BlockType.SHORTCUT);
            if (blockList.size() > 0) return blockList.get(0).blockId;
        }
        return -1;
    }

    public BlockGroup getBlockGroup(short id) {
        return blockGroupMap.get(id);
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

    private Direction getDirection(int blockIdA, int blockIdB) {
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

        byteBuilder.append(blockGroupMap.size());
        for (BlockGroup blockGroup : blockGroupMap.values()) {
            byteBuilder.append(blockGroup.getBytes());
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

    public boolean isRoadAlongX(int x, int y)
    {
        int aroundBlockId = getBlockId(x + expandWidth + 1, y);
        if (isOccupied(aroundBlockId)) return false;

        aroundBlockId = getBlockId(x - expandWidth - 1, y);
        if (isOccupied(aroundBlockId)) return false;

        return true;
    }

    public int getUnitWidth() {
        return bridgeWidth + 1;
    }

    public Block getSingleTerrainBlock(SegmentOld segment) {
        int blockIndex = (segment.distance() / getUnitWidth() - 1) * getUnitWidth() + (getUnitWidth() / 2) ;
        return segment.blockList.get(blockIndex);
    }

    /**
     * 获取从a点到b点最短路径的方向
     */
    public Direction getPointsDirection(int startBlockId, int endBlockId) {
        if (!isCrossPoint(startBlockId) || !isCrossPoint(endBlockId)) return null;

        List<Integer> path = getPath(startBlockId, endBlockId);
        if (path == null || path.size() == 0) return null;

        int firstWayPointId = path.get(1);
        SegmentOld segment = getSegment(startBlockId, firstWayPointId);
        if (segment == null) return null;

        int nextBlockId;
        if (startBlockId == segment.head.blockId) {
            nextBlockId = segment.blockList.get(0).blockId;
        } else {
            nextBlockId = segment.blockList.get(segment.blockList.size() - 1).blockId;
        }

        /*  for test
        Direction direction = getDirection(startBlockId, nextBlockId);
        if (direction == Direction.DOWN_LEFT || direction == Direction.DOWN_RIGHT ||
                direction == Direction.UP_LEFT || direction == Direction.UP_RIGHT) {
            System.out.println("block id:" + startBlockId + ", x:" + getX(startBlockId) + ", y:" + getY(startBlockId));
            System.out.println("block id:" + nextBlockId + ", x:" + getX(nextBlockId) + ", y:" + getY(nextBlockId));

            int a =  1;
        }
        */
        return getDirection(startBlockId, nextBlockId);
    }

    /**
     * 根据起点和终点获取segment
     */
    private SegmentOld getSegment(int startBlockId, int endBlockId) {
        SegmentOld chosenSegment = null;
        int size = Integer.MAX_VALUE;
        for  (SegmentOld segment: segmentList) {
            if ((segment.head.blockId == startBlockId && segment.tail.blockId == endBlockId) ||
                    (segment.head.blockId == endBlockId && segment.tail.blockId == startBlockId) ) {
                if (segment.distance() < size) {
                    chosenSegment = segment;
                    size = segment.distance();
                }
            }
        }
        return chosenSegment;
    }

    /**
     * 获取从节点a到节点b的所有crossPoint的list集合
     */
    private List<Integer> getPath(int startBlockId, int endBlockId) {
        Map<Integer, List<Integer>> pointPath = pathMap.get(startBlockId);
        if (pointPath != null)  return pointPath.get(endBlockId);
        return null;
    }

    /**
     * 判断是否为交叉点
     */
    public boolean isCrossPoint(int blockId) {
        return crossBlockIdSet.contains(blockId);
    }

    public void countPathInfo() {
        crossBlockIdSet.clear();
        for (SegmentOld segment: segmentList) {
            crossBlockIdSet.add(segment.head.blockId);
            crossBlockIdSet.add(segment.tail.blockId);
        }

        int[] blockIds = new int[crossBlockIdSet.size()];
        int index = 0;
        for (int crossBlockId: crossBlockIdSet) {
            blockIds[index ++] = crossBlockId;
        }

        Graph graph = new Graph(blockIds);

        for (SegmentOld segment : segmentList) {
            graph.setEdge(segment.head.blockId, segment.tail.blockId, segment.blockList.size() + 1);
        }

        pathMap = graph.countPath();
        System.out.println(pathMap);
        for (SegmentOld segment : segmentList) {
            int detourDistance = graph.countDetourDistance(segment.head.blockId, segment.tail.blockId);
            segment.detourDistance = detourDistance;
        }

        SegmentOld[] segments = new SegmentOld[segmentList.size()];
        int i = 0;
        for (SegmentOld segment : segmentList) {
            int m = i;
            for (int j = 0; j < i; j ++) {
                if (segment.blockList.size() >= segments[j].blockList.size()) continue;
                else {
                    m = j;
                    break;
                }
            }
            for (int k = i; k > m; k --) {
                segments[k] = segments[k - 1];
            }
            segments[m] = segment;
            i ++;
        }

        for (SegmentOld segment : segments) {
            System.out.println("segment: " +
                    "\t" + segment.head.blockId + "[" + segment.head.position.x + "," + segment.head.position.y + "], " +
                    "\t" + segment.tail.blockId + "[" + segment.tail.position.x + "," + segment.tail.position.y + "], " +
                    "\tdistance: " + segment.distance() + ", \tdetour distance: " + segment.detourDistance);
        }

        for (int blockId: crossBlockIdSet) {
            Direction direction = getPointsDirection(blockId, endBlockId);
            System.out.println("block id:" + blockId + ", x:" + getX(blockId) + ", y:" + getY(blockId) + ", direction: " + direction);
        }
    }

    public Point getRoadSidePoint(int blockId) {
        int minDistance = Integer.MAX_VALUE;
        Map<Integer, List<Block>> aroundBlocks = getBlocksInDistances(blockId, expandWidth * 2, BlockType.ROAD_EXTENSION);
        return null;
    }
}

package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;

import static com.prosper.chasing.game.util.Enums.*;

import com.prosper.chasing.game.util.Util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2018/4/28.
 */
public class GameMap {

    public static Map<BuildingType, Byte> buildingBlockCount = new HashMap<>();

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
     * 使用过的地块
     */
    public Map<Integer, Block> occupiedBlockMap;

    public List<Integer> unoccupiedBlockList;

    public GameMap(int boundX, int boundY, int bridgeWidth) {
        this.boundX = boundX;
        this.boundY = boundY;
        this.bridgeWidth = bridgeWidth;

        resourceMap = new HashMap<>();
        occupiedBlockMap = new HashMap<>();
        unoccupiedBlockList = new ArrayList<>();
        buildingMap = new HashMap<>();

        for (int x = 0; x < boundX; x ++) {
            for (int y = 0; y < boundY; y ++) {
                int blockId = getBlockId(x, y);
                if (!isOccupied(blockId)) {
                    unoccupiedBlockList.add(blockId);
                }
            }
        }
    }

    public void setMainroad(Branch mainRoad) {
        this.mainRoad = mainRoad;
        generateSingleRouteInfo();
    }

    public void setShortcutList(List<Branch> shortcutList) {
        this.shortcutList = shortcutList;
    }

    /*

    public RoadMap(int boundX, int boundY, Branch mainRoad, List<Branch> shortcutList) {
        this.boundX = boundX;
        this.boundY = boundY;

        this.mainRoad = mainRoad;
        this.shortcutList = shortcutList;

        generateSingleRouteInfo();
        resourceMap = new HashMap<>();

        occupiedBlockMap = new HashMap<>();
        Block current = mainRoad.blockList.get(0);
        while (current != null) {
            occupiedBlockMap.put(current.getBlockId(), current);
            current = current.next;
        }

        for (Branch shortcut: shortcutList) {
            if (shortcut.blockList.size() == 0) break;
            current = shortcut.blockList.get(0);
            while (current != null) {
                occupiedBlockMap.put(current.getBlockId(), current);
                current = current.next;
            }
        }

        unoccupiedBlockList = new ArrayList<>();
        for (int x = 0; x < boundX; x ++) {
            for (int y = 0; y < boundY; y ++) {
                int blockId = getBlockId(x, y);
                if (!isOccupied(blockId)) {
                    unoccupiedBlockList.add(blockId);
                }
            }
        }
    }
    */

    public void generateSingleRouteInfo() {
        Block block = mainRoad.blockList.get(mainRoad.blockList.size() - 1);
        int distance = 0;

        while (block != null) {
            block.distanceToFinish = distance ++;
            block = block.previous;
        }
    }

    public Block addBlock(int blockId, BlockType type, TerrainType terrainType) {
        return addBlock(blockId, type, terrainType, RoadDirection.NONE);
    }

    public Block addBlock(int blockId, BlockType type, TerrainType terrainType, RoadDirection roadDirection) {
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
        occupiedBlockMap.put(blockId, block);
        unoccupiedBlockList.remove(new Integer(blockId));
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

    public int getRandomUnoccupiedBlockId() {
        return unoccupiedBlockList.get(ThreadLocalRandom.current().nextInt(unoccupiedBlockList.size()));
    }

    public int getRandomMainRoadBlockId() {
        return mainRoad.blockList.get(ThreadLocalRandom.current().nextInt(mainRoad.blockList.size())).blockId;
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

            if (block.type == BlockType.MAIN_ROAD) terrainBytes[x][y] = 'M';
            else if (block.type == BlockType.MOUNTAIN_L1) terrainBytes[x][y] = 'h';
            else if (block.type == BlockType.MOUNTAIN_L2) terrainBytes[x][y] = 'H';
            else if (block.type == BlockType.MOUNTAIN_L3) terrainBytes[x][y] = 'w';
            else if (block.type == BlockType.MOUNTAIN_L4) terrainBytes[x][y] = 'W';
            else if (block.type == BlockType.MOUNTAIN_L5) terrainBytes[x][y] = 'x';
            else if (block.type == BlockType.MOUNTAIN_L6) terrainBytes[x][y] = 'X';
            else if (block.type == BlockType.SLOPE) terrainBytes[x][y] = 'Y';
            else if (block.type == BlockType.SLASH_ROAD_MOUNTAIN_L1 ||
                    block.type == BlockType.SLASH_MOUNTAIN_L1_ROAD ||
                    block.type == BlockType.SLASH_MOUNTAIN_L1_MOUNTAIN_L2 ||
                    block.type == BlockType.SLASH_MOUNTAIN_L2_MOUNTAIN_L1 ||
                    block.type == BlockType.SLASH_MOUNTAIN_L2_MOUNTAIN_L3 ||
                    block.type == BlockType.SLASH_MOUNTAIN_L3_MOUNTAIN_L2
                    ) terrainBytes[x][y] = '\\';
            else if (block.type == BlockType.BACKSLASH_ROAD_MOUNTAIN_L1 ||
                    block.type == BlockType.BACKSLASH_MOUNTAIN_L1_ROAD ||
                    block.type == BlockType.BACKSLASH_MOUNTAIN_L1_MOUNTAIN_L2 ||
                    block.type == BlockType.BACKSLASH_MOUNTAIN_L2_MOUNTAIN_L1 ||
                    block.type == BlockType.BACKSLASH_MOUNTAIN_L2_MOUNTAIN_L3 ||
                    block.type == BlockType.BACKSLASH_MOUNTAIN_L3_MOUNTAIN_L2
                    ) terrainBytes[x][y] = '/';

            if (block.terrainType == TerrainType.ANIMAL) terrainBytes[x][y] = 'A';
            else if (block.terrainType == TerrainType.BLANK) terrainBytes[x][y] = 'B';
            else if (block.terrainType == TerrainType.FOG) terrainBytes[x][y] = 'C';
            else if (block.terrainType == TerrainType.FOREST) terrainBytes[x][y] = 'D';
            else if (block.terrainType == TerrainType.GRASS) terrainBytes[x][y] = 'E';
            else if (block.terrainType == TerrainType.LAVA) terrainBytes[x][y] = 'F';
            else if (block.terrainType == TerrainType.RAIN) terrainBytes[x][y] = 'G';
            else if (block.terrainType == TerrainType.PAVEMENT) terrainBytes[x][y] = 'V';
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
            else if (block.terrainType == TerrainType.MOUNTAIN_L1) terrainBytes[x][y] = 'V';
            else if (block.terrainType == TerrainType.MOUNTAIN_L2) terrainBytes[x][y] = 'W';
            else if (block.terrainType == TerrainType.MOUNTAIN_L3) terrainBytes[x][y] = 'X';
            //else terrainBytes[x][y] = 'Z';
        }

        for (Building building: buildingMap.values()) {
            int x = building.point2D.x;
            int y = building.point2D.y;

            if (building.buildingType == BuildingType.WALL) terrainBytes[x][y] = '@';
            if (building.buildingType == BuildingType.TOWER_A) terrainBytes[x][y] = '#';
            if (building.buildingType == BuildingType.TOWER_B) terrainBytes[x][y] = '$';
            if (building.buildingType == BuildingType.GATE) terrainBytes[x][y] = '+';
            if (building.buildingType == BuildingType.CASTLE) terrainBytes[x][y] = '%';
            if (building.buildingType == BuildingType.ROTUNDA) terrainBytes[x][y] = ':';
            if (building.buildingType == BuildingType.GRAVEYARD) terrainBytes[x][y] = '^';
            if (building.buildingType == BuildingType.JACKSTRAW) terrainBytes[x][y] = '&';
            if (building.buildingType == BuildingType.STORE) terrainBytes[x][y] = '*';
            if (building.buildingType == BuildingType.TALL_TREE) terrainBytes[x][y] = '<';
            if (building.buildingType == BuildingType.WAREHOUSE) terrainBytes[x][y] = '>';
            if (building.buildingType == BuildingType.WELL) terrainBytes[x][y] = '?';
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
                addBlock(blockId, BlockType.BRANCH, TerrainType.PAVEMENT, RoadDirection.NONE);
            }
        } else if (ax < bx) {
            for (int i = ax + 1; i < bx; i ++) {
                int blockId = getBlockId(i, ay);
                if (isOccupied(blockId)) throw new RuntimeException("is occupied");
                addBlock(blockId, BlockType.BRANCH, TerrainType.PAVEMENT, RoadDirection.NONE);
            }
        } else if (ay > by) {
            for (int i = by + 1; i < ay; i ++) {
                int blockId = getBlockId(ax, i);
                if (isOccupied(blockId)) throw new RuntimeException("is occupied");
                addBlock(blockId, BlockType.BRANCH, TerrainType.PAVEMENT, RoadDirection.NONE);
            }
        } else if (ay < by) {
            for (int i = ay + 1; i < by; i ++) {
                int blockId = getBlockId(ax, i);
                if (isOccupied(blockId)) throw new RuntimeException("is occupied");
                addBlock(blockId, BlockType.BRANCH, TerrainType.PAVEMENT, RoadDirection.NONE);
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
            printMap();
            if (blockList.get(0).position.x == x) {
                return RoadDirection.VERTICAL_END;
            } else if (blockList.get(0).position.y == y) {
                return RoadDirection.HORIZONTAL_END;
            }
        }
        return RoadDirection.STAND_ALONE;
    }

    public void addBuilding(BuildingType buildingType, int x, int y, Orientation orientation) {
        int id = getBlockId(x, y);
        Building building = new Building(id, buildingType, new Point2D(x, y), orientation);
        buildingMap.put(id, building);
    }

    public List<Block> getAdjacent(int blockId, int distance, boolean corner, BlockType... blockTypes) {
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

        return Direction.SELF;
    }
}

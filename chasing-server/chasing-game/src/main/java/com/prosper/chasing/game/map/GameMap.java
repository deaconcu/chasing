package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;
import com.prosper.chasing.game.util.Constant;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2018/4/28.
 */
public class GameMap {

    public static Map<Byte, Byte> buildingBlockCount = new HashMap<>();

    static {
        buildingBlockCount.put(Constant.BuildingType.WAREHOUSE, (byte) 4);
        buildingBlockCount.put(Constant.BuildingType.STORE, (byte) 8);
        buildingBlockCount.put(Constant.BuildingType.WELL, (byte) 8);
        buildingBlockCount.put(Constant.BuildingType.TALL_TREE, (byte) 4);
        buildingBlockCount.put(Constant.BuildingType.GRAVEYARD, (byte) 4);
        buildingBlockCount.put(Constant.BuildingType.JACKSTRAW, (byte) 4);
    }

    public int boundX;

    public int boundY;

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
    public Map<Integer, Byte> resourceMap;

    /**
     * 建筑物
     */
    public Map<Integer, Building> buildingMap;

    /**
     * 使用过的地块
     */
    public Map<Integer, Block> occupiedBlockMap;

    public List<Integer> unoccupiedBlockList;

    public GameMap(int boundX, int boundY) {
        this.boundX = boundX;
        this.boundY = boundY;

        resourceMap = new HashMap<>();
        occupiedBlockMap = new HashMap<>();
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

    public void setMainroad(Branch mainRoad) {
        this.mainRoad = mainRoad;
        generateSingleRouteInfo();
    }

    public void setShortcutList(List<Branch> shortcutList) {
        this.shortcutList = shortcutList;
    }

    /*

    public GameMap(int boundX, int boundY, Branch mainRoad, List<Branch> shortcutList) {
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

    public Block addBlock(int blockId, byte type, byte firstTerrainTypeId, byte secondTerrainTypeId) {
        if (isOccupied(blockId)) {
            return occupiedBlockMap.get(blockId);
        }
        if (firstTerrainTypeId == Constant.TerrainType.BUILDING) {
            //buildingMap.put(blockId, secondTerrainTypeId);
        } else if (firstTerrainTypeId == Constant.TerrainType.VIEW) {
            resourceMap.put(blockId, firstTerrainTypeId);
        } else {
            resourceMap.put(blockId, firstTerrainTypeId);
        }

        Block block = new Block(new Point2D(getX(blockId), getY(blockId)), blockId, type, firstTerrainTypeId);
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

            if (block.type == Constant.MapBlockType.MAIN_ROAD) terrainBytes[x][y] = 'M';
            else if (block.type == Constant.MapBlockType.SHORTCUT) terrainBytes[x][y] = 'Y';

            if (block.terrainType == Constant.TerrainType.ANIMAL) terrainBytes[x][y] = 'A';
            else if (block.terrainType == Constant.TerrainType.BLANK) terrainBytes[x][y] = 'B';
            else if (block.terrainType == Constant.TerrainType.FOG) terrainBytes[x][y] = 'C';
            else if (block.terrainType == Constant.TerrainType.FOREST) terrainBytes[x][y] = 'D';
            else if (block.terrainType == Constant.TerrainType.GRASS) terrainBytes[x][y] = 'E';
            else if (block.terrainType == Constant.TerrainType.LAVA) terrainBytes[x][y] = 'F';
            else if (block.terrainType == Constant.TerrainType.RAIN) terrainBytes[x][y] = 'G';
            else if (block.terrainType == Constant.TerrainType.ROAD) terrainBytes[x][y] = 'H';
            else if (block.terrainType == Constant.TerrainType.ROCK) terrainBytes[x][y] = 'I';
            else if (block.terrainType == Constant.TerrainType.SAND) terrainBytes[x][y] = 'J';
            else if (block.terrainType == Constant.TerrainType.SNOW) terrainBytes[x][y] = 'K';
            else if (block.terrainType == Constant.TerrainType.SWAMP) terrainBytes[x][y] = 'L';
            else if (block.terrainType == Constant.TerrainType.VEGETABLE) terrainBytes[x][y] = 'N';
            else if (block.terrainType == Constant.TerrainType.WATER) terrainBytes[x][y] = 'O';
            else if (block.terrainType == Constant.TerrainType.WIND) terrainBytes[x][y] = 'P';
            else if (block.terrainType == Constant.TerrainType.WILDWIND) terrainBytes[x][y] = 'Q';
            else if (block.terrainType == Constant.TerrainType.WHEAT) terrainBytes[x][y] = 'R';
            else if (block.terrainType == Constant.TerrainType.VIEW) terrainBytes[x][y] = 'S';
            else if (block.terrainType == Constant.TerrainType.BUILDING) terrainBytes[x][y] = 'T';
            else terrainBytes[x][y] = 'U';
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

    public List<Integer> getBorderPoints(int blockId, int width, int height) {
        if (width <= 0 || height <= 0) return null;
        int x  = getX(blockId);
        int y = getY(blockId);

        if (!isInBound(x, y)) return null;
        if (!isInBound(x + width, y)) return null;
        if (!isInBound(x, y + width)) return null;
        if (!isInBound(x + width, y + width)) return null;

        List<Integer> borderPointList = new LinkedList<>();
        for (int i = 0; i <=width; i ++) {
            borderPointList.add(getBlockId(x + i, y));
        }
        for (int i = 1; i <=height; i ++) {
            borderPointList.add(getBlockId(x, y + i));
        }
        for (int i = 1; i <= width; i ++) {
            borderPointList.add(getBlockId(x + i, y + height));
        }
        for (int i = 1; i < height; i ++) {
            borderPointList.add(getBlockId(x + width, y + i));
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
                addBlock(blockId, Constant.MapBlockType.BRANCH, Constant.TerrainType.ROAD, (byte)0);
            }
        } else if (ax < bx) {
            for (int i = ax + 1; i < bx; i ++) {
                int blockId = getBlockId(i, ay);
                if (isOccupied(blockId)) throw new RuntimeException("is occupied");
                addBlock(blockId, Constant.MapBlockType.BRANCH, Constant.TerrainType.ROAD, (byte)0);
            }
        } else if (ay > by) {
            for (int i = by + 1; i < ay; i ++) {
                int blockId = getBlockId(ax, i);
                if (isOccupied(blockId)) throw new RuntimeException("is occupied");
                addBlock(blockId, Constant.MapBlockType.BRANCH, Constant.TerrainType.ROAD, (byte)0);
            }
        } else if (ay < by) {
            for (int i = ay + 1; i < by; i ++) {
                int blockId = getBlockId(ax, i);
                if (isOccupied(blockId)) throw new RuntimeException("is occupied");
                addBlock(blockId, Constant.MapBlockType.BRANCH, Constant.TerrainType.ROAD, (byte)0);
            }
        }
    }
}

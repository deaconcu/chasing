package com.prosper.chasing.game.old;

import com.prosper.chasing.game.base.Point2D;
import com.prosper.chasing.game.util.Constant;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2018/4/22.
 */
public class GameMap {

    public static class BlockAreaConfig {
        public byte minWidth;
        public byte maxWidth;
        public byte minHeight;
        public byte maxHeight;

        public BlockAreaConfig(byte minWidth, byte maxWidth, byte minHeight, byte maxHeight) {
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }
    }

    public static class BlockArea {
        public short id;
        public byte width;
        public byte height;
        Point2D leftBottomPoint;

        @Override
        public String toString() {
            return "id:" + id + ", width:" + width + ", height:" + height + ", position:[" + leftBottomPoint + "]";
        }

    }

    public static class Block {
        public Point2D position;
        public int blockId;
        public byte type; // 0:wall 1:block area 2: road

        public Block(Point2D position, int blockId, byte type) {
            this.position = position;
            this.blockId = blockId;
            this.type = type;
        }
    }

    public Map<Integer, Block> terrainBlockMap;

    protected Set<Integer> wallSet;

    protected Set<Integer> unexploredRoadSet;

    protected Set<Integer> unusedBlockSet;

    protected Map<Short, BlockArea> terrainBlockAreaMap;

    public Point2D start;

    public Point2D end;

    public int boundX;

    public int boundY;

    public GameMap(int boundX, int boundY) {
        terrainBlockMap = new HashMap<>();
        terrainBlockAreaMap = new HashMap<>();

        this.boundX = boundX;
        this.boundY = boundY;

        unusedBlockSet = new HashSet<>();
        for (int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                unusedBlockSet.add(getBlockId(i, j));
            }
        }

        generate();
    }

    protected BlockArea randomBlockArea(byte minWidth, byte maxWidth, byte minHeight, byte maxHeight) {
        BlockArea blockArea = new BlockArea();
        blockArea.width = (byte)ThreadLocalRandom.current().nextInt(minWidth, maxWidth + 1);
        blockArea.height = (byte)ThreadLocalRandom.current().nextInt(minHeight, maxHeight + 1);

        blockArea.leftBottomPoint = new Point2D(
                ThreadLocalRandom.current().nextInt(boundX + 1 - blockArea.width),
                ThreadLocalRandom.current().nextInt(boundY + 1 - blockArea.height)
        );
        return blockArea;
    }

    protected int randomUnusedPoint() {
        int choise = ThreadLocalRandom.current().nextInt(unusedBlockSet.size());
        for (int blockId: unusedBlockSet) {
            if (choise -- == 0) {
                return blockId;
            }
        }
        return -1;
    }

    protected int getDistance(int blockIdA, int blockIdB) {
        return (int) Math.hypot(
                getXByBlockId(blockIdA) - getXByBlockId(blockIdB), getYByBlockId(blockIdA) - getYByBlockId(blockIdB));
    }

    protected void generateLine(int startBlockId, int endBlockId) {

    }

    protected void putTerrainBlockArea (BlockArea blockArea) {
        for (int i = blockArea.leftBottomPoint.x; i < blockArea.leftBottomPoint.x + blockArea.width; i ++) {
            for (int j = blockArea.leftBottomPoint.y; j < blockArea.leftBottomPoint.y + blockArea.height; j ++) {
                if(terrainBlockMap.containsKey(getBlockId(i, j))){
                    //System.out.println("block rejected: " + blockArea);
                    return;
                }
            }
        }

        blockArea.id = (short) (terrainBlockAreaMap.size() + 1);
        terrainBlockAreaMap.put(blockArea.id, blockArea);
        //System.out.println("block accepted: " + blockArea);

        for (int i = blockArea.leftBottomPoint.x; i < blockArea.leftBottomPoint.x + blockArea.width; i ++) {
            for (int j = blockArea.leftBottomPoint.y; j < blockArea.leftBottomPoint.y + blockArea.height; j ++) {
                addBlock(i, j, Constant.MapBlockType.BLOCK_AREA);
            }
        }

        int areaWallX = blockArea.leftBottomPoint.x - 1;
        int areaWallY = blockArea.leftBottomPoint.y - 1;

        do {
            if (isInBound(areaWallX, areaWallY)) {
                addBlock(areaWallX, areaWallY, Constant.MapBlockType.WALL);
            }

            if (areaWallX < blockArea.leftBottomPoint.x + blockArea.width &&
                    areaWallY == blockArea.leftBottomPoint.y - 1) {
                areaWallX ++;
            } else if (areaWallX == blockArea.leftBottomPoint.x + blockArea.width &&
                    areaWallY < blockArea.leftBottomPoint.y + blockArea.height) {
                areaWallY ++;
            } else if (areaWallY == blockArea.leftBottomPoint.y + blockArea.height &&
                    areaWallX > blockArea.leftBottomPoint.x - 1) {
                areaWallX --;
            } else {
                areaWallY --;
            }
        } while(areaWallX != blockArea.leftBottomPoint.x - 1 || areaWallY != blockArea.leftBottomPoint.y - 1);
    }

    protected int getRandomUnusedSibling(int x, int y) {
        int[] validSiblings = new int[4];
        int validSiblingCount = 0;
        if (isInBound(x, y + 1) && unusedBlockSet.contains(getBlockId(x, y + 1)))
            validSiblings[validSiblingCount ++] = getBlockId(x, y + 1);
        if (isInBound(x + 1, y) && unusedBlockSet.contains(getBlockId(x + 1, y)))
            validSiblings[validSiblingCount ++] = getBlockId(x + 1, y);
        if (isInBound(x, y - 1) && unusedBlockSet.contains(getBlockId(x, y - 1)))
            validSiblings[validSiblingCount ++] = getBlockId(x, y - 1);
        if (isInBound(x - 1, y) && unusedBlockSet.contains(getBlockId(x - 1, y)))
            validSiblings[validSiblingCount ++] = getBlockId(x - 1, y);

        if (validSiblingCount == 0) return -1;
        else return validSiblings[ThreadLocalRandom.current().nextInt(validSiblingCount)];
    }

    protected void makeWall(int x, int y, boolean isX) {
        if (isX) {
            for (int i = -2; i < 3; i ++) {
                if (!isInBound(x, y + i)) continue;
                if (unusedBlockSet.contains(getBlockId(x, y + i))) {
                    addBlock(x, y + i, Constant.MapBlockType.WALL);
                }
            }
        } else {
            for (int i = -2; i < 3; i ++) {
                if (!isInBound(x + i, y)) continue;
                if (unusedBlockSet.contains(getBlockId(x + i, y))) {
                    addBlock(x + i, y, Constant.MapBlockType.WALL);
                }
            }
        }
    }

    protected void makeRoad(int startPointId, int endPointId) {
        if (!unusedBlockSet.contains(startPointId) || !unusedBlockSet.contains(endPointId)) return;

        LinkedList<Integer> pathStack = new LinkedList<Integer>();
        Map<Integer, Integer> usedDirection = new HashMap<>();

        int currentBlockId = startPointId;
        while (currentBlockId != -1) {
            int x = getXByBlockId(currentBlockId);
            int y = getYByBlockId(currentBlockId);


            if (isInBound(x, y + 1) && terrainBlockMap.containsKey(getBlockId(x, y + 1))
                    && terrainBlockMap.get(getBlockId(x, y + 1)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x, y + 1, false);
            if (isInBound(x + 1, y) && terrainBlockMap.containsKey(getBlockId(x + 1, y))
                    && terrainBlockMap.get(getBlockId(x + 1, y)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x + 1, y, true);
            if (isInBound(x, y - 1) && terrainBlockMap.containsKey(getBlockId(x, y - 1))
                    && terrainBlockMap.get(getBlockId(x, y - 1)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x, y - 1, false);
            if (isInBound(x - 1, y) && terrainBlockMap.containsKey(getBlockId(x - 1, y))
                    && terrainBlockMap.get(getBlockId(x - 1, y)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x - 1, y, true);

            currentBlockId = getRandomUnusedSibling(x, y);
        }

    }

    protected void makeRoad(int blockId) {
        if (!unusedBlockSet.contains(blockId)) return;

        while (blockId != -1) {
            int x = getXByBlockId(blockId);
            int y = getYByBlockId(blockId);
            addBlock(x, y, Constant.MapBlockType.MAIN_ROAD);


            if (isInBound(x, y + 1) && terrainBlockMap.containsKey(getBlockId(x, y + 1))
                    && terrainBlockMap.get(getBlockId(x, y + 1)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x, y + 1, false);
            if (isInBound(x + 1, y) && terrainBlockMap.containsKey(getBlockId(x + 1, y))
                    && terrainBlockMap.get(getBlockId(x + 1, y)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x + 1, y, true);
            if (isInBound(x, y - 1) && terrainBlockMap.containsKey(getBlockId(x, y - 1))
                    && terrainBlockMap.get(getBlockId(x, y - 1)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x, y - 1, false);
            if (isInBound(x - 1, y) && terrainBlockMap.containsKey(getBlockId(x - 1, y))
                    && terrainBlockMap.get(getBlockId(x - 1, y)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x - 1, y, true);

            blockId = getRandomUnusedSibling(x, y);
        }
    }

    protected void addBlock(int x, int y, byte type) {
        int blockId = getBlockId(x, y);
        terrainBlockMap.put(blockId, new Block(new Point2D(x, y), blockId, type));
        unusedBlockSet.remove(blockId);

        try {
            if (type == Constant.MapBlockType.MAIN_ROAD) {
                Thread.sleep(100);
                printTerrainBlocks();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //System.out.println("add block to map, block: [x:" + x + ", y:" + y + ", type: " + type + "]");
    }

    protected int getBlockId(int x, int y) {
        return boundX * y + x + 1;
    }

    protected int getXByBlockId(int id) {
        return (id - 1) % boundX;
    }

    protected int getYByBlockId(int id) {
        return (id - 1) / boundX;
    }

    private boolean isInBound(int x, int y) {
        if (x < 0 || x >= boundX) return false;
        if (y < 0 || y >= boundY) return false;
        return true;
    }

    public void printTerrainBlocks() {
        char[][] terrainBytes = new char[boundX][boundY];
        for(int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                terrainBytes[i][j] = ' ';
            }
        }

        for (Block block: terrainBlockMap.values()) {
            if (block.position.x >= 100 || block.position.y >= 100) {
                System.out.println("error: [x:" + block.position.x + ", y:" + block.position.y + ", type: " + block.type + "]");
            }
            if (block.position.x < 0 || block.position.y < 0) {
                System.out.println("error: [x:" + block.position.x + ", y:" + block.position.y + ", type: " + block.type + "]");
            }
            if (block.type == Constant.MapBlockType.MAIN_ROAD) {
                terrainBytes[block.position.x][block.position.y] = 'z';
            } else if (block.type == Constant.MapBlockType.BLOCK_AREA){
                terrainBytes[block.position.x][block.position.y] = 'X';
            } else {
                terrainBytes[block.position.x][block.position.y] = ' ';
            }
        }

        for(int i = 0; i < boundX; i ++) {
            printLineNo(i);
            for (int j = 0; j < boundY; j ++) {
                System.out.print(terrainBytes[i][j]);
                System.out.print(" ");
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

    public void generate() {

    }


}

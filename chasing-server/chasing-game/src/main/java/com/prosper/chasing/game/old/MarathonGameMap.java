package com.prosper.chasing.game.old;

import com.prosper.chasing.game.util.Constant;

import java.util.List;
import java.util.Map;

/**
 * Created by deacon on 2018/4/22.
 */
public class MarathonGameMap extends GameMapBase {

    MarathonGameMap(int boundX, int boundY) {
        super(boundX, boundY);
    }

    @Override
    public void generate() {
        generate100();
    }

    public void generate100Version2() {
        int startBlockId = 304;
        int endBlockId = 9697;

        clear();
        makeRoad();
        removeBrandRoad(startBlockId, endBlockId);
        int length = getRoadLength(startBlockId, endBlockId);
        System.out.println("path length: " + length);

        markDistance(startBlockId, endBlockId);
        printTerrainBlocks();
        generateBranches();
    }

    public void generate30() {
        int startBlockId = 70;
        int endBlockId = 415;

        int length = 0;
        while(length < 100) {
            clear();
            makeRoad();
            removeBrandRoad(startBlockId, endBlockId);
            length = getRoadLength(startBlockId, endBlockId);
            System.out.println("path length: " + length);
        }

        printTerrainBlocks();
        Map<Integer, Block> blockMapA = usedBlockMap;

        length = 0;
        while(length < 100) {
            clear();
            makeRoad();
            removeBrandRoad(startBlockId, endBlockId);
            length = getRoadLength(startBlockId, endBlockId);
            System.out.println("path length: " + length);
        }

        printTerrainBlocks();

        for (Block block: blockMapA.values()) {
            if (block.type == Constant.MapBlockType.MAIN_ROAD) {
                addBlock(block.blockId, Constant.MapBlockType.MAIN_ROAD);
            } else {
                addBlock(block.blockId, block.type);
            }
        }
    }

    public void generate100() {
        int startBlockId = 304;
        int endBlockId = 9697;

        int length = 0;
        while(length < 800) {
            clear();
            makeRoad();
            removeBrandRoad(startBlockId, endBlockId);
            length = getRoadLength(startBlockId, endBlockId);
            System.out.println("path length: " + length);
        }

        Map<Integer, Block> blockMapA = usedBlockMap;

        length = 0;
        while(length < 800) {
            clear();
            makeRoad();
            removeBrandRoad(startBlockId, endBlockId);
            length = getRoadLength(startBlockId, endBlockId);
            System.out.println("path length: " + length);
        }

        Map<Integer, Block> blockMapB = usedBlockMap;

        length = 0;
        while(length < 800) {
            clear();
            makeRoad();
            removeBrandRoad(startBlockId, endBlockId);
            length = getRoadLength(startBlockId, endBlockId);
            System.out.println("path length: " + length);
        }

        markDistance(startBlockId, endBlockId);

        for (Block block: blockMapA.values()) {
            if (block.type == Constant.MapBlockType.MAIN_ROAD) {
                addBlock(block.blockId, Constant.MapBlockType.MAIN_ROAD);
            } else {
                addBlock(block.blockId, block.type);
            }
        }

        for (Block block: blockMapB.values()) {
            if (block.type == Constant.MapBlockType.MAIN_ROAD) {
                addBlock(block.blockId, Constant.MapBlockType.MAIN_ROAD);
            } else {
                addBlock(block.blockId, block.type);
            }
        }
    }


    private void generateLines(int startBlockId, int endBlockId) {
        generateLine(startBlockId, endBlockId);
    }

    private void generateLines() {
        while(unusedBlockSet.size() > 0) {
            int blockId = unusedBlockSet.iterator().next();
            makeRoad(blockId);
        }
    }

    private void generateBlockAreas(List<Integer> forbidBlockIdList) {
        for (int i = 0; i < 1000; i ++) {
            //System.out.println("generating block area, times: " + i);
            putTerrainBlockArea(randomBlockArea((byte)3, (byte)3, (byte)3, (byte)3), forbidBlockIdList);
            if ((float)areaBlockCount / (float)(boundX * boundY) > 0) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        //MarathonGameMapCreator marathonGameMap = new MarathonGameMapCreator(22, 22);
        MarathonGameMap marathonGameMap = new MarathonGameMap(100, 100);
        marathonGameMap.printTerrainBlocks();
    }
}

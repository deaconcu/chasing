package com.prosper.chasing.game.map;

import com.prosper.chasing.common.util.Pair;
import com.prosper.chasing.game.base.Point;
import com.prosper.chasing.game.util.Util;

import static com.prosper.chasing.game.util.Enums.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2018/4/25.
 */
public class MarathonGameMapCreator {



    /**
     * 主路上需要增加的地形长度比例
     */
    public static float ROAD_EXTEND_PERCENT = 0.3f;

    /**
     * 主路上特殊地形的最小间隔
     */
    public static int ROAD_TERRAIN_GAP = 30;

    /**
     * 景观的比例
     */
    public static float VIEW_RATE = 0.05f;

    /**
     * 地形配置
     */
    private static Map<TerrainType, TerrainConfig> terrainConfigMap = new HashMap<>();

    /**
     * 建筑物配置
     */
    private static Map<BuildingType, Byte> buildingConfigMap = new HashMap<>();

    /**
     * 建筑物占用地块配置
     */
    private static Map<BuildingType, Integer> buildingSizeConfigMap = new HashMap<>();

    /**
     * 景观配置
     */
    private static Map<TerrainType, Float> viewConfigMap = new HashMap<>();


    static {

        /*****************
         * 地形配置       *
         *****************/

        /**
         * 快速通过
         */
        terrainConfigMap.put(TerrainType.PAVEMENT, new TerrainConfig(
                // 铺装路面，存在于主路和支路
                TerrainType.PAVEMENT, new LocateType[]{LocateType.ROAD}));
        // 废弃，作为资源随机出现，不在道路上出现
        /*
        terrainConfigMap.put(TerrainType.VEGETABLE, new TerrainConfig(
                // 蔬菜地，存在于主路
                TerrainType.VEGETABLE, new LocateType[]{LocateType.PAVEMENT}, 0.05f));
        */

        /**
         * 不能通过，需要找资源才能通过
         */
        terrainConfigMap.put(TerrainType.LAVA, new TerrainConfig(
                // 岩浆：存在于快速路，
                TerrainType.LAVA, new LocateType[]{LocateType.SHORTCUT}, 1, 1, 0, 0));
        terrainConfigMap.put(TerrainType.WATER, new TerrainConfig(
                // 水面：存在于快速路，
                TerrainType.WATER, new LocateType[]{LocateType.SHORTCUT}, 1, 1, 0, 0, 0.05f));
        terrainConfigMap.put(TerrainType.FOREST, new TerrainConfig(
                // 森林：存在于快速路
                TerrainType.FOREST, new LocateType[]{LocateType.SHORTCUT}, 1, 1, 0, 0, 0.05f));
        terrainConfigMap.put(TerrainType.ROCK, new TerrainConfig(
                // 石头地：存在于快速路
                TerrainType.ROCK, new LocateType[]{LocateType.SHORTCUT}, 1, 1, 0, 0, 0.05f));
        terrainConfigMap.put(TerrainType.BLANK, new TerrainConfig(
                // 下沉路面：存在于快速路，
                TerrainType.BLANK, new LocateType[]{LocateType.SHORTCUT}, 1, 1, 0, 0, 0.05f));

        /**
         * 缓慢通过，可以停止运动休息，单纯增加运动距离
         */
        terrainConfigMap.put(TerrainType.GRASS, new TerrainConfig(
                // 荒地：很多比较高的野草，存在于主路或者支路，速度降低30%，单段长度10-30
                TerrainType.GRASS, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 2, 4, 0.9f, 0));
        terrainConfigMap.put(TerrainType.WHEAT, new TerrainConfig(
                // 麦田：存在于主路和快速路，速度降低30%，单端长度7-24
                TerrainType.WHEAT, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 2, 4, 0.7f, 0, 0.05f));
        terrainConfigMap.put(TerrainType.RAIN, new TerrainConfig(
                // 雨地：存在于主路和快速路，速度降低30%，单端长度7-24
                TerrainType.RAIN, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 2, 4, 0.7f, 0));

        /**
         *  缓慢通过，不可以停止运动，考验持续长距离跑步能力
         */
        terrainConfigMap.put(TerrainType.SAND, new TerrainConfig(
                // 沙地：存在于主路和快速路，速度降低30%，单端长度7-24
                TerrainType.SAND, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 2, 4, 0.7f, 0));
        terrainConfigMap.put(TerrainType.SWAMP, new TerrainConfig(
                // 沼泽：存在于主路和快速路，速度降低70%，单端长度4-15
                TerrainType.SWAMP, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 2, 4, 0.3f, 0));

        /**
         * 缓慢通过，必须保持速度，否则后退或者返回某一个复活点
         * 只在快速路出现，考验短距离快速跑步能力
         */
        terrainConfigMap.put(TerrainType.SNOW, new TerrainConfig(
                // 雪地；存在于快速路，速度降低50%，单端长度5-19，规定时间通过，否则返回墓地
                TerrainType.SNOW, new LocateType[]{LocateType.SHORTCUT}, 2, 3, 0.5f, 0));
        terrainConfigMap.put(TerrainType.WIND, new TerrainConfig(
                // 大风地带：存在于快速路，速度降低40%，单端长度5-19
                TerrainType.WIND, new LocateType[]{LocateType.SHORTCUT}, 2, 3, 0.6f, 0.2f));
        terrainConfigMap.put(TerrainType.WILD_WIND, new TerrainConfig(
                // 飓风地带：存在于快速路，速度降低60%，单端长度5-19
                TerrainType.WILD_WIND, new LocateType[]{LocateType.SHORTCUT}, 2, 3, 0.6f, 0.2f));
        terrainConfigMap.put(TerrainType.ANIMAL, new TerrainConfig(
                // 有猛兽的地方, 存在于快速路，实际速度应该在平时的200%，保持一段时间 单段长度5-19
                TerrainType.ANIMAL, new LocateType[]{LocateType.SHORTCUT}, 2, 3, 1, 0.5f));

        /**
         * 其他
         */
        terrainConfigMap.put(TerrainType.FOG, new TerrainConfig(
                // 浓雾：存在于主路和支路，随机地形，单端长度5-20
                TerrainType.FOG, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 2, 3, 1, 0));


        /*****************
         * 建筑物配置      *
         *****************/

        buildingConfigMap.put(BuildingType.WAREHOUSE, (byte)5);
        buildingConfigMap.put(BuildingType.STORE, (byte)5);
        buildingConfigMap.put(BuildingType.TALL_TREE, (byte)5);
        buildingConfigMap.put(BuildingType.GRAVEYARD, (byte)5);
        buildingConfigMap.put(BuildingType.WELL, (byte)5);
        buildingConfigMap.put(BuildingType.JACKSTRAW, (byte)5);

        buildingSizeConfigMap.put(BuildingType.WAREHOUSE, 5);
        buildingSizeConfigMap.put(BuildingType.STORE, 7);
        buildingSizeConfigMap.put(BuildingType.TALL_TREE, 5);
        buildingSizeConfigMap.put(BuildingType.GRAVEYARD, 5);
        buildingSizeConfigMap.put(BuildingType.WELL, 5);
        buildingSizeConfigMap.put(BuildingType.JACKSTRAW, 5);

        /*****************
         * 景观配置       *
         *****************/

        viewConfigMap.put(TerrainType.FOREST, 0.05f);
        viewConfigMap.put(TerrainType.ROCK, 0.01f);
        viewConfigMap.put(TerrainType.VEGETABLE, 0.005f);
    }

    public enum LocateType {ROAD, SHORTCUT}

    public static class BuildingConfig {

        // 建筑物id
        byte buildingId;

        // 出现几率
        float probability;

    }

    public static class TerrainConfig {

        // 地形id
        TerrainType terrainType;

        // 存在的类型, 分支还是主路
        LocateType[] locateTypes;

        // 地形最小长度
        int minLength;

        // 地形最大长度
        int maxLength;

        // 地形速度与正常速度的比
        float speedRate;

        // 是否为资源地形
        boolean isResource;

        // 为资源地形时出现的几率
        float resourceOdds;

        // 奖励加成，比如要求保持速度的地形，实际跑步距离可以比分支距离加上缩短距离少10%
        float addOn;

        TerrainConfig(TerrainType terrainType, LocateType[] locateTypes) {
            this.terrainType = terrainType;
            this.locateTypes = locateTypes;
        }

        TerrainConfig(TerrainType terrainType, LocateType[] locateTypes, float resourceOdds) {
            this.terrainType = terrainType;
            this.locateTypes = locateTypes;
            this.resourceOdds = resourceOdds;
        }

        TerrainConfig(TerrainType terrainType, LocateType[] locateTypes, int minLength, int maxLength, float speedRate, float addOn) {
            this.terrainType = terrainType;
            this.locateTypes = locateTypes;
            this.minLength = minLength;
            this.maxLength = maxLength;

            if (speedRate > 1) this.speedRate = 1;
            else if (speedRate < 0) this.speedRate = 0;
            else this.speedRate = speedRate;

            this.addOn = addOn;
            this.isResource = false;
        }

        TerrainConfig(TerrainType terrainType, LocateType[] locateTypes, int minLength, int maxLength, float speedRate,
                      float addOn, float resourceOdds) {
            this(terrainType, locateTypes, minLength, maxLength, speedRate, addOn);
            this.resourceOdds = resourceOdds;
        }
    }

    public GameMap generate(int boundX, int boundY, int bridgeWidth) {
        GameMapCreator gameMapCreator = new GameMapCreator(boundX, boundY);

        int startBlockId = gameMapCreator.getBlockId(1, 1);
        int endBlockId = gameMapCreator.getBlockId(boundX - 2, boundY - 2);

        gameMapCreator.start = startBlockId;
        gameMapCreator.end = endBlockId;

        int length = 0;
        while (length < 10) {
            gameMapCreator.clear();
            gameMapCreator.fillWithRandomRoads();
            gameMapCreator.removeBrand(startBlockId, endBlockId);

            length = gameMapCreator.countLength(startBlockId, endBlockId);
            System.out.println("path length: " + length);
        }

        gameMapCreator.generateSingleRouteInfo(startBlockId, endBlockId);
        gameMapCreator.printTerrainBlocks(3);

        int mergeCount = 50;
        for (int count = 0; count < mergeCount; count ++) {
            GameMapCreator gameMapCreatorForMerge = new GameMapCreator(boundX, boundY);

            length = 0;
            while (length < 10) {
                gameMapCreatorForMerge.clear();
                gameMapCreatorForMerge.fillWithRandomRoads();
                gameMapCreatorForMerge.removeBrand(startBlockId, endBlockId);

                length = gameMapCreatorForMerge.countLength(startBlockId, endBlockId);
                System.out.println("path length: " + length);
            }

            gameMapCreatorForMerge.generateSingleRouteInfo(startBlockId, endBlockId);
            gameMapCreator.merge(gameMapCreatorForMerge);
        }
        gameMapCreator.printTerrainBlocks(3);

        GameMap gameMap = gameMapCreator.expandWithBorder(bridgeWidth);
        for (Block block: gameMap.occupiedBlockMap.values()) {
            block.blockGroupId = gameMap.ROAD_GROUP_ID;
        }

        //generateTerrainForMainRoad(gameMap);
        //generateTerrainForShortcuts(gameMap);
        generateTerrainTest(gameMap);

        twistMap(gameMap, 20);
        gameMap.printMap();

        expandRoadV3(gameMap, 6);
        gameMap.printMap();
        generateLamp(gameMap);

        gameMap.generateMapBytes();
        /*
        generateBuildings(gameMap);
        */

        //gameMap.printGroupId();
        //gameMap.printDistances();
        //gameMap.printHeight();

        System.out.println("\nblock count" + gameMap.occupiedBlockMap.size());
        System.out.print("\n");

        for (BlockGroup blockGroup : gameMap.blockGroupMap.values()) {
            System.out.print(blockGroup);
            System.out.print(",");
        }
        System.out.print("\n");

        return gameMap;
    }

    public void generateTerrainTest(GameMap gameMap) {
        short groupId = gameMap.getNextGroupId();

        int length = 50;
        Block startBlock = gameMap.mainRoad.blockList.get(25);
        Block block = startBlock;
        Block endBlock = null;
        while (length > 0) {
            block.blockGroupId = groupId;
            block = block.next;
            endBlock =  block;
            length --;
        }

        gameMap.blockGroupMap.put(
                groupId,
                new BlockGroup(
                        groupId, TerrainType.WILD_FIRE, startBlock.blockId, endBlock.blockId, length)
        );
    }

    /**
     * 给主路随机增加地形
     * 1.增加的长度不超过总长度的30%
     * 2.允许在主路上的地形随机分布，长度为最小长度和最大长度的随机值
     * 3.特殊地形之间有随机距离，长度为平均间隔距离的一半到1.5倍之间的随机值
     */
    public void generateTerrainForMainRoad(GameMap gameMap) {
        // 计算特殊路段长度
        Branch mainRoad = gameMap.mainRoad;
        int specialSegTotalLen = (int) (mainRoad.blockList.size() * ROAD_EXTEND_PERCENT / gameMap.bridgeWidth);
        System.out.println("special terrain: " + specialSegTotalLen);

        // 获取有效的特殊路段列表
        List<TerrainConfig> validTerrainConfig = new ArrayList<>();
        for (TerrainConfig terrainConfig: terrainConfigMap.values()) {
            for (LocateType locateType: terrainConfig.locateTypes) {
                if (locateType == LocateType.ROAD && terrainConfig.terrainType != TerrainType.PAVEMENT) {
                    validTerrainConfig.add(terrainConfig);
                    continue;
                }
            }
        }

        // 生成随机的特殊路段列表
        List<Pair<TerrainType, Integer>> specialSegmentList = new LinkedList<>();
        while (specialSegTotalLen > 0) {
            TerrainConfig terrainConfig = validTerrainConfig.get(
                    ThreadLocalRandom.current().nextInt(validTerrainConfig.size()));
            int specialSegLen = ThreadLocalRandom.current().nextInt(
                    terrainConfig.minLength, terrainConfig.maxLength + 1);
            specialSegmentList.add(new Pair(terrainConfig.terrainType, specialSegLen));
            specialSegTotalLen -= specialSegLen;
        }

        int normalSegTotalLen =
                (int) (mainRoad.blockList.size() * (1 - ROAD_EXTEND_PERCENT) / (gameMap.bridgeWidth + 1));
        System.out.println("special terrain map: " + specialSegmentList);
        setBranchV2(gameMap, specialSegmentList, mainRoad, normalSegTotalLen, false);
    }

    /**
     * 给支路增加地形：
     * 1.支路距离加上地形距离应该稍少于路径节省距离，另外一半随机
     * 2.比较长的支路应该有多个特殊地形
     */
    public void generateTerrainForShortcuts(GameMap gameMap) {
        List<Branch> shortcutList = gameMap.shortcutList;
        List<TerrainConfig> validTerrainConfig = new ArrayList<>();
        for (TerrainConfig terrainConfig: terrainConfigMap.values()) {
            for (LocateType locateType: terrainConfig.locateTypes) {
                if (locateType == LocateType.SHORTCUT) {
                    validTerrainConfig.add(terrainConfig);
                }
            }
        }

        for (Branch branch: shortcutList) {
            generateTerrainForShortcut(gameMap, validTerrainConfig, branch);
        }
    }

    private void generateTerrainForShortcut(
            GameMap gameMap, List<TerrainConfig> validTerrainConfigList, Branch branch) {
        int distance = branch.getShort() / (gameMap.bridgeWidth + 1);
        List<Pair<TerrainType, Integer>> specialSegmentList = new LinkedList<>();

        int totalSegmentLength = 0;
        while(totalSegmentLength < distance / 2) {
            TerrainConfig terrainConfig = validTerrainConfigList.get(
                    ThreadLocalRandom.current().nextInt(validTerrainConfigList.size()));

            for (int i = terrainConfig.maxLength; i >= terrainConfig.minLength; i--) {
                int length;
                if (terrainConfig.speedRate == 0) {
                    length = 2 * i;
                } else {
                    length = (int) ((float) i / terrainConfig.speedRate - 1);
                }
                if (length + totalSegmentLength > distance) continue;
                else {
                    if (length >= branch.size()) {
                        length = branch.size() / (gameMap.bridgeWidth + 1) - 1;
                    }
                    specialSegmentList.add(new Pair(terrainConfig.terrainType, length));
                    totalSegmentLength += length;
                    distance -= length;
                }
            }
        }
        setBranchV2(gameMap, specialSegmentList, branch, branch.getShort(), true);
    }

    private void setBranchV2(GameMap gameMap, List<Pair<TerrainType, Integer>> specialSegmentList,
                             Branch branch, int normalSegDistance, boolean shortcut) {
        if (specialSegmentList.size() == 0) {
            return;
        }

        // 生成随机普通路面列表
        int normalSegCount;
        int normalSegTotalLen;
        if (shortcut) {
            normalSegCount = specialSegmentList.size() - 1;
            normalSegTotalLen = normalSegDistance;
        } else {
            normalSegCount = specialSegmentList.size() - 1;
            normalSegTotalLen = normalSegDistance;
        }

        List<Integer> normalSegmentList = new ArrayList<>();
        if (normalSegCount > 0) {
            int leastSegLen = (int) (normalSegTotalLen / normalSegCount * 0.6);
            if (leastSegLen < 1) leastSegLen = 1;

            int normalSegRemain = normalSegTotalLen - normalSegCount * leastSegLen;
            for (int i = 0; i < normalSegCount; i ++) {
                int add = ThreadLocalRandom.current().nextInt(0, normalSegRemain + 1);
                normalSegmentList.add(leastSegLen + add);
                normalSegRemain -= add;
            }
        }

        // 找到第一个生成点
        Block currentBlock = branch.blockList.get(0);
        while (currentBlock != null &&
                ((currentBlock.position.x + 1) % (gameMap.bridgeWidth + 1) != ((gameMap.bridgeWidth + 1) / 2) &&
                        (currentBlock.position.y + 1) % (gameMap.bridgeWidth + 1) != ((gameMap.bridgeWidth + 1) / 2))) {
            currentBlock = currentBlock.next;
        }

        for (Pair<TerrainType, Integer> specialSegment: specialSegmentList) {
            int count = specialSegment.getY() * (gameMap.bridgeWidth + 1);
            if (count <= 0) continue;

            short groupId = gameMap.getNextGroupId();
            //gameMap.blockGroupMap.put(interaciveObjectId, specialSegment.getX());
            for (int i = 0; i < count; i ++) {
                if (currentBlock == null) {
                    break;
                }
                //currentBlock.terrainType = specialSegment.getX();
                currentBlock.blockGroupId = groupId;
                currentBlock = currentBlock.next;
            }
            if (currentBlock != null) {
                System.out.println("special terrain count: " + count);
            }

            if (normalSegmentList.size() > 0) {
                int normalSegmentLenIndex = ThreadLocalRandom.current().nextInt(normalSegmentList.size());
                for (int i = 0; i < normalSegmentList.get(normalSegmentLenIndex) * (gameMap.bridgeWidth + 1); i++) {
                    if (currentBlock == null) {
                        break;
                    }
                    currentBlock = currentBlock.next;
                }
                if (currentBlock != null) {
                    System.out.println("normal terrain count: " +
                            normalSegmentList.get(normalSegmentLenIndex) * (gameMap.bridgeWidth + 1));
                }
                normalSegmentList.remove(normalSegmentLenIndex);
            }
        }
    }

    private void expandRoadV3(GameMap gameMap, int width) {
        gameMap.expandWidth = width;
        // 扩展道路
        Map<Integer, Pair<Integer, Short>> addBlockMap= new HashMap<>();
        for (int blockId: gameMap.unoccupiedBlockSet) {
            Pair<Integer, Block> result = gameMap.getNearestBlockOfCross(
                    blockId, width, BlockType.ARTERY, BlockType.SHORTCUT, BlockType.BRANCH);

            if (result != null) {
                addBlockMap.put(blockId, new Pair<>(result.getX(), result.getY().blockGroupId));
            }
        }

        for (Map.Entry<Integer, Pair<Integer, Short>> entry: addBlockMap.entrySet()) {
            gameMap.addBlock(entry.getKey(), BlockType.ROAD_EXTENSION, RoadDirection.NONE,
                    entry.getValue().getY(), entry.getValue().getX(), -1);
        }
    }

    /*
    private void expandRoadV2(GameMap gameMap, int width) {
        // 扩展道路
        Map<Integer, Pair<Integer, Short>> addBlockMap= new HashMap<>();
        for (int blockId: gameMap.unoccupiedBlockSet) {
            Pair<Integer, Block> result = gameMap.getNearestBlockOfCross(
                    blockId, width, BlockType.ARTERY, BlockType.SHORTCUT, BlockType.BRANCH);

            if (result != null) {
                addBlockMap.put(blockId, new Pair<>(result.getX(), result.getY().interaciveObjectId));
            }
        }

        for (Map.Entry<Integer, Pair<Integer, Short>> entry: addBlockMap.entrySet()) {
            gameMap.addBlock(entry.getKey(), BlockType.ROAD_EXTENSION, TerrainType.PAVEMENT, RoadDirection.NONE,
                    entry.getValue().getY(), entry.getValue().getX(), -1);
        }

        // 将三面被道路包围的空白块添加为道路块
        addSurroundedBlock(gameMap, BlockType.ROAD_EXTENSION, TerrainType.PAVEMENT, width, BlockType.ROAD_EXTENSION);
        System.out.println("\n\nafter road extension\n\n");
        gameMap.printMap();

        // 将距离道路4个单位及以上的块添加为3级山地
        List<Integer> blockList = new LinkedList<>();
        for (int blockId: gameMap.unoccupiedBlockSet) {
            if (gameMap.getBlocksInRangeOfCross(blockId, 4, BlockType.ROAD_EXTENSION).size() == 0) {
                blockList.add(blockId);
            }
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L3, TerrainType.NONE, GameMap.MOUNTAIN_LEVEL_1_GROUP_ID);
        }

        // 将剩下的块添加为1级山地
        blockList.clear();
        for (int blockId: gameMap.unoccupiedBlockSet) {
            blockList.add(blockId);
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L1, TerrainType.NONE, GameMap.MOUNTAIN_LEVEL_1_GROUP_ID);
        }
        System.out.println("\n\nafter 3/1 mountain\n\n");
        gameMap.printMap();

        // 将四个边的三级山地及周边地块转成一级海
        Set<Block> edgeBlockSets = new HashSet<>();
        for (int i = 0; i < gameMap.boundX; i ++) {
            if (gameMap.getBlockType(i, 0) == BlockType.MOUNTAIN_L3)
                edgeBlockSets.add(gameMap.getBlock(i, 0));
            if (gameMap.getBlockType(i, gameMap.boundY - 1) == BlockType.MOUNTAIN_L3)
                edgeBlockSets.add(gameMap.getBlock(i, gameMap.boundY - 1));
            if (gameMap.getBlockType(0, i) == BlockType.MOUNTAIN_L3)
                edgeBlockSets.add(gameMap.getBlock(0, i));
            if (gameMap.getBlockType(gameMap.boundX - 1, i) == BlockType.MOUNTAIN_L3)
                edgeBlockSets.add(gameMap.getBlock(gameMap.boundX - 1, i));
        }

        for (Block block: edgeBlockSets) {
            block.type = BlockType.SEA_L1;
        }

        Set<Block> seaLevel6BlockSets = new HashSet<>();
        Queue<Block> blockQueue = new LinkedList<>();

        for (Block edgeBlock: edgeBlockSets) {
            blockQueue.offer(edgeBlock);
        }
        Set<Block> executedSet = new HashSet<>();
        while(blockQueue.size() > 0) {
            Block block = blockQueue.poll();
            executedSet.add(block);
            List<Block> adjacentBlocks = gameMap.getAdjacentInDistance(block.blockId, 1, true, BlockType.MOUNTAIN_L3);
            for(Block adjacentBlock: adjacentBlocks) {
                seaLevel6BlockSets.add(adjacentBlock);
                if (!executedSet.contains(adjacentBlock) && !blockQueue.contains(adjacentBlock)) {
                    blockQueue.offer(adjacentBlock);
                }
                block.type = BlockType.SEA_L1;
            }
        }

        System.out.println("\n\nafter edge sea\n\n");
        gameMap.printMap();

        // 找到所有的三级山地并分组，面积大于150的三级山地转成一级海
        Set<Block> aBlockSets = new HashSet<>();
        for(Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MOUNTAIN_L3) {
                aBlockSets.add(block);
            }
        }

        List<Set<Block>> blockSetList = new LinkedList<>();
        blockQueue.clear();
        executedSet.clear();
        while(aBlockSets.size() > 0) {
            Set<Block> currentBlockSet = new HashSet<>();
            blockSetList.add(currentBlockSet);

            Block firstBlock = (Block)aBlockSets.toArray()[0];

            blockQueue.add(firstBlock);
            currentBlockSet.add(firstBlock);
            aBlockSets.remove(firstBlock);

            while(blockQueue.size() > 0) {
                Block block = blockQueue.poll();
                executedSet.add(block);
                List<Block> adjacentBlocks = gameMap.getAdjacentInDistance(block.blockId, 1, false, BlockType.MOUNTAIN_L3);
                for (Block adjacentBlock: adjacentBlocks) {
                    if (!executedSet.contains(adjacentBlock) && !blockQueue.contains(adjacentBlock)) {
                        blockQueue.offer(adjacentBlock);
                    }
                    currentBlockSet.add(adjacentBlock);
                    aBlockSets.remove(adjacentBlock);
                }
            }
        }

        for (Set<Block> blockSet: blockSetList) {
            if (blockSet.size() > 450) {
                for (Block block: blockSet) {
                    seaLevel6BlockSets.add(block);
                    block.type = BlockType.SEA_L1;
                }
            } else if (blockSet.size() > 250) {
                for (Block block: blockSet) {
                    seaLevel6BlockSets.add(block);
                    block.type = BlockType.HILL;
                }
            }
        }

        System.out.println("\n\nafter 150 to sea\n\n");
        gameMap.printMap();

        // 扩展1级海周边两格为1级海
        aBlockSets.clear();
        Set<Block> hillBlockSet = new HashSet<>();
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MOUNTAIN_L1) {
                int distance = gameMap.getNearestBlockDistanceOfAround(block.blockId, 4, BlockType.SEA_L1);
                if (distance != -1) {
                    aBlockSets.add(block);
                }
                distance = gameMap.getNearestBlockDistanceOfAround(block.blockId, 4, BlockType.HILL);
                if (distance != -1) {
                    hillBlockSet.add(block);
                }
            }
        }

        for (Block block: aBlockSets) {
            block.type = BlockType.SEA_L1;
        }
        for (Block block: hillBlockSet) {
            block.type = BlockType.HILL;
        }

        System.out.println("\n\nafter sea expansion \n\n");
        gameMap.printMap();

        // 生成height map数据
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type != BlockType.SEA_L1) {
                int distance = gameMap.getNearestBlockDistanceOfAround(block.blockId, 30, BlockType.SEA_L1);
                if (distance == -1) {
                    block.height = 30;
                } else {
                    block.height = (byte)distance;
                }
            }
        }

        // 让road旁边的道路块与road块保持高度一致
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.ROAD_EXTENSION) {
                List<Block> nearestRoadBlockList = gameMap.getNearestBlocksOfAround(block.blockId, 10,
                        BlockType.ARTERY, BlockType.SHORTCUT, BlockType.BRANCH);
                if (nearestRoadBlockList == null) continue;
                int sum = 0;
                for (Block roadBlock: nearestRoadBlockList) {
                    sum += roadBlock.height;
                }
                block.height = (byte) (sum / nearestRoadBlockList.size());
            }
        }

        // 按与道路的距离修改1级海为1到6级海
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.SEA_L1) {
                int distance = gameMap.getNearestBlockDistanceOfAround(block.blockId, 6,
                        BlockType.ROAD_EXTENSION, BlockType.MOUNTAIN_L1, BlockType.MOUNTAIN_L3);
                if (distance == -1) {
                    block.type = BlockType.SEA_L6;
                } else {
                    block.type = gameMap.getBlockTypeByDepth(- distance);
                }
            }
        }

        System.out.println("\n\nafter sea define level \n\n");
        gameMap.printMap();

        // 将三级山地转成一级山地
        for(Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MOUNTAIN_L3) {
                block.type = BlockType.MOUNTAIN_L1;
            }
        }

        // 生成树林用地
        int randomPosition = ThreadLocalRandom.current().nextInt(1, 1000000);
        for(Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MOUNTAIN_L1 && block.height > 10) {
                double value = Simplex.noise(
                        ((double)block.position.x + randomPosition) / 40,
                        ((double)block.position.y + randomPosition) / 40);
                if (value > 0.2) {
                    block.type = BlockType.WOODS;
                }
            }
        }

        // 计算树林用地和道路的距离
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.WOODS || block.type == BlockType.HILL) {
                int distance = gameMap.getNearestBlockDistanceOfAround(block.blockId, 30, BlockType.ROAD_EXTENSION);
                if (distance == -1) {
                    block.distanceAwayFromRoadExtention = 30;
                } else {
                    block.distanceAwayFromRoadExtention = (byte)distance;
                }
            }
        }

        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MOUNTAIN_L1) {
                Pair<Integer, Block> pair = gameMap.getNearestBlockOfCross(
                        block.blockId, 1, BlockType.ROAD_EXTENSION, BlockType.WOODS, BlockType.HILL,
                        BlockType.SEA_L1, BlockType.SEA_L2);
                if (pair!= null) {
                    block.type = BlockType.MOUNTAIN_SLOP;
                }
            }
        }
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.ROAD_EXTENSION || block.type == BlockType.SEA_L1 ||
                    block.type == BlockType.HILL || block.type == BlockType.WOODS ) {
                int distance = gameMap.getNearestBlockDistanceOfAround(block.blockId, 1, BlockType.MOUNTAIN_L1);
                if (distance != -1) {
                    block.type = BlockType.MOUNTAIN_SLOP;
                }
            }
        }

        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MOUNTAIN_SLOP) {
                int distance = gameMap.getNearestBlockDistanceOfAround(block.blockId, 1, BlockType.MOUNTAIN_L1);
                if (distance == -1) {
                    block.type = BlockType.MOUNTAIN_ROCK;
                }
            }
        }

        System.out.println("\n\nafter 3 to 1 mountain and slop rock\n\n");
        gameMap.printMap();

        // 一级山地都转成生成二级山地
        for(Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MOUNTAIN_L1) {
                block.type = BlockType.MOUNTAIN_L2;
            }
        }

        // 按柏林噪声生成三级山地
        randomPosition = ThreadLocalRandom.current().nextInt(1, 1000000);
        for(Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MOUNTAIN_L1 || block.type == BlockType.MOUNTAIN_L2) {
                double value = Simplex.noise(
                        ((double)block.position.x + randomPosition) / 10,
                        ((double)block.position.y + randomPosition) / 10);
                if (value > 0) {
                    block.type = BlockType.MOUNTAIN_L3;
                }
            }
        }

        System.out.println("\n\nafter define mountain\n\n");
        gameMap.printMap();
    }
    */

    /*
    private void expandRoad(GameMap gameMap, int width) {
        Map<Integer, Pair<Integer, Short>> addBlockMap= new HashMap<>();
        for (int blockId: gameMap.unoccupiedBlockSet) {
            Pair<Integer, Block> result = gameMap.getNearestBlockOfCross(
                    blockId, width, BlockType.ARTERY, BlockType.SHORTCUT, BlockType.BRANCH);

            if (result != null) {
                addBlockMap.put(blockId, new Pair<>(result.getX(), result.getY().interaciveObjectId));
            }
        }

        for (Map.Entry<Integer, Pair<Integer, Short>> entry: addBlockMap.entrySet()) {
            gameMap.addBlock(entry.getKey(), BlockType.ROAD_EXTENSION, TerrainType.PAVEMENT, RoadDirection.NONE,
                    entry.getValue().getY(), entry.getValue().getX(), -1);
        }

        // 将三面被道路包围的空白块添加为道路块
        addSurroundedBlock(gameMap, BlockType.ROAD_EXTENSION, TerrainType.PAVEMENT, width, BlockType.ROAD_EXTENSION);
        gameMap.printMap();

        // 添加紧邻道路的块为一级山地，宽度为1格或者2格
        List<Integer> blockList = new LinkedList<>();
        for (int blockId: gameMap.unoccupiedBlockSet) {
            if (gameMap.getBlocksInRange(blockId, 1, BlockType.ROAD_EXTENSION).size() > 0) {
                blockList.add(blockId);
            }
            if (gameMap.getBlocksInRangeOfCross(blockId, 2, BlockType.ROAD_EXTENSION).size() > 0) {
                if (ThreadLocalRandom.current().nextInt(10) < 3) {
                    blockList.add(blockId);
                }
            }
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L1, TerrainType.NONE, GameMap.MOUNTAIN_LEVEL_1_GROUP_ID);
        }

        // 添加紧邻一级山地的块为二级山地，宽度为1格
        blockList.clear();
        for (int blockId: gameMap.unoccupiedBlockSet) {
            if (gameMap.getBlocksInRange(blockId, 1, BlockType.MOUNTAIN_L1).size() > 0) {
                blockList.add(blockId);
            }
            if (gameMap.getBlocksInRangeOfCross(blockId, 2, BlockType.MOUNTAIN_L1).size() > 0) {
                if (ThreadLocalRandom.current().nextInt(10) < 3) {
                    blockList.add(blockId);
                }
            }
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L2, TerrainType.NONE, GameMap.MOUNTAIN_LEVEL_2_GROUP_ID);
        }

        // 添加其余的块为三级山地
        blockList.clear();
        for (int blockId: gameMap.unoccupiedBlockSet) {
            blockList.add(blockId);
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L3, TerrainType.NONE, GameMap.MOUNTAIN_LEVEL_3_GROUP_ID);
        }

        gameMap.printMap();

        // 将四个边的三级山地及周边地块转成二级海
        Set<Block> edgeBlockSets = new HashSet<>();
        for (int i = 0; i < gameMap.boundX; i ++) {
            if (gameMap.getBlockType(i, 0) == BlockType.MOUNTAIN_L3)
                edgeBlockSets.add(gameMap.getBlock(i, 0));
            if (gameMap.getBlockType(i, gameMap.boundY - 1) == BlockType.MOUNTAIN_L3)
                edgeBlockSets.add(gameMap.getBlock(i, gameMap.boundY - 1));
            if (gameMap.getBlockType(0, i) == BlockType.MOUNTAIN_L3)
                edgeBlockSets.add(gameMap.getBlock(0, i));
            if (gameMap.getBlockType(gameMap.boundX - 1, i) == BlockType.MOUNTAIN_L3)
                edgeBlockSets.add(gameMap.getBlock(gameMap.boundX - 1, i));
        }

        for (Block block: edgeBlockSets) {
            block.type = BlockType.SEA_L6;
        }

        Set<Block> seaLevel3BlockSets = new HashSet<>();
        Queue<Block> blockQueue = new LinkedList<>();

        for (Block edgeBlock: edgeBlockSets) {
            blockQueue.offer(edgeBlock);
        }
        Set<Block> executedSet = new HashSet<>();
        while(blockQueue.size() > 0) {
            Block block = blockQueue.poll();
            executedSet.add(block);
            List<Block> adjacentBlocks = gameMap.getAdjacentInDistance(block.blockId, 1, true, BlockType.MOUNTAIN_L3);
            for(Block adjacentBlock: adjacentBlocks) {
                seaLevel3BlockSets.add(adjacentBlock);
                if (!executedSet.contains(adjacentBlock) && !blockQueue.contains(adjacentBlock)) {
                    blockQueue.offer(adjacentBlock);
                }
                block.type = BlockType.SEA_L6;
            }
        }

        // 找到所有的三级山地并分组，面积大于150的三级山地转成二级海
        Set<Block> aBlockSets = new HashSet<>();
        for(Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MOUNTAIN_L3) {
                aBlockSets.add(block);
            }
        }

        List<Set<Block>> blockSetList = new LinkedList<>();
        blockQueue.clear();
        executedSet.clear();
        while(aBlockSets.size() > 0) {
            Set<Block> currentBlockSet = new HashSet<>();
            blockSetList.add(currentBlockSet);

            Block firstBlock = (Block)aBlockSets.toArray()[0];

            blockQueue.add(firstBlock);
            currentBlockSet.add(firstBlock);
            aBlockSets.remove(firstBlock);

            while(blockQueue.size() > 0) {
                Block block = blockQueue.poll();
                executedSet.add(block);
                List<Block> adjacentBlocks = gameMap.getAdjacentInDistance(block.blockId, 1, false, BlockType.MOUNTAIN_L3);
                for (Block adjacentBlock: adjacentBlocks) {
                    if (!executedSet.contains(adjacentBlock) && !blockQueue.contains(adjacentBlock)) {
                        blockQueue.offer(adjacentBlock);
                    }
                    currentBlockSet.add(adjacentBlock);
                    aBlockSets.remove(adjacentBlock);
                }
            }
        }

        for (Set<Block> blockSet: blockSetList) {
            if (blockSet.size() > 150) {
                for (Block block: blockSet) {
                    seaLevel3BlockSets.add(block);
                    block.type = BlockType.SEA_L6;
                }
            }
        }

        // 和二级海相邻的二级山地转成一级海
        Set<Block> seaLevel2BlockSets = new HashSet<>();
        for (Block seaLevel3Block: seaLevel3BlockSets) {
            List<Block> adjacentBlocks = gameMap.getAdjacent(seaLevel3Block.blockId, 2, true,
                    BlockType.MOUNTAIN_L3, BlockType.MOUNTAIN_L2, BlockType.MOUNTAIN_L1);
            for(Block block: adjacentBlocks) {
                seaLevel2BlockSets.add(block);
                block.type = BlockType.SEA_L1;
            }
        }

        // 和一级海相邻的一级山地转成一级海
        for (Block seaLevel2Block: seaLevel2BlockSets) {
            List<Block> blocks = gameMap.getAdjacent(seaLevel2Block.blockId, 1, true,
                    BlockType.MOUNTAIN_L3, BlockType.MOUNTAIN_L2, BlockType.MOUNTAIN_L1);
            for(Block block: blocks) {
                block.type = BlockType.SEA_L1;
            }
        }
    }
    */

    private void addSurroundedBlock(
            GameMap gameMap, BlockType blockType, TerrainType terrainType, int width, BlockType... surroundedBlockTypes) {
        List<Integer> roadBlockList = new LinkedList<>();
        boolean clear = false;
        while (!clear) {
            for (int blockId: gameMap.unoccupiedBlockSet) {
                if (gameMap.getAdjacentInDistance(blockId, 1, false, surroundedBlockTypes).size() >= 3) {
                    roadBlockList.add(blockId);
                }
            }

            if (roadBlockList.size() == 0) clear = true;
            else {
                for (int blockId: roadBlockList) {
                    gameMap.addBlock(blockId, blockType, RoadDirection.NONE, gameMap.ROAD_GROUP_ID, width + 1, -1);
                }
            }
            roadBlockList.clear();
        }
    }

    private void twistMap(GameMap gameMap, int maxOffset) {
        Set<Integer> turningSet = new HashSet<>();
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.ARTERY || block.type == BlockType.SHORTCUT) {
                RoadDirection roadDirection = gameMap.getRoadDirection(
                        block.getBlockId(), BlockType.ARTERY, BlockType.SHORTCUT);
                if (roadDirection == RoadDirection.TURNING || roadDirection == RoadDirection.CROSS ||
                        roadDirection == RoadDirection.VERTICAL_END || roadDirection == RoadDirection.HORIZONTAL_END) {
                    turningSet.add(block.blockId);
                }
            }
        }

        Set<Integer> removedBlockSet = new HashSet<>();
        int lastOffset = 0;
        boolean moved = false;
        for (int i = 0; i < gameMap.mainRoad.blockList.size(); i ++) {
        //for (Block block: gameMap.mainRoad.blockList) {
            Block block = gameMap.mainRoad.blockList.get(i);
            int nearDistance = nearTurning(block, turningSet, maxOffset * 2 + 10);
            int offset;

            if (moved) {
                offset = lastOffset;
            } else {
                int[] randomOffsets;
                if (lastOffset == - maxOffset) {
                    randomOffsets= new int[] {- maxOffset, (- maxOffset) + 1};
                } else if (lastOffset == maxOffset) {
                    randomOffsets= new int[] {maxOffset, maxOffset - 1};
                } else {
                    randomOffsets= new int[] {lastOffset - 1, lastOffset, lastOffset + 1};
                }

                int[] validOffsets = getValidOffsets(nearDistance, maxOffset);
                int[] possibleOffset = Util.intersect(randomOffsets, validOffsets);
                if (possibleOffset.length == 0) {
                    offset = 0;
                } else {
                    offset = possibleOffset[ThreadLocalRandom.current().nextInt(possibleOffset.length)];
                }
            }

            if (offset != lastOffset) {
                moved = true;
            } else {
                moved = false;
            }
            lastOffset = offset;


            RoadDirection roadDirection = gameMap.getRoadDirection(block.getBlockId(), BlockType.ARTERY);
            if (roadDirection == RoadDirection.HORIZONTAL) {
                if (offset != 0) {
                    Block newBlock = gameMap.addBlock(gameMap.getBlockId(block.position.x, block.position.y + offset),
                            block.type, block.roadDirection, block.blockGroupId,
                            block.distanceAwayFromRoad, block.distanceAwayFromRoadCrossPoint);
                    newBlock.next = block.next;
                    newBlock.previous = block.previous;
                    newBlock.previous.next = newBlock;
                    newBlock.next.previous = newBlock;
                    newBlock.distanceToFinish = block.distanceToFinish;

                    gameMap.mainRoad.blockList.set(i, newBlock);
                    removedBlockSet.add(block.blockId);
                }
            } else if (roadDirection == RoadDirection.VERTICAL) {
                if (offset != 0) {
                    Block newBlock = gameMap.addBlock(gameMap.getBlockId(block.position.x + offset, block.position.y),
                            block.type, block.roadDirection, block.blockGroupId,
                            block.distanceAwayFromRoad, block.distanceAwayFromRoadCrossPoint);
                    newBlock.next = block.next;
                    newBlock.previous = block.previous;
                    newBlock.previous.next = newBlock;
                    newBlock.next.previous = newBlock;
                    newBlock.distanceToFinish = block.distanceToFinish;

                    gameMap.mainRoad.blockList.set(i, newBlock);
                    removedBlockSet.add(block.blockId);
                }
            }
        }


        for (Branch branch: gameMap.shortcutList) {
            lastOffset = 0;
            moved = false;
            for (int i = 0; i < branch.blockList.size(); i ++) {
            //for (Block block: branch.blockList) {
                Block block = branch.blockList.get(i);
                int nearDistance = nearTurning(block, turningSet, maxOffset * 2 + 10);
                int offset = 0;

                if (moved) {
                    offset = lastOffset;
                } else {
                    int[] randomOffsets;
                    if (lastOffset == - maxOffset) {
                        randomOffsets= new int[] {- maxOffset, (- maxOffset) + 1};
                    } else if (lastOffset == maxOffset) {
                        randomOffsets= new int[] {maxOffset, maxOffset - 1};
                    } else {
                        randomOffsets= new int[] {lastOffset - 1, lastOffset, lastOffset + 1};
                    }

                    int[] validOffsets = getValidOffsets(nearDistance, maxOffset);
                    int[] possibleOffset = Util.intersect(randomOffsets, validOffsets);
                    if (possibleOffset.length == 0) {
                        offset = 0;
                    } else {
                        offset = possibleOffset[ThreadLocalRandom.current().nextInt(possibleOffset.length)];
                    }
                }

                if (offset != lastOffset) {
                    moved = true;
                } else {
                    moved = false;
                }
                lastOffset = offset;

                if (offset != 0) {
                    RoadDirection roadDirection = gameMap.getRoadDirection(block.getBlockId(), BlockType.SHORTCUT);
                    if (roadDirection == RoadDirection.HORIZONTAL) {
                        Block newBlock = gameMap.addBlock(gameMap.getBlockId(
                                block.position.x, block.position.y + offset), block.type, block.roadDirection,
                                block.blockGroupId, block.distanceAwayFromRoad, block.distanceAwayFromRoadCrossPoint);
                        newBlock.next = block.next;
                        newBlock.previous = block.previous;
                        newBlock.previous.next = newBlock;
                        newBlock.next.previous = newBlock;
                        newBlock.distanceToFinish = block.distanceToFinish;

                        branch.blockList.set(i, newBlock);
                        removedBlockSet.add(block.blockId);
                    } else if (roadDirection == RoadDirection.VERTICAL) {
                        Block newBlock = gameMap.addBlock(gameMap.getBlockId(
                                block.position.x + offset, block.position.y), block.type, block.roadDirection,
                                block.blockGroupId, block.distanceAwayFromRoad, block.distanceAwayFromRoadCrossPoint);
                        newBlock.next = block.next;
                        newBlock.previous = block.previous;
                        newBlock.previous.next = newBlock;
                        newBlock.next.previous = newBlock;
                        newBlock.distanceToFinish = block.distanceToFinish;

                        branch.blockList.set(i, newBlock);
                        removedBlockSet.add(block.blockId);
                    }
                }
            }
        }
        for (int blockId: removedBlockSet) {
            gameMap.occupiedBlockMap.remove(blockId);
            gameMap.unoccupiedBlockSet.add(blockId);
        }
    }

    private int[] getValidOffsets(int nearDistance, int maxOffset) {
        int allowOffset = (Math.abs(nearDistance) - 1) / 2;
        int offset = Math.min(allowOffset, maxOffset);
        int[] offsets = new int[offset * 2 + 1];
        for (int i = - offset, j = 0; i <= offset; i ++, j ++) {
            offsets[j] = i;
        }
        return offsets;
    }

    private int nearTurning(Block block, Set<Integer> turningSet, int maxDistance) {
        if (block!= null && turningSet.contains(block.blockId)) {
            return 0;
        }

        Block nearBlock = block.next;
        int distance = Integer.MAX_VALUE;
        for (int i = 1; i <= maxDistance; i ++) {
            if (nearBlock == null || turningSet.contains(nearBlock.blockId)) {
                if (distance > i) distance = i;
                break;
            }
            nearBlock = nearBlock.next;
        }

        nearBlock = block.previous;
        for (int i = -1; i >= -maxDistance; i --) {
            if (nearBlock == null || turningSet.contains(nearBlock.blockId)) {
                if (distance > -i) distance = i;
                break;
            }
            nearBlock = nearBlock.previous;
        }
        return distance;
    }

    /**
     * 生成灯
     */
    private void generateLamp(GameMap gameMap) {
        int prevId = 0;
        for (Block block: gameMap.mainRoad.blockList) {
            if (block.distanceAwayFromRoadCrossPoint != 0 && block.distanceAwayFromRoadCrossPoint != 25) continue;
            gameMap.lampMap.put(
                    block.blockId,
                    new Lamp(block.blockId, new Point(block.position.x, block.position.y, 0), 0));
            if (prevId != 0) {
                gameMap.lampMap.get(prevId).addSiblings(block.blockId);
                gameMap.lampMap.get(block.blockId).addSiblings(prevId);
            }
            prevId = block.blockId;
        }

        for (Branch branch: gameMap.shortcutList) {
            prevId = branch.head.blockId;
            for (Block block: branch.blockList) {
                if (block.distanceAwayFromRoadCrossPoint != 0 && block.distanceAwayFromRoadCrossPoint != 25) continue;
                gameMap.lampMap.put(
                        block.blockId,
                        new Lamp(block.blockId, new Point(block.position.x, block.position.y, 0), 0));
                gameMap.lampMap.get(prevId).addSiblings(block.blockId);
                gameMap.lampMap.get(block.blockId).addSiblings(prevId);
                prevId = block.blockId;
            }
            gameMap.lampMap.get(prevId).addSiblings(branch.tail.blockId);
            gameMap.lampMap.get(branch.tail.blockId).addSiblings(prevId);
        }
        System.out.println("generated lamp: " + gameMap.lampMap.size());
    }

    private void generateBuildings(GameMap gameMap) {
        Map<Block, Direction> possiblePositionMap1 = new HashMap<>();
        Map<Block, Direction> possiblePositionMap2 = new HashMap<>();
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MOUNTAIN_L3 ||
                    block.type == BlockType.MOUNTAIN_L2 ||
                    block.type == BlockType.MOUNTAIN_L1 ||
                    block.type == BlockType.MOUNTAIN_SLOP ||
                    block.type == BlockType.MOUNTAIN_ROCK ||
                    block.type == BlockType.SEA_L1 ||
                    block.type == BlockType.SEA_L2 ||
                    block.type == BlockType.SEA_L3) {
                Map<Block, Direction> adjacentBlockMap =
                        gameMap.getAdjacentWithDirInDistance(block.blockId, 2, BlockType.ROAD_EXTENSION);
                if (adjacentBlockMap.size() > 0) {
                    for (Direction direction: adjacentBlockMap.values()) {
                        possiblePositionMap1.put(block, direction);
                        break;
                    }
                } else {
                    adjacentBlockMap = gameMap.getAdjacentWithDirInDistance(block.blockId, 3, BlockType.ROAD_EXTENSION);
                    for (Direction direction : adjacentBlockMap.values()) {
                        possiblePositionMap2.put(block, direction);
                        break;
                    }
                }
            }
        }

        List<Block> key1Array = new ArrayList<>(possiblePositionMap1.keySet());
        List<Block> key2Array = new ArrayList<>(possiblePositionMap2.keySet());
        int id = 1;
        for (Map.Entry<BuildingType, Byte> buildingConfig : buildingConfigMap.entrySet()) {
            BuildingType buildingType = buildingConfig.getKey();
            int count = buildingConfig.getValue();

            Map<Block, Direction> chosenMap ;
            List<Block> chosenKeyArray;
            if (buildingSizeConfigMap.get(buildingType) == 5) {
                chosenMap = possiblePositionMap1;
                chosenKeyArray = key1Array;
            } else if (buildingSizeConfigMap.get(buildingType) == 7) {
                chosenMap = possiblePositionMap2;
                chosenKeyArray = key2Array;
            } else {
                continue;
            }

            while (count > 0) {
                List<Block> removeList = new LinkedList<>();
                while(count > 0 && chosenMap.size() > 0) {
                    int randomIndex = ThreadLocalRandom.current().nextInt(chosenMap.size());
                    Block buildingBlock = chosenKeyArray.get(randomIndex);
                    Direction direction = chosenMap.get(buildingBlock);
                    gameMap.buildingMap.put(id, new Building(id ++, buildingType, buildingBlock.position, direction));

                    count --;
                    removeList.clear();
                    for (Block otherBlock: chosenMap.keySet()) {
                        if (otherBlock.position.distance(buildingBlock.position) < 20) {
                            removeList.add(otherBlock);
                        }
                    }
                    for (Block removeBlock: removeList) {
                        chosenMap.remove(removeBlock);
                        chosenKeyArray.remove(removeBlock);
                    }
                }
            }
        }

        for (Building building: gameMap.buildingMap.values()) {
            Block buildingBlock = gameMap.occupiedBlockMap.get(
                    gameMap.getBlockId(building.point2D.x, building.point2D.y));
            if (buildingBlock == null) continue;
            buildingBlock.type = BlockType.BUILDING;
            int size = buildingSizeConfigMap.get(building.buildingType);
            int expandSize;
            if (size == 5) {
                expandSize = 3;
            } else if (size == 7) {
                expandSize = 4;
            } else {
                continue;
            }

            Map<Integer, List<Block>> adjacentBlockMap = gameMap.getBlocksAroundInDistances(buildingBlock.blockId, expandSize,
                    BlockType.MOUNTAIN_L1, BlockType.MOUNTAIN_L2, BlockType.MOUNTAIN_L3,
                    BlockType.MOUNTAIN_SLOP, BlockType.MOUNTAIN_ROCK,
                    BlockType.ROAD_EXTENSION,
                    BlockType.SEA_L1, BlockType.SEA_L2, BlockType.SEA_L3,
                    BlockType.SEA_L4, BlockType.SEA_L5, BlockType.SEA_L6);
            for (int distance: adjacentBlockMap.keySet()) {
                List<Block> adjacentBlockList = adjacentBlockMap.get(distance);
                if (adjacentBlockList != null && adjacentBlockList.size() > 0) {
                    for (Block block: adjacentBlockList) {
                        block.type = BlockType.BUILDING;
                        if (distance == expandSize) {
                            List<Block> blockList = gameMap.getBlocksAroundInDistance(block.blockId, 1,
                                    BlockType.MOUNTAIN_L1, BlockType.MOUNTAIN_L2, BlockType.MOUNTAIN_L3);
                            if (blockList.size() > 0) {
                                block.type = BlockType.MOUNTAIN_SLOP;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 生成有资源的块
     * 策略：按出现几率随机生成，生成地点是道路的两旁
     * 策略：随机生成
     * @param gameMap
     */
    private void generateResource(GameMap gameMap) {
        float totalResourceOddsCount = 0;
        for (TerrainConfig terrainConfig: terrainConfigMap.values()) {
            totalResourceOddsCount += terrainConfig.resourceOdds;
        }
        if (totalResourceOddsCount > 0.3) {
            throw new RuntimeException("resource odds mush less than 30%");
        }

        gameMap.resourceMap = new HashMap<>();
        List<Block> blockList = gameMap.mainRoad.blockList;
        for (TerrainConfig terrainConfig: terrainConfigMap.values()) {
            if (terrainConfig.resourceOdds > 0 && terrainConfig.resourceOdds < 1) {
                int count = (int) (blockList.size() * terrainConfig.resourceOdds);
                while(count > 0) {
                    Block startBlock = blockList.get(ThreadLocalRandom.current().nextInt(blockList.size()));
                    Block currentBlock = startBlock;
                    int size = ThreadLocalRandom.current().nextInt(3, 5);

                    // TODO 会在同样的路段重复生成资源，互相覆盖
                    boolean isValid = true;
                    for (int i = 0; i < size; i ++) {
                        if (currentBlock == null) {
                            isValid = false;
                            break;
                        }
                        currentBlock = currentBlock.next;
                    }

                    if (!isValid) continue;
                    currentBlock = startBlock;
                    for (int i = 0; i < size; i ++) {
                        int x = gameMap.getX(currentBlock.blockId);
                        int y = gameMap.getY(currentBlock.blockId);
                        if (!gameMap.isOccupied(gameMap.getBlockId(x + 1, y))) {
                            gameMap.addBlock(gameMap.getBlockId(x + 1, y), BlockType.BRANCH);
                        } else {
                            gameMap.addBlock(gameMap.getBlockId(x, y + 1), BlockType.BRANCH);
                        }
                        currentBlock = currentBlock.next;
                    }
                    count = count - size;
                }
            }
        }
    }


    public static void main(String[] args) {
        int i = 1;
        while(i -- > 0) {
            MarathonGameMapCreator marathonGameMap = new MarathonGameMapCreator();
            marathonGameMap.generate(20, 20, 49);
            //marathonGameMap.generate(40, 40, 13);
        }
    }
}

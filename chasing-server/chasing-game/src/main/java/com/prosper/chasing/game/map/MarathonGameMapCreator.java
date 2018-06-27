package com.prosper.chasing.game.map;

import com.prosper.chasing.common.util.Pair;
import com.prosper.chasing.game.base.Point2D;
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
            block.groupId = gameMap.ROAD_GROUP_ID;
        }
        gameMap.printMap();
        gameMap.printGroupId();

        generateTerrainForMainRoad(gameMap);
        generateTerrainForShortcuts(gameMap);

        twistMap(gameMap, 2);
        gameMap.printMap();
        gameMap.printGroupId();

        expandRoad(gameMap, 3);
        gameMap.printMap();
        //gameMap.printGroupId();
        //gameMap.printDistances();
        System.out.println(gameMap.TerrainTypeMapOfGroup);

        for (int i = 1; i <= gameMap.boundX * gameMap.boundY; i ++) {
            Block block = gameMap.occupiedBlockMap.get(i);
;            byte[] bytes = block.getBlockBytes();
            for (byte byteValue: bytes) {
                System.out.print(byteValue & 0xFF);
                System.out.print(",");
            }

            if (i % 100 == 0) {
                System.out.print("\n");
            }
        }
        System.out.print("\n");

        for (short i = 1; i <= gameMap.TerrainTypeMapOfGroup.size(); i ++) {
            System.out.print(gameMap.TerrainTypeMapOfGroup.get(i).getValue());
            System.out.print(",");
        }
        System.out.print("\n");

        return gameMap;
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

    private void generateTerrainForShortcutV2(
            GameMap gameMap, List<TerrainConfig> validTerrainConfigList, Branch branch) {
        int extraDistance = branch.getShort() / 8;
        int maxLength = (branch.blockList.size() + 1) / 8 - 1;
        List<Pair<TerrainType, Integer>> specialSegmentList = new LinkedList<>();

        List<TerrainConfig> copyList = new LinkedList<>();
        Collections.shuffle(validTerrainConfigList);

        int totalSegmentExtraLength = 0;
        int totalSegmentLength = 0;

        for (TerrainConfig terrainConfig: validTerrainConfigList) {
            for (int i = terrainConfig.maxLength; i >= terrainConfig.minLength; i--) {
                int estimateExtraDistance;
                if (terrainConfig.speedRate == 0) {
                    estimateExtraDistance = 4 * i;
                } else {
                    estimateExtraDistance = (int) Math.ceil ((float) i / terrainConfig.speedRate - i);
                }

                if (estimateExtraDistance + totalSegmentExtraLength >= extraDistance ||
                        i + totalSegmentLength > maxLength - specialSegmentList.size()) continue;
                else {
                    specialSegmentList.add(new Pair(terrainConfig.terrainType, i));
                    totalSegmentExtraLength += estimateExtraDistance;
                    totalSegmentLength += i;
                    break;
                }
            }
        }

        int normalSegDistance = maxLength - totalSegmentLength;
        setBranchV2(gameMap, specialSegmentList, branch, normalSegDistance, true);
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

    private void setBranch(Map<TerrainType, Integer> specialSegmentMap, Branch branch, int normalSegDistance) {
        int normalSegCount = specialSegmentMap.size() + 1;
        int normalSegTotalLen = normalSegDistance;
        int normalSegAverageLen = normalSegTotalLen / normalSegCount;
        int minNormalSegLen = normalSegAverageLen / 2;

        List<Integer> normalSegmentList = new ArrayList<>();
        for (int i = 0; i < normalSegCount / 2; i ++) {
            int length = ThreadLocalRandom.current().nextInt(minNormalSegLen, normalSegAverageLen);
            normalSegmentList.add(length);
            normalSegmentList.add(2 * normalSegAverageLen - length);
        }
        if (normalSegCount % 2 == 1) {
            normalSegmentList.add(normalSegAverageLen);
        }

        Block currentBlock = branch.blockList.get(0);
        for (Map.Entry<TerrainType, Integer> specialSegment: specialSegmentMap.entrySet()) {
            int count = specialSegment.getValue();
            for (int i = 0; i < count; i ++) {
                if (currentBlock == null) {
                    break;
                }
                currentBlock.terrainType = specialSegment.getKey();
                currentBlock = currentBlock.next;
            }

            int normalSegmentLenIndex = ThreadLocalRandom.current().nextInt(normalSegmentList.size());
            for (int i = 0; i < normalSegmentList.get(normalSegmentLenIndex); i ++) {
                if (currentBlock == null) {
                    break;
                }
                currentBlock = currentBlock.next;
            }
            normalSegmentList.remove(normalSegmentLenIndex);
        }
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
            gameMap.TerrainTypeMapOfGroup.put(groupId, specialSegment.getX());
            for (int i = 0; i < count; i ++) {
                if (currentBlock == null) {
                    break;
                }
                currentBlock.terrainType = specialSegment.getX();
                currentBlock.groupId = groupId;
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

    private void expandRoad(GameMap gameMap, int width) {
        Map<Integer, Pair<Integer, Short>> addBlockMap= new HashMap<>();
        for (int blockId: gameMap.unoccupiedBlockList) {
            Pair<Integer, Block> result = gameMap.getNearestBlockOfCross(
                    blockId, width, BlockType.ARTERY, BlockType.SHORTCUT, BlockType.BRANCH);

            if (result != null) {
                addBlockMap.put(blockId, new Pair<>(result.getX(), result.getY().groupId));
            }
            /*
            for (Block block: crossBlockList) {
                if (!addBlockMap.containsKey(blockId) ||
                        (addBlockMap.get(blockId)[0] == TerrainType.PAVEMENT && block.terrainType != TerrainType.PAVEMENT)) {
                    addBlockMap.put(blockId, new Object[]{block.terrainType, block.groupId});
                }
            }
            */
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
        for (int blockId: gameMap.unoccupiedBlockList) {
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
        for (int blockId: gameMap.unoccupiedBlockList) {
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
        for (int blockId: gameMap.unoccupiedBlockList) {
            blockList.add(blockId);
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L3, TerrainType.NONE, GameMap.MOUNTAIN_LEVEL_3_GROUP_ID);
        }

        gameMap.printMap();

        // 将边缘的三级山地及周边地块转成海
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
            block.type = BlockType.SEA_L2;
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
                block.type = BlockType.SEA_L2;
            }
        }

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
                List<Block> adjacentBlocks = gameMap.getAdjacentInDistance(block.blockId, 1, true, BlockType.MOUNTAIN_L3);
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
                    block.type = BlockType.SEA_L2;
                }
            }
        }

        Set<Block> seaLevel2BlockSets = new HashSet<>();
        for (Block seaLevel3Block: seaLevel3BlockSets) {
            List<Block> adjacentBlocks = gameMap.getAdjacent(seaLevel3Block.blockId, 2, true, BlockType.MOUNTAIN_L2);
            for(Block block: adjacentBlocks) {
                seaLevel2BlockSets.add(block);
                block.type = BlockType.SEA_L1;
            }
        }

        for (Block seaLevel2Block: seaLevel2BlockSets) {
            List<Block> blocks = gameMap.getAdjacent(seaLevel2Block.blockId, 1, true, BlockType.MOUNTAIN_L1);
            for(Block block: blocks) {
                block.type = BlockType.SEA_L1;
            }
        }

        /*
        for (Block seaLevel2Block: seaLevel2BlockSets) {
            for(Block block: gameMap.getAdjacent(seaLevel2Block.blockId, 1, true, BlockType.SEA_L2)) {
                block.type = BlockType.SEA_L1;
            }
            for(Block block: gameMap.getAdjacent(seaLevel2Block.blockId, 2, false, BlockType.SEA_L2)) {
                if (ThreadLocalRandom.current().nextInt(10) < 4) {
                    block.type = BlockType.SEA_L1;
                }
            }
        }
        */
    }

    private void addSurroundedBlock(
            GameMap gameMap, BlockType blockType, TerrainType terrainType, int width, BlockType... surroundedBlockTypes) {
        List<Integer> roadBlockList = new LinkedList<>();
        boolean clear = false;
        while (!clear) {
            for (int blockId: gameMap.unoccupiedBlockList) {
                if (gameMap.getAdjacentInDistance(blockId, 1, false, surroundedBlockTypes).size() >= 3) {
                    roadBlockList.add(blockId);
                }
            }

            if (roadBlockList.size() == 0) clear = true;
            else {
                for (int blockId: roadBlockList) {
                    gameMap.addBlock(blockId, blockType, terrainType, RoadDirection.NONE,
                            gameMap.ROAD_GROUP_ID, width + 1, -1);
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
        for (Block block: gameMap.mainRoad.blockList) {
            int nearDistance = nearTurning(block, turningSet, 8);
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


            RoadDirection roadDirection = gameMap.getRoadDirection(block.getBlockId(), BlockType.ARTERY);
            if (roadDirection == RoadDirection.HORIZONTAL) {
                if (offset != 0) {
                    Block newBlock = gameMap.addBlock(gameMap.getBlockId(block.position.x, block.position.y + offset),
                            block.type, block.terrainType, block.roadDirection, block.groupId,
                            block.distanceAwayFromRoad, block.distanceAwayFromRoadCrossPoint);
                    newBlock.next = block.next;
                    newBlock.previous = block.previous;
                    newBlock.distanceToFinish = block.distanceToFinish;

                    removedBlockSet.add(block.blockId);
                }
            } else if (roadDirection == RoadDirection.VERTICAL) {
                if (offset != 0) {
                    Block newBlock = gameMap.addBlock(gameMap.getBlockId(block.position.x + offset, block.position.y),
                            block.type, block.terrainType, block.roadDirection, block.groupId,
                            block.distanceAwayFromRoad, block.distanceAwayFromRoadCrossPoint);
                    newBlock.next = block.next;
                    newBlock.previous = block.previous;
                    newBlock.distanceToFinish = block.distanceToFinish;

                    removedBlockSet.add(block.blockId);
                }
            }
        }

        for (Branch branch: gameMap.shortcutList) {
            lastOffset = 0;
            moved = false;
            for (Block block: branch.blockList) {
                int nearDistance = nearTurning(block, turningSet, 8);
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
                                block.position.x, block.position.y + offset), block.type, block.terrainType, block.roadDirection,
                                block.groupId, block.distanceAwayFromRoad, block.distanceAwayFromRoadCrossPoint);
                        newBlock.next = block.next;
                        newBlock.previous = block.previous;
                        newBlock.distanceToFinish = block.distanceToFinish;

                        removedBlockSet.add(block.blockId);
                    } else if (roadDirection == RoadDirection.VERTICAL) {
                        Block newBlock = gameMap.addBlock(gameMap.getBlockId(
                                block.position.x + offset, block.position.y), block.type, block.terrainType, block.roadDirection,
                                block.groupId, block.distanceAwayFromRoad, block.distanceAwayFromRoadCrossPoint);
                        newBlock.next = block.next;
                        newBlock.previous = block.previous;
                        newBlock.distanceToFinish = block.distanceToFinish;

                        removedBlockSet.add(block.blockId);
                    }
                }
            }
        }

        for (int blockId: removedBlockSet) {
            gameMap.occupiedBlockMap.remove(blockId);
            gameMap.unoccupiedBlockList.add(blockId);
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

    private int nearTurning(Block block, Set<Integer> turningSet, int distance) {
        if (block!= null && turningSet.contains(block.blockId)) {
            return 0;
        }

        Block nearBlock = block.next;
        for (int i = 1; i <= distance; i ++) {
            if (nearBlock == null || turningSet.contains(nearBlock.blockId)) {
                return i;
            }
            nearBlock = nearBlock.next;
        }

        nearBlock = block.previous;
        for (int i = -1; i >= -distance; i --) {
            if (nearBlock == null || turningSet.contains(nearBlock.blockId)) {
                return i;
            }
            nearBlock = nearBlock.previous;
        }
        return Integer.MAX_VALUE;
    }

    /**
     * 修改交叉路口的地形保持一致
     */
    private void modifyCrossTerrain(GameMap gameMap) {
        BlockType[] blockTypes = new BlockType[] {BlockType.SHORTCUT, BlockType.ARTERY};
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (gameMap.getRoadDirection(block.blockId, blockTypes)
                    == RoadDirection.CROSS) {
                int x = block.position.x;
                int y = block.position.y;

                TerrainType adjacentTerrainType = TerrainType.PAVEMENT;
                for (int i = -1; i <= 1; i = i + 2) {
                    if (gameMap.isInBound(x + i, y)) {
                        Block adjacentBlock = gameMap.occupiedBlockMap.get(gameMap.getBlockId(x + i, y));
                        if (adjacentBlock != null && Util.arrayContains(blockTypes, adjacentBlock.type) &&
                                adjacentBlock.terrainType != TerrainType.PAVEMENT) {
                            adjacentTerrainType = adjacentBlock.terrainType;
                        }
                    }
                }

                for (int i = -1; i <= 1; i = i + 2) {
                    if (gameMap.isInBound(x, y + i)) {
                        Block adjacentBlock = gameMap.occupiedBlockMap.get(gameMap.getBlockId(x, y + i));
                        if (adjacentBlock != null && Util.arrayContains(blockTypes, adjacentBlock.type) &&
                                adjacentBlock.terrainType != TerrainType.PAVEMENT) {
                            adjacentTerrainType = adjacentBlock.terrainType;
                        }
                    }
                }

                if (adjacentTerrainType != TerrainType.PAVEMENT) {
                    for (int i = -4; i <= 4; i ++) {
                        if (gameMap.isInBound(x + i, y)) {
                            Block adjacentBlock = gameMap.occupiedBlockMap.get(gameMap.getBlockId(x + i, y));
                            if (adjacentBlock != null && Util.arrayContains(blockTypes, adjacentBlock.type) &&
                                    adjacentBlock.terrainType == TerrainType.PAVEMENT) {
                                adjacentBlock.terrainType = adjacentTerrainType;
                            }
                        }
                    }

                    for (int i = -4; i <= 4; i ++) {
                        if (gameMap.isInBound(x, y + i)) {
                            Block adjacentBlock = gameMap.occupiedBlockMap.get(gameMap.getBlockId(x, y + i));
                            if (adjacentBlock != null && Util.arrayContains(blockTypes, adjacentBlock.type) &&
                                    adjacentBlock.terrainType == TerrainType.PAVEMENT) {
                                adjacentBlock.terrainType = adjacentTerrainType;
                            }
                        }
                    }
                }
            }
        }
    }

    private void generateBuildingsV2(GameMap gameMap) {
        gameMap.buildingMap = new HashMap<>();
        int id = 1;
        for (Map.Entry<BuildingType, Byte> buildingConfig : buildingConfigMap.entrySet()) {
            BuildingType buildingType = buildingConfig.getKey();
            int count = buildingConfig.getValue();

            while (count > 0) {
                int blockId = gameMap.getRandomUnoccupiedBlockId();
                int width = ThreadLocalRandom.current().nextInt(5, 6);
                int height = ThreadLocalRandom.current().nextInt(5, 6);

                int x = gameMap.getX(blockId);
                int y = gameMap.getY(blockId);

                boolean isValid = true;
                outLoop:
                for (int i = 0; i < width; i ++) {
                    for (int j = 0; j < height; j ++) {
                        int buildingBlockId = gameMap.getBlockId(x + i, y + j);
                        if (gameMap.isOccupiedAround(buildingBlockId, 2)) {
                            isValid = false;
                            break outLoop;
                        }
                    }
                }

                if (isValid) {
                    List<Integer> borderPointList = gameMap.getBorderPoints(
                            gameMap.getX(blockId), gameMap.getY(blockId), width, height, false);

                    loop:
                    while (borderPointList != null && borderPointList.size() > 0) {
                        int borderBlockId = borderPointList.get(
                                ThreadLocalRandom.current().nextInt(borderPointList.size()));

                        int borderBlockX = gameMap.getX(borderBlockId);
                        int borderBlockY = gameMap.getY(borderBlockId);

                        for (int i = 2; i < 10; i ++) {
                            int tryBlockId;
                            if (borderBlockX == x) {
                                if (!gameMap.isInBound(borderBlockX - i, borderBlockY)) break loop;
                                tryBlockId = gameMap.getBlockId(borderBlockX - i, borderBlockY);
                            } else if (borderBlockY == y) {
                                if (!gameMap.isInBound(borderBlockX, borderBlockY - i)) break loop;
                                tryBlockId = gameMap.getBlockId(borderBlockX, borderBlockY - i);
                            } else if (borderBlockX == x + width) {
                                if (!gameMap.isInBound(borderBlockX + i, borderBlockY)) break loop;
                                tryBlockId = gameMap.getBlockId(borderBlockX + i, borderBlockY);
                            } else {
                                if (!gameMap.isInBound(borderBlockX, borderBlockY + i)) break loop;
                                tryBlockId = gameMap.getBlockId(borderBlockX, borderBlockY + i);
                            }

                            Block block = gameMap.occupiedBlockMap.get(tryBlockId);
                            if (block != null && (block.type == BlockType.ARTERY
                                    || block.type == BlockType.SHORTCUT)) {
                                for (int m = 0; m <= width; m ++) {
                                    for (int n = 0; n <= height; n ++) {
                                        int buildingBlockId = gameMap.getBlockId(x + m, y + n);
                                        gameMap.addBlock(buildingBlockId, BlockType.BRANCH, TerrainType.BUILDING);
                                    }
                                }
                                gameMap.buildingMap.put(id, new Building(id, buildingType, new Point2D(x + 2, y + 2)));
                                id ++;

                                gameMap.addBranch(borderBlockId, tryBlockId);
                                count --;
                                break loop;
                            } else if (block != null) {
                                break;
                            }
                        }
                        borderPointList.remove(new Integer(borderBlockId));
                    }
                }
            }
        }
    }

    private Point2D getFrameCenterPoint(GameMap gameMap, int width, int height, int minDistance, int maxDistance) {
        while(true) {
            int blockId = gameMap.getRandomUnoccupiedBlockId();

            int x = gameMap.getX(blockId);
            int y = gameMap.getY(blockId);
            int i = 0, j = 0;

            outLoop:
            for (; i < width; i++) {
                for (; j < height; j++) {
                    int viewBlockId = gameMap.getBlockId(x + i, y + j);
                    for (int distance = 1; distance <= maxDistance; distance ++) {
                        if (gameMap.isOccupiedAround(viewBlockId, distance)) {
                            if (distance < maxDistance) break outLoop;
                        }

                    }
                }
            }

            x = gameMap.getX(blockId);
            y = gameMap.getY(blockId);
            if (i == width && j == height) {
                for (int m = 0; m < width; m++) {
                    for (int n = 0; n < height; n++) {
                        int viewBlockId = gameMap.getBlockId(x + m, y + n);
                        gameMap.addBlock(viewBlockId, BlockType.BRANCH, TerrainType.VIEW);
                    }
                }
            }
        }
    }

    /**
     * 生成建筑物
     * @param gameMap
     */
    private void generateBuildingsV3(GameMap gameMap) {
        for (Map.Entry<BuildingType, Byte> buildingConfig : buildingConfigMap.entrySet()) {
            BuildingType buildingType = buildingConfig.getKey();
            int count = buildingConfig.getValue();

            for (int m = 0; m < count;) {
                int blockId;
                while(true) {
                    blockId = gameMap.getRandomUnoccupiedBlockId();
                    if (gameMap.isNear(blockId, 3, BlockType.ARTERY, BlockType.SHORTCUT)) {
                        break;
                    }
                }
                int width = 3;
                int height = 3;

                int x = gameMap.getX(blockId);
                int y = gameMap.getY(blockId);

                boolean isValid = true;
                outLoop:
                for (int i = 0; i < width; i ++) {
                    for (int j = 0; j < height; j ++) {
                        if (!gameMap.isInBound(x + i, y + j)) {
                            isValid = false;
                            break outLoop;
                        } else {
                            int buildingBlockId = gameMap.getBlockId(x + i, y + j);
                            if (gameMap.occupiedBlockMap.get(buildingBlockId) != null) {
                                isValid = false;
                                break outLoop;
                            }
                        }
                    }
                }

                if (isValid) {
                    for (int i = 0; i < width; i ++) {
                        for (int j = 0; j < height; j ++) {
                            gameMap.addBlock(gameMap.getBlockId(x + i, y + j),
                                    BlockType.ROAD_EXTENSION, TerrainType.BUILDING);
                        }
                    }
                    gameMap.addBuilding(buildingType, x + 1, y + 1, Orientation.FREE);
                    m ++;
                }
            }
        }
    }

    /**
     * 生成一些静态的物体
     */
    private void generateBuildings(GameMap gameMap) {
        gameMap.buildingMap = new HashMap<>();
        List<Block> blockList = gameMap.mainRoad.blockList;
        for (Map.Entry<BuildingType, Byte> buildingConfig : buildingConfigMap.entrySet()) {
            BuildingType buildingType = buildingConfig.getKey();
            int count = buildingConfig.getValue();

            while(count > 0) {
                Block startBlock = blockList.get(ThreadLocalRandom.current().nextInt(blockList.size()));
                Block currentBlock = startBlock;
                int size = GameMap.buildingBlockCount.get(buildingType);
                for (int i = 0; i < size; i ++) {
                    if (currentBlock == null || gameMap.isOccupied(currentBlock.blockId)) break;
                    currentBlock = currentBlock.next;
                }

                currentBlock = startBlock;
                for (int i = 0; i < size; i ++) {
                    gameMap.addBlock(currentBlock.blockId, BlockType.BRANCH, TerrainType.BUILDING);
                }

                count -= size;
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
                            gameMap.addBlock(gameMap.getBlockId(x + 1, y), BlockType.BRANCH, terrainConfig.terrainType);
                        } else {
                            gameMap.addBlock(gameMap.getBlockId(x, y + 1), BlockType.BRANCH, terrainConfig.terrainType);
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
            //marathonGameMap.generate(10, 10);
            marathonGameMap.generate(20, 20, 11);
        }
    }
}

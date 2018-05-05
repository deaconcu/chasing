package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;
import com.prosper.chasing.game.util.Constant;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2018/4/25.
 */
public class MarathonGameMapCreator {

    /**
     * 主路上需要增加的地形长度比例
     */
    public static int ROAD_EXTEND_PERCENT = 30;

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
    private static Map<Byte, TerrainConfig> terrainConfigMap = new HashMap<>();

    /**
     * 建筑物配置
     */
    private static Map<Byte, Byte> buildingConfigMap = new HashMap<>();


    static {

        /*****************
         * 地形配置       *
         *****************/

        /**
         * 快速通过
         */
        terrainConfigMap.put(Constant.TerrainType.ROAD, new TerrainConfig(
                // 铺装路面，存在于主路
                Constant.TerrainType.ROAD, new LocateType[]{LocateType.ROAD}));
        terrainConfigMap.put(Constant.TerrainType.VEGETABLE, new TerrainConfig(
                // 蔬菜地，存在于主路
                Constant.TerrainType.VEGETABLE, new LocateType[]{LocateType.ROAD}, 0.05f));

        /**
         * 不能通过，需要找资源才能通过
         */
        terrainConfigMap.put(Constant.TerrainType.LAVA, new TerrainConfig(
                // 岩浆：存在于支路
                Constant.TerrainType.LAVA, new LocateType[]{LocateType.SHORTCUT}, 1, 3, 0, 0));
        /* 水面废弃，用水井代替
        terrainConfigMap.put(Constant.TerrainType.WATER, new TerrainConfig(
                // 水面：存在于支路
                Constant.TerrainType.WATER, new LocateType[]{LocateType.SHORTCUT}, 1, 3, 0, 0, 0.05f));
         */
        terrainConfigMap.put(Constant.TerrainType.FOREST, new TerrainConfig(
                // 森林：存在于支路，也存在于主路，在主路上时，存在于道路两旁，提供木材；支路时，存在于道路中间
                Constant.TerrainType.FOREST, new LocateType[]{LocateType.SHORTCUT}, 1, 8, 0, 0, 0.05f));
        terrainConfigMap.put(Constant.TerrainType.ROCK, new TerrainConfig(
                // 石头地：存在于支路，也存在于主路，在主路上时，存在于道路两旁，提供石料；支路时，存在于道路中间
                Constant.TerrainType.ROCK, new LocateType[]{LocateType.SHORTCUT}, 1, 5, 0, 0, 0.05f));

        /**
         * 缓慢通过，停止运动后不会后退
         */
        terrainConfigMap.put(Constant.TerrainType.GRASS, new TerrainConfig(
                // 草地：存在于主路或者支路，速度降低10%，单段长度10-30
                Constant.TerrainType.GRASS, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 10, 30, 0.9f, 0));
        terrainConfigMap.put(Constant.TerrainType.WHEAT, new TerrainConfig(
                // 麦田：存在于主路和支路，速度降低30%，单端长度7-24
                Constant.TerrainType.WHEAT, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 7, 24, 0.7f, 0, 0.05f));
        terrainConfigMap.put(Constant.TerrainType.RAIN, new TerrainConfig(
                // 雨林：存在于主路和支路，速度降低30%，单端长度7-24
                Constant.TerrainType.RAIN, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 7, 24, 0.7f, 0));
        terrainConfigMap.put(Constant.TerrainType.SAND, new TerrainConfig(
                // 沙地：存在于主路和支路，速度降低30%，单端长度7-24
                Constant.TerrainType.SAND, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 7, 24, 0.7f, 0));
        terrainConfigMap.put(Constant.TerrainType.SWAMP, new TerrainConfig(
                // 沼泽：存在于主路和支路，速度降低70%，单端长度4-15
                Constant.TerrainType.SWAMP, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 4, 15, 0.3f, 0));

        /**
         * 缓慢通过，必须保持速度，否则后退或者返回某一个复活点
         */
        terrainConfigMap.put(Constant.TerrainType.SNOW, new TerrainConfig(
                // 雪地；存在于主路和支路，速度降低50%，单端长度5-19，规定时间通过，否则送回起点
                Constant.TerrainType.SNOW, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 5, 19, 0.5f, 0));
        terrainConfigMap.put(Constant.TerrainType.WIND, new TerrainConfig(
                // 大风地带：存在于主路和支路，速度降低40%，单端长度5-19
                Constant.TerrainType.WIND, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 5, 19, 0.6f, 0.2f));
        terrainConfigMap.put(Constant.TerrainType.WILDWIND, new TerrainConfig(
                // 飓风地带：存在于主路和支路，速度降低60%，单端长度5-19
                Constant.TerrainType.WILDWIND, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 5, 19, 0.6f, 0.2f));
        terrainConfigMap.put(Constant.TerrainType.ANIMAL, new TerrainConfig(
                // 有猛兽的地方, 存在于主路和支路，速度应该在平时的200%，保持一段时间 单段长度5-19
                Constant.TerrainType.ANIMAL, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 5, 19, 1, 0.5f));

        /**
         * 其他
         */
        terrainConfigMap.put(Constant.TerrainType.FOG, new TerrainConfig(
                // 浓雾：存在于主路和支路，随机地形，单端长度5-20
                Constant.TerrainType.FOG, new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 5, 20, 1, 0));


        /*****************
         * 建筑物配置       *
         *****************/

        buildingConfigMap.put(Constant.BuildingType.WAREHOUSE, (byte)5);
        buildingConfigMap.put(Constant.BuildingType.STORE, (byte)5);
        buildingConfigMap.put(Constant.BuildingType.TALL_TREE, (byte)5);
        buildingConfigMap.put(Constant.BuildingType.GRAVEYARD, (byte)5);
        buildingConfigMap.put(Constant.BuildingType.WELL, (byte)5);
        buildingConfigMap.put(Constant.BuildingType.JACKSTRAW, (byte)5);
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
        byte terrainId;

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

        // 奖励加成，比如要求保持速度的地形，实际跑步距离可以比分支距离加上缩短距离 少 10%
        float addOn;

        TerrainConfig(byte terrainId, LocateType[] locateTypes) {
            this.terrainId = terrainId;
            this.locateTypes = locateTypes;
        }

        TerrainConfig(byte terrainId, LocateType[] locateTypes, float resourceOdds) {
            this.terrainId = terrainId;
            this.locateTypes = locateTypes;
            this.resourceOdds = resourceOdds;
        }

        TerrainConfig(byte terrainId, LocateType[] locateTypes, int minLength, int maxLength, float speedRate, float addOn) {
            this.terrainId = terrainId;
            this.locateTypes = locateTypes;
            this.minLength = minLength;
            this.maxLength = maxLength;

            if (speedRate > 1) this.speedRate = 1;
            else if (speedRate < 0) this.speedRate = 0;
            else this.speedRate = speedRate;

            this.addOn = addOn;
            this.isResource = false;
        }

        TerrainConfig(byte terrainId, LocateType[] locateTypes, int minLength, int maxLength, float speedRate,
                      float addOn, float resourceOdds) {
            this(terrainId, locateTypes, minLength, maxLength, speedRate, addOn);
            this.resourceOdds = resourceOdds;
        }

    }

    /**
     * 给主路随机增加地形
     * 1.增加的长度不超过总长度的30%
     * 2.允许在主路上的地形随机分布，长度为最小长度和最大长度的随机值
     * 3.特殊地形之间有随机距离，长度为平均间隔距离的一半到1.5倍之间的随机值
     */
    public void generateTerrainForMainRoad(Branch mainRoad) {
        int specialSegTotalLen = mainRoad.blockList.size() * ROAD_EXTEND_PERCENT;

        List<TerrainConfig> validTerrainConfig = new ArrayList<>();
        for (TerrainConfig terrainConfig: terrainConfigMap.values()) {
            for (LocateType locateType: terrainConfig.locateTypes) {
                if (locateType == LocateType.ROAD) {
                    validTerrainConfig.add(terrainConfig);
                    continue;
                }
            }
        }

        Map<Byte, Integer> specialSegmentMap = new HashMap<>();
        while (specialSegTotalLen > 0) {
            TerrainConfig terrainConfig = validTerrainConfig.get(
                    ThreadLocalRandom.current().nextInt(validTerrainConfig.size()));
            int specialSegLen = ThreadLocalRandom.current().nextInt(
                    terrainConfig.minLength, terrainConfig.maxLength + 1);
            specialSegmentMap.put(terrainConfig.terrainId, specialSegLen);
            specialSegTotalLen -= specialSegLen;
        }

        setBranch(specialSegmentMap, mainRoad, mainRoad.blockList.size() - specialSegTotalLen);
    }

    /**
     * 给支路增加地形：
     * 1.支路距离加上地形距离应该稍少于路径节省距离，另外一半随机
     * 2.比较长的支路应该有多个特殊地形
     */
    public void generateTerrainForShortcuts(List<Branch> shortcutList) {
        List<TerrainConfig> validTerrainConfig = new ArrayList<>();
        for (TerrainConfig terrainConfig: terrainConfigMap.values()) {
            for (LocateType locateType: terrainConfig.locateTypes) {
                if (locateType == LocateType.SHORTCUT) {
                    validTerrainConfig.add(terrainConfig);
                }
            }
        }

        for (Branch branch: shortcutList) {
            generateTerrainForShortcut(validTerrainConfig, branch);
        }
    }

    private void generateTerrainForShortcut(List<TerrainConfig> validTerrainConfigList, Branch branch) {
        int distance = branch.getShort();
        Map<Byte, Integer> specialSegmentMap = new HashMap<>();

        int totalSegmentLength = 0;
        while(totalSegmentLength < distance / 2) {
            TerrainConfig terrainConfig = validTerrainConfigList.get(
                    ThreadLocalRandom.current().nextInt(validTerrainConfigList.size()));

            for (int i = terrainConfig.maxLength; i >= terrainConfig.minLength; i--) {
                int length = 0;
                if (terrainConfig.speedRate == 0) {
                    length = 10 * i;
                } else {
                    length = (int) ((float) i / terrainConfig.speedRate - 1);
                }
                if (length + totalSegmentLength > distance) continue;
                else {
                    specialSegmentMap.put(terrainConfig.terrainId, length);
                    totalSegmentLength += length;
                    distance -= length;
                }
            }
        }
        setBranch(specialSegmentMap, branch, branch.getShort());
    }

    private void setBranch(Map<Byte, Integer> specialSegmentMap, Branch branch, int normalSegDistance) {
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
        for (Map.Entry<Byte, Integer> specialSegment: specialSegmentMap.entrySet()) {

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


    public GameMap generate(int boundX, int boundY) {
        // generate 1
        //GameMapCreator gameMapCreator = new GameMapCreator(50, 50);
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
            //gameMapCreatorForMerge.printTerrainBlocks();

            gameMapCreator.merge(gameMapCreatorForMerge);
        }
        gameMapCreator.printTerrainBlocks(3);

        GameMap gameMap = gameMapCreator.expand(6);
        gameMap.printMap();

        generateTerrainForMainRoad(gameMap.mainRoad);
        gameMap.printMap();
        generateTerrainForShortcuts(gameMap.shortcutList);
        gameMap.printMap();

        generateResource(gameMap);
        gameMap.printMap();
        generateBuildingsV2(gameMap);
        gameMap.printMap();
        generateView(gameMap);
        gameMap.printMap();

        return gameMap;
    }

    /**
     * 生成景观
     * @param gameMap
     */
    private void generateView(GameMap gameMap) {
        int count = (int) (gameMap.unoccupiedBlockList.size() * VIEW_RATE);
        while (count > 0) {
            int blockId = gameMap.getRandomUnoccupiedBlockId();
            int width = ThreadLocalRandom.current().nextInt(1, 5);
            int height = ThreadLocalRandom.current().nextInt(1, 5);

            int x = gameMap.getX(blockId);
            int y = gameMap.getY(blockId);

            boolean isValid = true;
            outLoop:
            for (int i = 0; i <= width; i ++) {
                for (int j = 0; j <= height; j ++) {
                    int viewBlockId = gameMap.getBlockId(x + i, y + j);
                    if (gameMap.isOccupiedAround(viewBlockId, 2)) {
                        isValid = false;
                        break outLoop;
                    }
                }
            }

            x = gameMap.getX(blockId);
            y = gameMap.getY(blockId);
            if (isValid) {
                for (int m = 0; m <= width; m ++) {
                    for (int n = 0; n <= height; n ++) {
                        int viewBlockId = gameMap.getBlockId(x + m, y + n);
                        gameMap.addBlock(viewBlockId, Constant.MapBlockType.BRANCH, Constant.TerrainType.FOREST, (byte)0);
                    }
                }
            }
            count -= (width - 1) * (height - 1);
        }
    }

    private void generateBuildingsV2(GameMap gameMap) {
        gameMap.buildingMap = new HashMap<>();
        int id = 1;
        for (Map.Entry<Byte, Byte> buildingConfig : buildingConfigMap.entrySet()) {
            byte typeId = buildingConfig.getKey();
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
                    List<Integer> borderPointList = gameMap.getBorderPoints(blockId, width, height);

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
                            if (block != null && (block.type == Constant.MapBlockType.MAIN_ROAD
                                    || block.type == Constant.MapBlockType.SHORTCUT)) {
                                for (int m = 0; m <= width; m ++) {
                                    for (int n = 0; n <= height; n ++) {
                                        int buildingBlockId = gameMap.getBlockId(x + m, y + n);
                                        gameMap.addBlock(buildingBlockId, Constant.MapBlockType.BRANCH,
                                                Constant.TerrainType.BUILDING, (byte)0);
                                    }
                                }
                                gameMap.buildingMap.put(id, new Building(id, typeId, new Point2D(x + 2, y + 2)));
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
                        gameMap.addBlock(viewBlockId, Constant.MapBlockType.BRANCH, Constant.TerrainType.VIEW, (byte) 0);
                    }
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
        for (Map.Entry<Byte, Byte> buildingConfig : buildingConfigMap.entrySet()) {
            byte id = buildingConfig.getKey();
            int count = buildingConfig.getValue();

            while(count > 0) {
                Block startBlock = blockList.get(ThreadLocalRandom.current().nextInt(blockList.size()));
                Block currentBlock = startBlock;
                int size = GameMap.buildingBlockCount.get(id);
                for (int i = 0; i < size; i ++) {
                    if (currentBlock == null || gameMap.isOccupied(currentBlock.blockId)) break;
                    currentBlock = currentBlock.next;
                }

                currentBlock = startBlock;
                for (int i = 0; i < size; i ++) {
                    gameMap.addBlock(currentBlock.blockId,
                            Constant.MapBlockType.BRANCH, Constant.TerrainType.BUILDING, id);
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
                            gameMap.addBlock(gameMap.getBlockId(x + 1, y),
                                    Constant.MapBlockType.BRANCH, terrainConfig.terrainId, (byte)0);
                        } else {
                            gameMap.addBlock(gameMap.getBlockId(x, y + 1),
                                    Constant.MapBlockType.BRANCH, terrainConfig.terrainId, (byte)0);
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
            marathonGameMap.generate(20, 20);
        }
    }
}

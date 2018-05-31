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

    /**
     * 给主路随机增加地形
     * 1.增加的长度不超过总长度的30%
     * 2.允许在主路上的地形随机分布，长度为最小长度和最大长度的随机值
     * 3.特殊地形之间有随机距离，长度为平均间隔距离的一半到1.5倍之间的随机值
     */
    public void generateTerrainForMainRoad(Branch mainRoad) {
        int specialSegTotalLen = (int) (mainRoad.blockList.size() * ROAD_EXTEND_PERCENT);

        List<TerrainConfig> validTerrainConfig = new ArrayList<>();
        for (TerrainConfig terrainConfig: terrainConfigMap.values()) {
            for (LocateType locateType: terrainConfig.locateTypes) {
                if (locateType == LocateType.ROAD) {
                    validTerrainConfig.add(terrainConfig);
                    continue;
                }
            }
        }

        Map<TerrainType, Integer> specialSegmentMap = new HashMap<>();
        while (specialSegTotalLen > 0) {
            TerrainConfig terrainConfig = validTerrainConfig.get(
                    ThreadLocalRandom.current().nextInt(validTerrainConfig.size()));
            int specialSegLen = ThreadLocalRandom.current().nextInt(
                    terrainConfig.minLength, terrainConfig.maxLength + 1);
            specialSegmentMap.put(terrainConfig.terrainType, specialSegLen);
            specialSegTotalLen -= specialSegLen;
        }

        setBranch(specialSegmentMap, mainRoad, mainRoad.blockList.size() - specialSegTotalLen);
    }

    /**
     * 给主路随机增加地形
     * 1.增加的长度不超过总长度的30%
     * 2.允许在主路上的地形随机分布，长度为最小长度和最大长度的随机值
     * 3.特殊地形之间有随机距离，长度为平均间隔距离的一半到1.5倍之间的随机值
     */
    public void generateTerrainForMainRoadV2(GameMap gameMap) {
        Branch mainRoad = gameMap.mainRoad;
        int specialSegTotalLen = (int) (mainRoad.blockList.size() * ROAD_EXTEND_PERCENT / 8);
        System.out.println("special terrain: " + specialSegTotalLen);

        List<TerrainConfig> validTerrainConfig = new ArrayList<>();
        for (TerrainConfig terrainConfig: terrainConfigMap.values()) {
            for (LocateType locateType: terrainConfig.locateTypes) {
                if (locateType == LocateType.ROAD && terrainConfig.terrainType != TerrainType.PAVEMENT) {
                    validTerrainConfig.add(terrainConfig);
                    continue;
                }
            }
        }

        List<Pair<TerrainType, Integer>> specialSegmentList = new LinkedList<>();
        while (specialSegTotalLen > 0) {
            TerrainConfig terrainConfig = validTerrainConfig.get(
                    ThreadLocalRandom.current().nextInt(validTerrainConfig.size()));
            int specialSegLen = ThreadLocalRandom.current().nextInt(
                    terrainConfig.minLength, terrainConfig.maxLength + 1);
            specialSegmentList.add(new Pair(terrainConfig.terrainType, specialSegLen));
            specialSegTotalLen -= specialSegLen;
        }

        int normalSegTotalLen = (int) (mainRoad.blockList.size() * (1 - ROAD_EXTEND_PERCENT) / 8);
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
        int distance = branch.getShort() / 8;
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
                        length = branch.size() / 8 - 1;
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

        System.out.println("normal terrain:" + normalSegDistance);

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


            /*
            int normalSegAverageLen = normalSegTotalLen / normalSegCount;
            int minNormalSegLen = normalSegAverageLen / 2;

            for (int i = 0; i < normalSegCount / 2; i ++) {
                int length = ThreadLocalRandom.current().nextInt(minNormalSegLen, normalSegAverageLen);
                normalSegmentList.add(length);
                normalSegmentList.add(2 * normalSegAverageLen - length);
            }
            if (normalSegCount % 2 == 1) {
                normalSegmentList.add(normalSegAverageLen);
            }
            System.out.println("normal terrain: " + normalSegmentList);
            */
        }

        Block currentBlock = branch.blockList.get(0);
        while (currentBlock != null &&
                ((currentBlock.position.x + 1) % 8 != 4 && (currentBlock.position.y + 1) % 8 != 4)) {
            currentBlock = currentBlock.next;
        }

        int lastTerrain = 0;
        for (Pair<TerrainType, Integer> specialSegment: specialSegmentList) {
            int count = specialSegment.getY() * 8;
            for (int i = 0; i < count; i ++) {
                if (currentBlock == null) {
                    break;
                }
                if (i == 0 && lastTerrain != 1) {
                    RoadDirection roadDirection = gameMap.getRoadDirection(
                            currentBlock.blockId, BlockType.MAIN_ROAD, BlockType.BRANCH.SHORTCUT);
                    if (roadDirection == RoadDirection.VERTICAL) {
                        gameMap.addBuilding(BuildingType.GATE, currentBlock.position.x,
                                currentBlock.position.y, Orientation.NORTH);
                    } else if (roadDirection == RoadDirection.HORIZONTAL) {
                        gameMap.addBuilding(BuildingType.GATE, currentBlock.position.x,
                                currentBlock.position.y, Orientation.WEST);
                    } else {
                        throw new RuntimeException("unexpected road direction");
                    }
                    lastTerrain = 1;
                } else {
                    currentBlock.terrainType = specialSegment.getX();
                }
                currentBlock = currentBlock.next;
            }
            if (currentBlock != null) {
                System.out.println("special terrain count: " + count);
            }

            if (normalSegmentList.size() > 0) {
                int normalSegmentLenIndex = ThreadLocalRandom.current().nextInt(normalSegmentList.size());
                for (int i = 0; i < normalSegmentList.get(normalSegmentLenIndex) * 8; i++) {
                    if (currentBlock == null) {
                        break;
                    }
                    if (i == 0 && lastTerrain != 0) {
                        RoadDirection roadDirection = gameMap.getRoadDirection(
                                currentBlock.blockId, BlockType.MAIN_ROAD, BlockType.BRANCH.SHORTCUT);
                        if (roadDirection == RoadDirection.VERTICAL) {
                            gameMap.addBuilding(BuildingType.GATE, currentBlock.position.x,
                                    currentBlock.position.y, Orientation.NORTH);
                        } else if (roadDirection == RoadDirection.HORIZONTAL) {
                            gameMap.addBuilding(BuildingType.GATE, currentBlock.position.x,
                                    currentBlock.position.y, Orientation.WEST);
                        } else {
                            throw new RuntimeException("unexpected road direction");
                        }
                        lastTerrain = 0;
                    }
                    currentBlock = currentBlock.next;
                }
                if (currentBlock != null) {
                    System.out.println("normal terrain count: " + normalSegmentList.get(normalSegmentLenIndex) * 8);
                }
                normalSegmentList.remove(normalSegmentLenIndex);
            }
        }
        if (currentBlock != null) {
            RoadDirection roadDirection = gameMap.getRoadDirection(
                    currentBlock.blockId, BlockType.MAIN_ROAD, BlockType.BRANCH.SHORTCUT);
            if (roadDirection == RoadDirection.VERTICAL) {
                gameMap.addBuilding(BuildingType.GATE, currentBlock.position.x,
                        currentBlock.position.y, Orientation.NORTH);
            } else if (roadDirection == RoadDirection.HORIZONTAL) {
                gameMap.addBuilding(BuildingType.GATE, currentBlock.position.x,
                        currentBlock.position.y, Orientation.WEST);
            } else {
                throw new RuntimeException("unexpected road direction");
            }
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


        /*
        RoadMap gameMap = gameMapCreator.expand(6);
        gameMap.printMap();

        generateTerrainForMainRoad(gameMap.mainRoad);
        gameMap.printMap();
        generateTerrainForShortcuts(gameMap);
        gameMap.printMap();

        generateResource(gameMap);
        gameMap.printMap();
        generateBuildingsV2(gameMap);
        gameMap.printMap();
        generateView(gameMap);
        gameMap.printMap();

        return gameMap;
        */

        /*
        RoadMap gameMap = gameMapCreator.expandWithBorder(7);
        //gameMap.printMap();

        generateWall(gameMap);
        generateTerrainForMainRoadV2(gameMap);
        generateTerrainForShortcuts(gameMap);
        modifyCrossTerrain(gameMap);
        generateGateWall(gameMap);
        generateBuildingsV3(gameMap);
        generateViewV2(gameMap);
        gameMap.printMap();

        return gameMap;
        */

        GameMap gameMap = gameMapCreator.expandWithBorder(9);
        gameMap.printMap();

        twistMap(gameMap, 2);
        gameMap.printMap();

        expandRoadV2(gameMap);
        gameMap.printMap();

        for (int i = 1; i <= gameMap.boundX * gameMap.boundY; i ++) {
            Block block = gameMap.occupiedBlockMap.get(i);
            System.out.print(block.type.getValue());
            System.out.print(",");
            if (i % 100 == 0) {
                System.out.print("\n");
            }
        }
        System.out.print("\n");

        return gameMap;
    }

    private void expandRoadV2(GameMap gameMap) {
        // 扩展道路为基础路线块旁各2个方格
        Set<Integer> addBlockSet = new HashSet<>();
        for (int blockId: gameMap.unoccupiedBlockList) {
            if (gameMap.getBlocksInRangeOfCross(blockId, 2, BlockType.MAIN_ROAD, BlockType.SHORTCUT).size() > 0) {
                addBlockSet.add(blockId);
            }
        }

        for (int blockId: addBlockSet) {
            gameMap.addBlock(blockId, BlockType.PAVEMENT, TerrainType.PAVEMENT);
        }

        // 将三面被道路包围的空白块添加为道路块
        addSurroundedBlock(gameMap, BlockType.PAVEMENT, TerrainType.PAVEMENT, BlockType.PAVEMENT);
        gameMap.printMap();

        // 添加紧邻道路的块为一级山地，宽度为1格
        List<Integer> blockList = new LinkedList<>();
        for (int blockId: gameMap.unoccupiedBlockList) {
            if (gameMap.getBlocksInRange(blockId, 1, BlockType.PAVEMENT).size() > 0) {
                blockList.add(blockId);
            }
            if (gameMap.getBlocksInRangeOfCross(blockId, 2, BlockType.PAVEMENT).size() > 0) {
                if (ThreadLocalRandom.current().nextInt(10) < 3) {
                    blockList.add(blockId);
                }
            }
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L2, TerrainType.NONE);
        }

        // 添加紧邻一级山地的块为二级山地，宽度为1格
        blockList.clear();
        for (int blockId: gameMap.unoccupiedBlockList) {
            if (gameMap.getBlocksInRange(blockId, 1, BlockType.MOUNTAIN_L2).size() > 0) {
                blockList.add(blockId);
            }
            if (gameMap.getBlocksInRangeOfCross(blockId, 2, BlockType.MOUNTAIN_L2).size() > 0) {
                if (ThreadLocalRandom.current().nextInt(10) < 3) {
                    blockList.add(blockId);
                }
            }
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L4, TerrainType.NONE);
        }

        // 添加其余的块为三级山地
        blockList.clear();
        for (int blockId: gameMap.unoccupiedBlockList) {
            blockList.add(blockId);
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L6, TerrainType.NONE);
        }
    }

    private void expandRoad(GameMap gameMap) {
        // 扩展道路
        Set<Integer> addBlockSet = new HashSet<>();
        for (int blockId: gameMap.unoccupiedBlockList) {
            if (gameMap.getBlocksInRangeOfCross(blockId, 2, BlockType.MAIN_ROAD, BlockType.SHORTCUT).size() > 0) {
                addBlockSet.add(blockId);
            }
        }

        for (int blockId: addBlockSet) {
            gameMap.addBlock(blockId, BlockType.PAVEMENT, TerrainType.PAVEMENT);
        }

        // 将三面被道路包围的空白块添加为道路块
        addSurroundedBlock(gameMap, BlockType.PAVEMENT, TerrainType.PAVEMENT, BlockType.MAIN_ROAD, BlockType.SHORTCUT);
        gameMap.printMap();

        // 添加紧邻道路的块为一级山地
        List<Integer> blockList = new LinkedList<>();
        for (int blockId: gameMap.unoccupiedBlockList) {
            if (gameMap.getBlocksInRange(blockId, 2, BlockType.PAVEMENT).size() > 0) {
                blockList.add(blockId);
            }
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L2, TerrainType.NONE);
        }

        /*
        addSurroundedBlock(gameMap, BlockType.OBSTACLE, TerrainType.MOUNTAIN_L2, BlockType.OBSTACLE);
        gameMap.printMap();
        */

        // 添加紧邻一级山地的块为二级山地
        blockList.clear();
        for (int blockId: gameMap.unoccupiedBlockList) {
            if (gameMap.getBlocksInRange(blockId, 2, BlockType.MOUNTAIN_L2).size() > 0) {
                blockList.add(blockId);
            }
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L4, TerrainType.NONE);
        }

        /*
        addSurroundedBlock(gameMap, BlockType.OBSTACLE, TerrainType.MOUNTAIN_L4, BlockType.OBSTACLE);
        gameMap.printMap();
        */

        // 添加其余的块为三级山地
        blockList.clear();
        for (int blockId: gameMap.unoccupiedBlockList) {
            blockList.add(blockId);
        }
        for (int blockId: blockList) {
            gameMap.addBlock(blockId, BlockType.MOUNTAIN_L6, TerrainType.NONE);
        }

        gameMap.printMap();

        // 标记斜坡
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.PAVEMENT) {
                List<Block> crossList = gameMap.getBlocksInRangeOfCross(block.blockId, 1, BlockType.MOUNTAIN_L2);
                if (crossList.size() > 0) {
                    block.type = BlockType.SLOPE;
                    block.terrainType = TerrainType.NONE;
                    continue;
                }

                List<Block> cornerList = gameMap.getBlocksInRangeOfCorner(block.blockId, 1, BlockType.MOUNTAIN_L2);
                if (cornerList.size() == 1) {
                    Direction direction = gameMap.getDirection(block.blockId, cornerList.get(0).blockId);
                    if (direction == Direction.UP_LEFT) {
                        block.type = BlockType.SLASH_MOUNTAIN_L1_ROAD;
                    } else if (direction == Direction.UP_RIGHT) {
                        block.type = BlockType.BACKSLASH_ROAD_MOUNTAIN_L1;
                    } else if (direction == Direction.DOWN_LEFT) {
                        block.type = BlockType.BACKSLASH_MOUNTAIN_L1_ROAD;
                    } else if (direction == Direction.DOWN_RIGHT) {
                        block.type = BlockType.SLASH_ROAD_MOUNTAIN_L1;
                    }
                    block.terrainType = TerrainType.NONE;
                }
            } else if (block.type == BlockType.MOUNTAIN_L2) {
                List<Block> crossList = gameMap.getBlocksInRangeOfCross(block.blockId, 1, BlockType.MOUNTAIN_L4);
                if (crossList.size() > 0) {
                    block.type = BlockType.SLOPE;
                    block.terrainType = TerrainType.NONE;
                    continue;
                }

                List<Block> cornerList = gameMap.getBlocksInRangeOfCorner(block.blockId, 1, BlockType.MOUNTAIN_L4);
                if (cornerList.size() == 1) {
                    Direction direction = gameMap.getDirection(block.blockId, cornerList.get(0).blockId);
                    if (direction == Direction.UP_LEFT) {
                        block.type = BlockType.SLASH_MOUNTAIN_L2_MOUNTAIN_L1;
                    } else if (direction == Direction.UP_RIGHT) {
                        block.type = BlockType.BACKSLASH_MOUNTAIN_L1_MOUNTAIN_L2;
                    } else if (direction == Direction.DOWN_LEFT) {
                        block.type = BlockType.BACKSLASH_MOUNTAIN_L2_MOUNTAIN_L1;
                    } else if (direction == Direction.DOWN_RIGHT) {
                        block.type = BlockType.SLASH_MOUNTAIN_L1_MOUNTAIN_L2;
                    }
                    block.terrainType = TerrainType.NONE;
                }
            } else if (block.type == BlockType.MOUNTAIN_L4) {
                List<Block> crossList = gameMap.getBlocksInRangeOfCross(block.blockId, 1, BlockType.MOUNTAIN_L6);
                if (crossList.size() > 0) {
                    block.type = BlockType.SLOPE;
                    block.terrainType = TerrainType.NONE;
                    continue;
                }

                List<Block> cornerList = gameMap.getBlocksInRangeOfCorner(block.blockId, 1, BlockType.MOUNTAIN_L6);
                if (cornerList.size() == 1) {
                    Direction direction = gameMap.getDirection(block.blockId, cornerList.get(0).blockId);
                    if (direction == Direction.UP_LEFT) {
                        block.type = BlockType.SLASH_MOUNTAIN_L3_MOUNTAIN_L2;
                    } else if (direction == Direction.UP_RIGHT) {
                        block.type = BlockType.BACKSLASH_MOUNTAIN_L2_MOUNTAIN_L3;
                    } else if (direction == Direction.DOWN_LEFT) {
                        block.type = BlockType.BACKSLASH_MOUNTAIN_L3_MOUNTAIN_L2;
                    } else if (direction == Direction.DOWN_RIGHT) {
                        block.type = BlockType.SLASH_MOUNTAIN_L2_MOUNTAIN_L3;
                    }
                    block.terrainType = TerrainType.NONE;
                }
            }
        }
    }

    private BlockType getAdjacentType(int blockId, int distance, BlockType... blockTypes) {
        return null;
    }

    private void addSurroundedBlock(
            GameMap gameMap, BlockType blockType, TerrainType terrainType, BlockType... surroundedBlockTypes) {
        List<Integer> roadBlockList = new LinkedList<>();
        boolean clear = false;
        while (!clear) {
            for (int blockId: gameMap.unoccupiedBlockList) {
                if (gameMap.getAdjacent(blockId, 1, false, surroundedBlockTypes).size() >= 3) {
                    roadBlockList.add(blockId);
                }
            }

            if (roadBlockList.size() == 0) clear = true;
            else {
                for (int blockId: roadBlockList) {
                    gameMap.addBlock(blockId, blockType, terrainType);
                }
            }
            roadBlockList.clear();
        }
    }

    private void twistMap(GameMap gameMap, int maxOffset) {
        Set<Integer> turningSet = new HashSet<>();
        for (Block block: gameMap.occupiedBlockMap.values()) {
            if (block.type == BlockType.MAIN_ROAD || block.type == BlockType.SHORTCUT) {
                RoadDirection roadDirection = gameMap.getRoadDirection(
                        block.getBlockId(), BlockType.MAIN_ROAD, BlockType.SHORTCUT);
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


            RoadDirection roadDirection = gameMap.getRoadDirection(block.getBlockId(), BlockType.MAIN_ROAD);
            if (roadDirection == RoadDirection.HORIZONTAL) {
                if (offset != 0) {
                    Block newBlock = gameMap.addBlock(gameMap.getBlockId(
                            block.position.x, block.position.y + offset), block.type, block.terrainType);
                    newBlock.next = block.next;
                    newBlock.previous = block.previous;
                    newBlock.distanceToFinish = block.distanceToFinish;

                    removedBlockSet.add(block.blockId);
                }
            } else if (roadDirection == RoadDirection.VERTICAL) {
                if (offset != 0) {
                    Block newBlock = gameMap.addBlock(gameMap.getBlockId(
                            block.position.x + offset, block.position.y), block.type, block.terrainType);
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
                                block.position.x, block.position.y + offset), block.type, block.terrainType);
                        newBlock.next = block.next;
                        newBlock.previous = block.previous;
                        newBlock.distanceToFinish = block.distanceToFinish;

                        removedBlockSet.add(block.blockId);
                    } else if (roadDirection == RoadDirection.VERTICAL) {
                        Block newBlock = gameMap.addBlock(gameMap.getBlockId(
                                block.position.x + offset, block.position.y), block.type, block.terrainType);
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

    private void generateWallV2(GameMap gameMap) {
        List<Integer> wallBlockIdList = new LinkedList<>();
        for (int blockId: gameMap.unoccupiedBlockList) {
            if ((gameMap.isNear(blockId, 4, BlockType.MAIN_ROAD, BlockType.SHORTCUT) ||
                    gameMap.isNear(blockId, 4, BlockType.MAIN_ROAD, BlockType.SHORTCUT)) &&
                    !gameMap.isNear(blockId, 3, BlockType.MAIN_ROAD, BlockType.SHORTCUT) &&
                    !gameMap.isNear(blockId, 2, BlockType.MAIN_ROAD, BlockType.SHORTCUT) &&
                    !gameMap.isNear(blockId, 1, BlockType.MAIN_ROAD, BlockType.SHORTCUT)) {
                wallBlockIdList.add(blockId);
            }
        }

        for (int blockId: wallBlockIdList) {
            gameMap.addBlock(blockId, BlockType.WALL, TerrainType.WALL);
        }
    }

    /**
     * 生成景观
     */
    private void generateViewV2(GameMap gameMap) {

        // 生成城堡

        // 生成箭楼
        float rotundaRate = 0.2f;
        List<Building> towerBList = new LinkedList<>();
        for (Building building: gameMap.buildingMap.values()) {
            if (building.buildingType == BuildingType.TOWER_B) {
                towerBList.add(building);
            }
        }

        int rotundaCount = (int) (towerBList.size() * rotundaRate);
        for (int i = 0; i < rotundaCount;) {
            Building building = towerBList.get(ThreadLocalRandom.current().nextInt(towerBList.size()));
            int x = building.point2D.x;
            int y = building.point2D.y;

            int towerBBlockId = gameMap.getBlockId(x, y);
            RoadDirection roadDirection = gameMap.getRoadDirection(towerBBlockId, BlockType.WALL);
            if (roadDirection == RoadDirection.HORIZONTAL) {
                Building buildingNear = gameMap.buildingMap.get(gameMap.getBlockId(x, y - 8));
                Block block = gameMap.occupiedBlockMap.get(gameMap.getBlockId(x, y - 4));
                if (block != null && (block.type == BlockType.MAIN_ROAD || block.type == BlockType.SHORTCUT) &&
                        buildingNear != null && buildingNear.buildingType == BuildingType.TOWER_B) {
                    gameMap.addBuilding(BuildingType.ROTUNDA, x, y, Orientation.SOUTH);
                    i ++;
                    continue;
                }
                buildingNear = gameMap.buildingMap.get(gameMap.getBlockId(x, y + 8));
                block = gameMap.occupiedBlockMap.get(gameMap.getBlockId(x, y + 4));
                if (block != null && (block.type == BlockType.MAIN_ROAD || block.type == BlockType.SHORTCUT) &&
                        buildingNear != null && buildingNear.buildingType == BuildingType.TOWER_B) {
                    gameMap.addBuilding(BuildingType.ROTUNDA, x, y, Orientation.NORTH);
                    i ++;
                    continue;
                }
            } else if (roadDirection == RoadDirection.VERTICAL) {
                Building buildingNear = gameMap.buildingMap.get(gameMap.getBlockId(x - 8, y));
                Block block = gameMap.occupiedBlockMap.get(gameMap.getBlockId(x - 4, y));
                if (block != null && (block.type == BlockType.MAIN_ROAD || block.type == BlockType.SHORTCUT) &&
                        buildingNear != null && buildingNear.buildingType == BuildingType.TOWER_B) {
                    gameMap.addBuilding(BuildingType.ROTUNDA, x, y, Orientation.WEST);
                    i ++;
                    continue;
                }
                buildingNear = gameMap.buildingMap.get(gameMap.getBlockId(x + 8, y));
                block = gameMap.occupiedBlockMap.get(gameMap.getBlockId(x + 4, y));
                if (block != null && (block.type == BlockType.MAIN_ROAD || block.type == BlockType.SHORTCUT) &&
                        buildingNear != null && buildingNear.buildingType == BuildingType.TOWER_B) {
                    gameMap.addBuilding(BuildingType.ROTUNDA, x, y, Orientation.EAST);
                    i ++;
                    continue;
                }
            }
        }

        // 生成树林，石头地等
        int total = gameMap.unoccupiedBlockList.size();
        for (Map.Entry<TerrainType, Float> entry : viewConfigMap.entrySet()) {
            int count = (int) (entry.getValue() * total);
            if (count == 0) continue;

            for (int i = 0; i < count; i ++) {
                int blockId = gameMap.getRandomUnoccupiedBlockId();
                gameMap.addBlock(blockId, BlockType.OBSTACLE, entry.getKey());
            }
        }
    }

    /**
     * 生成城门和城门旁边的墙
     * @param gameMap
     */
    private void generateGateWall(GameMap gameMap) {
        List<Building> gateBuildingList = new LinkedList<>();
        for (Building building: gameMap.buildingMap.values()) {
            if (building.buildingType == BuildingType.GATE) {
                gateBuildingList.add(building);
            }
        }

        for (Building building: gateBuildingList) {
            int x = building.point2D.x;
            int y = building.point2D.y;

            int blockId = gameMap.getBlockId(x, y);
            RoadDirection roadDirection =
                    gameMap.getRoadDirection(blockId, BlockType.SHORTCUT, BlockType.MAIN_ROAD);

            if (roadDirection == RoadDirection.VERTICAL) {
                for (int i = -3; i <= 3; i ++) {
                    int adjacentBlockId = gameMap.getBlockId(x + i, y);

                    if (i == 0) {
                        continue;
                    } else {
                        gameMap.addBlock(adjacentBlockId, BlockType.WALL, TerrainType.WALL);
                    }
                }
            }

            if (roadDirection == RoadDirection.HORIZONTAL) {
                for (int i = -3; i <= 3; i ++) {
                    int adjacentBlockId = gameMap.getBlockId(x, y + i);

                    if (i == 0) {
                        continue;
                    } else {
                        gameMap.addBlock(adjacentBlockId, BlockType.WALL, TerrainType.WALL);
                    }
                }
            }
        }
    }

    /**
     * 修改交叉路口的地形保持一致
     */
    private void modifyCrossTerrain(GameMap gameMap) {
        BlockType[] blockTypes = new BlockType[] {BlockType.SHORTCUT, BlockType.MAIN_ROAD};
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

    /**
     * 生成围墙
     */
    private void generateWall(GameMap gameMap) {
        List<Integer> wallBlockIdList = new LinkedList<>();
        for (int blockId: gameMap.unoccupiedBlockList) {
            if ((gameMap.isNear(blockId, 4, BlockType.MAIN_ROAD, BlockType.SHORTCUT) ||
                    gameMap.isNear(blockId, 4, BlockType.MAIN_ROAD, BlockType.SHORTCUT)) &&
                    !gameMap.isNear(blockId, 3, BlockType.MAIN_ROAD, BlockType.SHORTCUT) &&
                    !gameMap.isNear(blockId, 2, BlockType.MAIN_ROAD, BlockType.SHORTCUT) &&
                    !gameMap.isNear(blockId, 1, BlockType.MAIN_ROAD, BlockType.SHORTCUT)) {
                wallBlockIdList.add(blockId);
            }
        }

        for (int blockId: wallBlockIdList) {
            gameMap.addBlock(blockId, BlockType.WALL, TerrainType.WALL);
        }

        for (int blockId : wallBlockIdList) {
            int x = gameMap.getX(blockId);
            int y = gameMap.getY(blockId);

            if ((x + 1) % 4 == 0 && (y + 1) % 4 == 0) {
                BuildingType buildingType;
                RoadDirection roadDirection = gameMap.getRoadDirection(blockId, BlockType.WALL);
                if (roadDirection == RoadDirection.VERTICAL || roadDirection == RoadDirection.HORIZONTAL) {
                    buildingType = BuildingType.TOWER_B;
                } else {
                    buildingType = BuildingType.TOWER_A;
                }
                gameMap.addBuilding(buildingType, x, y, Orientation.FREE);
            } else if ((x + 1) % 4 == 2 || ((y + 1) % 4 == 2)) {
                RoadDirection roadDirection = gameMap.getRoadDirection(blockId, BlockType.WALL);
                Orientation orientation;
                if (roadDirection == RoadDirection.VERTICAL) {
                    orientation = Orientation.EAST;
                } else {
                    orientation = Orientation.NORTH;
                }
                gameMap.addBuilding(BuildingType.WALL, x, y, orientation);
            }
        }
    }

    /**
     * 生成景观
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
                        gameMap.addBlock(viewBlockId, BlockType.BRANCH, TerrainType.FOREST);
                    }
                }
            }
            count -= (width - 1) * (height - 1);
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
                            if (block != null && (block.type == BlockType.MAIN_ROAD
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
                    if (gameMap.isNear(blockId, 3, BlockType.MAIN_ROAD, BlockType.SHORTCUT)) {
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
                                    BlockType.OBSTACLE, TerrainType.BUILDING);
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
            marathonGameMap.generate(10, 10);
        }
    }
}

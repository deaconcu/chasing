package com.prosper.chasing.game.map;

import com.prosper.chasing.game.util.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static Map<Byte, TerrainConfig> terrainConfigMap = new HashMap<>();

    static {
        /**
         * 快速通过
         */
        terrainConfigMap.put(Constant.TerrainType.ROAD, new TerrainConfig(
                // 铺装路面，存在于主路
                new LocateType[]{LocateType.ROAD}));
        terrainConfigMap.put(Constant.TerrainType.VEGETABLE, new TerrainConfig(
                // 蔬菜地，存在于主路
                new LocateType[]{LocateType.ROAD}));

        /**
         * 不能通过，需要找资源才能通过
         */
        terrainConfigMap.put(Constant.TerrainType.LAVA, new TerrainConfig(
                // 岩浆：存在于支路
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 1, 3, 0, 0));
        terrainConfigMap.put(Constant.TerrainType.WATER, new TerrainConfig(
                // 水面：存在于支路
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 1, 3, 0, 0));
        terrainConfigMap.put(Constant.TerrainType.FOREST, new TerrainConfig(
                // 森林：存在于支路，也存在于主路，在主路上时，存在于道路两旁，提供木材；支路时，存在于道路中间
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 1, 8, 0, 0));
        terrainConfigMap.put(Constant.TerrainType.ROCK, new TerrainConfig(
                // 石头地：存在于支路，也存在于主路，在主路上时，存在于道路两旁，提供石料；支路时，存在于道路中间
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 1, 5, 0, 0));

        /**
         * 缓慢通过，停止运动后不会后退
         */
        terrainConfigMap.put(Constant.TerrainType.GRASS, new TerrainConfig(
                // 草地：存在于主路或者支路，速度降低10%，单段长度10-30
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 10, 30, 0.9f, 0));
        terrainConfigMap.put(Constant.TerrainType.WHEAT, new TerrainConfig(
                // 麦田：存在于主路和支路，速度降低30%，单端长度7-24
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 7, 24, 0.7f, 0));
        terrainConfigMap.put(Constant.TerrainType.RAIN, new TerrainConfig(
                // 雨林：存在于主路和支路，速度降低30%，单端长度7-24
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 7, 24, 0.7f, 0));
        terrainConfigMap.put(Constant.TerrainType.SAND, new TerrainConfig(
                // 沙地：存在于主路和支路，速度降低30%，单端长度7-24
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 7, 24, 0.7f, 0));
        terrainConfigMap.put(Constant.TerrainType.SNOW, new TerrainConfig(
                // 雪地；存在于主路和支路，速度降低50%，单端长度5-19
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 5, 19, 0.5f, 0));
        terrainConfigMap.put(Constant.TerrainType.SWAMP, new TerrainConfig(
                // 沼泽：存在于主路和支路，速度降低70%，单端长度4-15
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 4, 15, 0.3f, 0));

        /**
         * 缓慢通过，必须保持速度，否则后退或者返回某一个复活点
         */
        terrainConfigMap.put(Constant.TerrainType.WIND, new TerrainConfig(
                // 大风地带：存在于主路和支路，速度降低40%，单端长度5-19
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 5, 19, 0.6f, 0.2f));
        terrainConfigMap.put(Constant.TerrainType.WILDWIND, new TerrainConfig(
                // 飓风地带：存在于主路和支路，速度降低60%，单端长度5-19
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 5, 19, 0.6f, 0.2f));
        terrainConfigMap.put(Constant.TerrainType.ANIMAL, new TerrainConfig(
                // 有猛兽的地方, 存在于主路和支路，速度应该在平时的200%，保持一段时间 单段长度5-19
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 5, 19, 1, 0.5f));

        /**
         * 其他
         */
        terrainConfigMap.put(Constant.TerrainType.FOG, new TerrainConfig(
                // 浓雾：存在于主路和支路，随机地形，单端长度5-20
                new LocateType[]{LocateType.ROAD, LocateType.SHORTCUT}, 5, 20, 1, 0));
    }

    public enum LocateType {ROAD, SHORTCUT}

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

        // 奖励加成，比如要求保持速度的地形，实际跑步距离可以比分支距离加上缩短距离 少 10%
        float addOn;

        TerrainConfig(LocateType[] locateTypes) {
            this.locateTypes = locateTypes;
        }

        TerrainConfig(LocateType[] locateTypes, int minLength, int maxLength, float speedRate, float addOn) {
            this.locateTypes = locateTypes;
            this.minLength = minLength;
            this.maxLength = maxLength;

            if (speedRate > 1) this.speedRate = 1;
            else if (speedRate < 0) this.speedRate = 0;
            else this.speedRate = speedRate;

            this.addOn = addOn;
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
        for (Branch branch: shortcutList) {
            List<TerrainConfig> validTerrainConfig = new ArrayList<>();
            for (TerrainConfig terrainConfig: terrainConfigMap.values()) {
                for (LocateType locateType: terrainConfig.locateTypes) {
                    if (locateType == LocateType.SHORTCUT) {
                        validTerrainConfig.add(terrainConfig);
                    }
                }
            }
            generateTerrainForShortcut(validTerrainConfig, branch);
        }
    }

    private void generateTerrainForShortcut(List<TerrainConfig> validTerrainConfigList, Branch branch) {
        int distance = branch.getShort() - branch.size();
        Map<Byte, Integer> specialSegmentMap = new HashMap<>();

        int totalSegmentLength = 0;
        while(totalSegmentLength > distance / 2) {
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
        setBranch(specialSegmentMap, branch, distance);
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
                normalSegmentList.remove(normalSegmentLenIndex);
            }
        }
    }


    public void generate(int boundX, int boundY) {
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

        int mergeCount = 100;
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
            GameMap gameMap = gameMapCreator.expand(3);

            generateTerrainForMainRoad(gameMap.mainRoad);
            generateTerrainForShortcuts(gameMap.shortcutList);
        }
    }

    public static void main(String[] args) {
        int i = 1;
        while(i -- > 0) {
            MarathonGameMapCreator marathonGameMap = new MarathonGameMapCreator();
            //marathonGameMap.generate(10, 10);
            marathonGameMap.generate(50, 50);
        }
    }
}

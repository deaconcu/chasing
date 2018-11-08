package com.prosper.chasing.game.base;

import java.util.*;

/**
 * 游戏里随机生成的道具配置, 假设游戏中的道具是全地图随机分布的，如果有新的业务场景再修改
 * 主要按如下方式生成：生成道具的总数以及生成每一种道具的概率
 * Created by deacon on 2018/3/1.
 */
public class GamePropConfigMap {

    // 生成道具的周期, 单位为秒
    private int period;

    // 每个周期生成道具的数量
    private int periodCount;

    // 存放道具配置信息的map
    private Map<Short, GamePropConfig> configMap = new HashMap<>();

    public GamePropConfigMap (int period, int count) {
        this.period = period;
        this.periodCount = count;
    }

    public int getTotal() {
        int total = 0;
        for (GamePropConfig gamePropConfig: configMap.values()) {
            total += gamePropConfig.count;
        }
        return total;
    }

    /**
     * 添加道具
     * @param duration duration单位为秒
     */
    public GamePropConfigMap add(short propTypeId, short count, short duration, boolean movable) {
        configMap.put(propTypeId, new GamePropConfig(propTypeId, count, duration, movable));
        return this;
    }

    /**
     * 获得游戏中的所有道具id列表
     * @return
     */
    public LinkedList<Short> getPropList() {
        LinkedList<Short> propList = new LinkedList<>();
        for(GamePropConfig gamePropConfig: configMap.values()) {
            for (int i = 0; i < gamePropConfig.count; i ++) {
                propList.add(gamePropConfig.propTypeId);
            }
        }
        Collections.shuffle(propList);
        return propList;
    }

    /**
     * 获取在场景中应该有的道具数量
     */
    public int getPropInScene(int second) {
        return (second / period + 1) * periodCount;
    }

    /**
     * 获取库存的道具数量
     */
    public int getPropInStock(int second) {
        int propInstock = getTotal() - getPropInScene(second);
        return propInstock > 0 ? propInstock : 0;
    }

    /**
     * 获取某一个prop的配置
     */
    public GamePropConfig getPropConfig(short propId) {
        return configMap.get(propId);
    }

    public static class GamePropConfig {
        // 道具类型id
        public short propTypeId;

        // 道具数量
        public short count;

        // 道具持续时间, 0为不消失
        public short duration;

        // 道具是否移动
        public boolean movable;

        /**
         * duration单位是秒
         */
        public GamePropConfig(short propTypeId, short count, short duration, boolean movable) {
            this.propTypeId = propTypeId;
            this.count = count;
            this.duration = duration;
            this.movable = movable;
        }
    }


}

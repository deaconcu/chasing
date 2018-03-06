package com.prosper.chasing.game.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 游戏里随机生成的道具配置, 假设游戏中的道具是全地图随机分布的，如果有新的业务场景再修改
 * 主要按如下方式生成：生成道具的总数以及生成每一种道具的概率
 * Created by deacon on 2018/3/1.
 */
public class GamePropConfigMap {

    // 生成道具总数
    public int count;

    public Map<Byte, GamePropConfig> configMap = new HashMap<>();

    private Random random = new Random();

    private short totalRate = 0;

    public GamePropConfigMap (int count) {
        this.count = count;
    }

    /**
     * 添加道具
     * 如果rate的值总数超过100, 抛出一个警告
     */
    public GamePropConfigMap add(byte propTypeId, short rate, short duration, boolean movable) {
        configMap.put(propTypeId, new GamePropConfig(propTypeId, rate, duration, movable));
        totalRate += rate;
        return this;
    }

    /**
     * 获取一个随机的道具配置
     */
    public GamePropConfig getRandomProp() {
        int hit = random.nextInt(1000) + 1;
        int sum = 0;
        for (GamePropConfig gamePropConfig: configMap.values()) {
            sum += gamePropConfig.rate;
            if (sum >= hit) return gamePropConfig;
        }
        return null;
    }

    public static class GamePropConfig {
        // 道具类型id
        public byte propTypeId;

        // 生成该道具的概率, 千分制
        public short rate;

        // 道具持续时间, 0为不消失
        public short duration;

        // 道具移动速度，0为静止道具
        public boolean movable;

        /**
         * rate是千分制, duration单位是秒
         */
        public GamePropConfig(byte propTypeId, short rate, short duration, boolean movable) {
            this.propTypeId = propTypeId;
            this.rate = rate;
            this.duration = duration;
            this.movable = movable;
        }
    }


}

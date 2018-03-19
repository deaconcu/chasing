package com.prosper.chasing.game.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by deacon on 2018/3/14.
 */
public class BuffConfig {

    private static Logger log = LoggerFactory.getLogger(PropConfig.class);

    public static Map<Byte, BuffConfig> buffConfigMap;

    public static final byte FLASH_LEVEL_1 = 1;
    public static final byte FLASH_LEVEL_2 = 2;
    public static final byte SPEED_UP_LEVEL_1 = 3;
    public static final byte SPEED_UP_LEVEL_2 = 4;
    public static final byte SPEED_DOWN_LEVEL_1 = 5;
    public static final byte SPEED_DOWN_LEVEL_2 = 6;
    public static final byte HOLD_POSITION = 7;
    public static final byte FOLLOW = 8;
    public static final byte FOLLOWED = 9;
    public static final byte INVISIBLE_LEVEL_1 = 10;
    public static final byte INVISIBLE_LEVEL_2 = 11;
    public static final byte DARK_VISION = 12;
    public static final byte IMMUNITY_LEVEL_1 = 13;
    public static final byte IMMUNITY_LEVEL_2 = 14;
    public static final byte NEAR_ENEMY_REMIND = 15;
    public static final byte REBOUND = 16;

    static {
        buffConfigMap.put(SPEED_UP_LEVEL_1, new BuffConfig(SPEED_UP_LEVEL_1, (short)20));
        buffConfigMap.put(SPEED_UP_LEVEL_2, new BuffConfig(SPEED_UP_LEVEL_2, (short)20));
        buffConfigMap.put(SPEED_DOWN_LEVEL_1, new BuffConfig(SPEED_DOWN_LEVEL_1, (short)20));
        buffConfigMap.put(SPEED_DOWN_LEVEL_2, new BuffConfig(SPEED_DOWN_LEVEL_2, (short)20));
        buffConfigMap.put(HOLD_POSITION, new BuffConfig(HOLD_POSITION, (short)20));
        buffConfigMap.put(FOLLOW, new BuffConfig(FOLLOW, (short)20));
        buffConfigMap.put(FOLLOWED, new BuffConfig(FOLLOWED, (short)20));
        buffConfigMap.put(INVISIBLE_LEVEL_1, new BuffConfig(INVISIBLE_LEVEL_1, (short)20));
        buffConfigMap.put(INVISIBLE_LEVEL_2, new BuffConfig(INVISIBLE_LEVEL_2, (short)20));
        buffConfigMap.put(DARK_VISION, new BuffConfig(DARK_VISION, (short)20));
        buffConfigMap.put(IMMUNITY_LEVEL_1, new BuffConfig(IMMUNITY_LEVEL_1, (short)20));
        buffConfigMap.put(IMMUNITY_LEVEL_2, new BuffConfig(IMMUNITY_LEVEL_2, (short)20));
        buffConfigMap.put(NEAR_ENEMY_REMIND, new BuffConfig(NEAR_ENEMY_REMIND, (short)20));
        buffConfigMap.put(REBOUND, new BuffConfig(REBOUND, (short)20));
    }

    public static void putBuff(BuffConfig buffConfig) {
        if (buffConfigMap.containsKey(buffConfig.id)) {
            log.warn("buff id is exist: " + buffConfig.id);
            return;
        }
        buffConfigMap.put(buffConfig.id, buffConfig);
    }

    public byte id;
    public short last;

    public BuffConfig (byte id, short last) {
        this.id = id;
        this.last = last;
    }

    public static BuffConfig getBuff(byte buffId) {
        return buffConfigMap.get(buffId);
    }
}

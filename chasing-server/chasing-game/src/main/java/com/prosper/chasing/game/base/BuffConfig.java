package com.prosper.chasing.game.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by deacon on 2018/3/14.
 */
public class BuffConfig {

    private static Logger log = LoggerFactory.getLogger(PropConfig.class);

    //public static GameMap<Byte, BuffConfig> buffConfigMap = new HashMap<>();

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
    public static final byte IMMUNITY = 13;
    public static final byte NEAR_ENEMY_REMIND = 14;
    public static final byte REBOUND = 15;
    public static final byte HOLD_SCEPTER = 16; // 持有权杖，可以淘汰对手
    public static final byte EXPEL = 17; // 持有权杖，可以淘汰对手

    public static final byte SPEED_DOWN_LEVEL_1_TERRAIN = 18;
    public static final byte SPEED_DOWN_LEVEL_2_TERRAIN = 19;
    public static final byte SPEED_DOWN_LEVEL_3_TERRAIN = 20;

    public static final byte WIND = 21;
    public static final byte ANIMAL = 22;

    public static final byte SLEEPY_LEVEL_1 = 23;
    public static final byte SLEEPY_LEVEL_2 = 24;
    public static final byte DREAMING = 25;

    /*
    static {
        buffConfigMap.put(SPEED_UP_LEVEL_1, new BuffConfig(SPEED_UP_LEVEL_1, (short)20));
        buffConfigMap.put(SPEED_UP_LEVEL_2, new BuffConfig(SPEED_UP_LEVEL_2, (short)20));
        buffConfigMap.put(SPEED_DOWN_LEVEL_1, new BuffConfig(SPEED_DOWN_LEVEL_1, (short)20));
        buffConfigMap.put(SPEED_DOWN_LEVEL_2, new BuffConfig(SPEED_DOWN_LEVEL_2, (short)20));
        buffConfigMap.put(HOLD_POSITION, new BuffConfig(HOLD_POSITION, (short)20));
        buffConfigMap.put(FOLLOW, new BuffConfig(FOLLOW, (short)20));
        buffConfigMap.put(FOLLOWED, new BuffConfig(FOLLOWED, (short)20));
        buffConfigMap.put(INVISIBLE_LEVEL_1, new BuffConfig(INVISIBLE_LEVEL_1, (short)30));
        buffConfigMap.put(INVISIBLE_LEVEL_2, new BuffConfig(INVISIBLE_LEVEL_2, (short)30));
        buffConfigMap.put(DARK_VISION, new BuffConfig(DARK_VISION, (short)20));
        buffConfigMap.put(IMMUNITY, new BuffConfig(IMMUNITY, (short)20));
        buffConfigMap.put(IMMUNITY_LEVEL_2, new BuffConfig(IMMUNITY_LEVEL_2, (short)20));
        buffConfigMap.put(NEAR_ENEMY_REMIND, new BuffConfig(NEAR_ENEMY_REMIND, (short)20));
        buffConfigMap.put(REBOUND, new BuffConfig(REBOUND, (short)20));
        buffConfigMap.put(HOLD_SCEPTER, new BuffConfig(HOLD_POSITION, (short)300));
        buffConfigMap.put(EXPEL, new BuffConfig(HOLD_POSITION, Short.MAX_VALUE));
        buffConfigMap.put(SPEED_DOWN_LEVEL_1_TERRAIN, new BuffConfig(SPEED_DOWN_LEVEL_1_TERRAIN, (short)-1));
        buffConfigMap.put(SPEED_DOWN_LEVEL_2_TERRAIN, new BuffConfig(SPEED_DOWN_LEVEL_2_TERRAIN, (short)-1));
        buffConfigMap.put(SPEED_DOWN_LEVEL_3_TERRAIN, new BuffConfig(SPEED_DOWN_LEVEL_3_TERRAIN, (short)-1));
        buffConfigMap.put(WIND_OLD, new BuffConfig(WIND_OLD, (short)-1));
        buffConfigMap.put(ANIMAL, new BuffConfig(ANIMAL, (short)-1));
    }
    */

    /*
    public static void putBuff(BuffConfig buffConfig) {
        if (buffConfigMap.containsKey(buffConfig.objectId)) {
            log.warn("buff objectId is exist: " + buffConfig.objectId);
            return;
        }
        buffConfigMap.put(buffConfig.objectId, buffConfig);
    }
    */

    /**
     * 检查新的buff是否可以加入到buffList中
     * @return
     */
    /*
    public static boolean checkBuff(List<Buff> buffList, byte buffTypeId, int groupId) {
        if (groupId != 0) {
            for (Buff buff: buffList) {
                if (buff.groupId == groupId) {
                    return false;
                }
            }
        } else {
            if (buffTypeId == INVISIBLE_LEVEL_1 && (hasBuff(buffList, INVISIBLE_LEVEL_1) || hasBuff(buffList, INVISIBLE_LEVEL_2))) {
                return false;
            } else if (buffTypeId == INVISIBLE_LEVEL_2 && hasBuff(buffList, INVISIBLE_LEVEL_2)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasBuff(List<Buff> buffList, byte buffTypeId) {
        for (Buff buff: buffList) {
            if (buff.typeId == buffTypeId) {
                return true;
            }
        }
        return false;
    }
    */

    //public byte objectId;
    //public short last;


    /*
    public BuffConfig (byte objectId, short last) {
        this.objectId = objectId;
        this.last = last;
    }
    */

    /*
    public static BuffConfig getBuff(byte buffId) {
        return buffConfigMap.get(buffId);
    }
    */
}

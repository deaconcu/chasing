package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums.*;
import com.prosper.chasing.game.base.Abilities.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by deacon on 2019/3/29.
 */
public class DefaultBuffConfig {

    public static Map<BuffType, Abilities.Ability[]> getBuffConfig() {
        Map<BuffType, Abilities.Ability[]> buffConfigMap = new HashMap<>();
        buffConfigMap.put(BuffType.FLASH_LEVEL_1, new Ability[]{});
        buffConfigMap.put(BuffType.FLASH_LEVEL_2, new Ability[]{});
        buffConfigMap.put(BuffType.SPEED_UP_LEVEL_1, new Ability[]{new SpeedRateAddOn((short)20)});
        buffConfigMap.put(BuffType.SPEED_UP_LEVEL_2, new Ability[]{new SpeedRateAddOn((short)40)});
        buffConfigMap.put(BuffType.SPEED_DOWN_LEVEL_1, new Ability[]{new SpeedRateAddOn((short)-20)});
        buffConfigMap.put(BuffType.SPEED_DOWN_LEVEL_2, new Ability[]{new SpeedRateAddOn((short) -40)});
        buffConfigMap.put(BuffType.HOLD_POSITION, new Ability[]{});
        buffConfigMap.put(BuffType.FOLLOW, new Ability[]{});
        buffConfigMap.put(BuffType.FOLLOWED, new Ability[]{});
        buffConfigMap.put(BuffType.INVISIBLE_LEVEL_1, new Ability[]{});
        buffConfigMap.put(BuffType.INVISIBLE_LEVEL_2, new Ability[]{});
        buffConfigMap.put(BuffType.DARK_VISION, new Ability[]{});
        buffConfigMap.put(BuffType.IMMUNITY, new Ability[]{});
        buffConfigMap.put(BuffType.NEAR_ENEMY_REMIND, new Ability[]{});
        buffConfigMap.put(BuffType.REBOUND, new Ability[]{});
        buffConfigMap.put(BuffType.HOLD_SCEPTER, new Ability[]{new SpeedRateAddOn((short)100)});
        buffConfigMap.put(BuffType.EXPEL, new Ability[]{});
        buffConfigMap.put(BuffType.SPEED_DOWN_LEVEL_1_TERRAIN, new Ability[]{new SpeedRateAddOn((short)-20)});
        buffConfigMap.put(BuffType.SPEED_DOWN_LEVEL_2_TERRAIN, new Ability[]{new SpeedRateAddOn((short)-40)});
        buffConfigMap.put(BuffType.SPEED_DOWN_LEVEL_3_TERRAIN, new Ability[]{new SpeedRateAddOn((short)-60)});
        buffConfigMap.put(BuffType.WIND, new Ability[]{});
        buffConfigMap.put(BuffType.ANIMAL, new Ability[]{});
        buffConfigMap.put(BuffType.SLEEPY_LEVEL_1, new Ability[]{new SpeedRateAddOn((short)-25)});
        buffConfigMap.put(BuffType.SLEEPY_LEVEL_2, new Ability[]{new SpeedRateAddOn((short)-50)});
        buffConfigMap.put(BuffType.DREAMING, new Ability[]{});

        return buffConfigMap;
    }
}

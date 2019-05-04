package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums.*;
import com.prosper.chasing.game.base.Abilities.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by deacon on 2019/3/28.
 */
public class DefaultPropConfig {

    public static Map<PropType, Prop> getPropConfig() {
        Map<PropType, Prop> propConfigMap = new HashMap<>();
        propConfigMap.put(PropType.MARK, new Prop(new Ability[]{new Mark()}));

        propConfigMap.put(PropType.INVISIBLE_LEVEL_1, new Prop(new Ability[]{
                new AddBuff(BuffType.INVISIBLE_LEVEL_1, (short)30)
        }));

        propConfigMap.put(PropType.INVISIBLE_LEVEL_2, new Prop(new Ability[]{
                new AddBuff(BuffType.INVISIBLE_LEVEL_2, (short)30)
        }));

        propConfigMap.put(PropType.ANTI_INVISIBLE, new Prop(new Ability[]{
                new RemoveBuff(BuffType.INVISIBLE_LEVEL_1),
                new RemoveBuff(BuffType.INVISIBLE_LEVEL_2)
        }));

        propConfigMap.put(PropType.RETURN_TO_INIT_POSITION, new Prop(new Ability[]{}));

        propConfigMap.put(PropType.RANDOM_POSITION, new Prop(new Ability[]{
                new RandomPosition()
        }));

        propConfigMap.put(PropType.FLASH_LEVEL_1, new Prop(new Ability[]{
                new AddBuff(BuffType.FLASH_LEVEL_1, (short)10)
        }));

        propConfigMap.put(PropType.FLASH_LEVEL_2, new Prop(new Ability[]{
                new AddBuff(BuffType.FLASH_LEVEL_2, (short)10)
        }));

        propConfigMap.put(PropType.FOLLOW, new Prop(new Ability[]{new Mark()}));

        propConfigMap.put(PropType.SPEED_UP_LEVEL_1, new Prop(new Ability[]{
                new RemoveBuff(BuffType.SPEED_DOWN_LEVEL_1),
                new RemoveBuff(BuffType.SPEED_DOWN_LEVEL_2),
                new AddBuff(BuffType.SPEED_UP_LEVEL_1, (short)20)
        }));

        propConfigMap.put(PropType.SPEED_UP_LEVEL_2, new Prop(new Ability[]{
                new RemoveBuff(BuffType.SPEED_DOWN_LEVEL_1),
                new RemoveBuff(BuffType.SPEED_DOWN_LEVEL_2),
                new RemoveBuff(BuffType.SPEED_UP_LEVEL_1),
                new AddBuff(BuffType.SPEED_UP_LEVEL_2, (short)20)
        }));

        propConfigMap.put(PropType.SPEED_DOWN_LEVEL_1, new Prop(new Ability[]{
                new RemoveBuff(BuffType.SPEED_UP_LEVEL_1),
                new RemoveBuff(BuffType.SPEED_UP_LEVEL_2),
                new AddBuff(BuffType.SPEED_DOWN_LEVEL_1, (short)20)
        }));

        propConfigMap.put(PropType.SPEED_DOWN_LEVEL_2, new Prop(new Ability[]{
                new RemoveBuff(BuffType.SPEED_UP_LEVEL_1),
                new RemoveBuff(BuffType.SPEED_UP_LEVEL_2),
                new RemoveBuff(BuffType.SPEED_DOWN_LEVEL_1),
                new AddBuff(BuffType.SPEED_DOWN_LEVEL_2, (short)20)
        }));

        propConfigMap.put(PropType.HOLD_POSITION, new Prop(new Ability[]{
                new AddBuff(BuffType.HOLD_POSITION, (short)20)
        }));

        propConfigMap.put(PropType.BLOOD_PILL, new Prop(new Ability[]{}));
        propConfigMap.put(PropType.BLOOD_BAG, new Prop(new Ability[]{}));
        propConfigMap.put(PropType.REBIRTH, new Prop(new Ability[]{}));

        propConfigMap.put(PropType.DARK_VISION, new Prop(new Ability[]{
                new AddBuff(BuffType.DARK_VISION, (short)20)
        }));

        propConfigMap.put(PropType.IMMUNITY, new Prop(new Ability[]{
                new AddBuff(BuffType.IMMUNITY, (short)20)
        }));

        propConfigMap.put(PropType.REBOUND, new Prop(new Ability[]{
                new AddBuff(BuffType.REBOUND, (short)20)
        }));

        propConfigMap.put(PropType.NEAR_ENEMY_REMIND, new Prop(new Ability[]{
                new AddBuff(BuffType.NEAR_ENEMY_REMIND, (short)20)
        }));

        propConfigMap.put(PropType.PROP_BOMB, new Prop(new Ability[]{}));

        propConfigMap.put(PropType.MONEY, new Prop(new Ability[]{
                new AddMoney(2000)
        }));

        propConfigMap.put(PropType.GIFT_BOX, new Prop(new Ability[]{
                new AddProp()
        }));

        propConfigMap.put(PropType.SCEPTER, new Prop(
                new Ability[]{new AddBuff(BuffType.HOLD_SCEPTER, (short)-1)},
                new Ability[]{new Kill()})
        );

        propConfigMap.put(PropType.BRIDGE, new Prop(new Ability[]{}));
        propConfigMap.put(PropType.RAIN_CLOUD, new Prop(new Ability[]{}));
        propConfigMap.put(PropType.WOOD, new Prop(new Ability[]{}));
        return propConfigMap;
    }
}

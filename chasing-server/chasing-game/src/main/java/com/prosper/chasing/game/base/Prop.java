package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums.*;
import com.prosper.chasing.game.base.Abilitys.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by deacon on 2019/3/29.
 */
public class Prop {

    private Map<PropUsageType, Ability[]> abilityMap = new HashMap<>();

    public Prop(Ability[] holdAbilities, Ability[] useAbilities) {
        abilityMap.put(PropUsageType.HOLD, holdAbilities);
        abilityMap.put(PropUsageType.USE, useAbilities);
    }

    public Prop(Ability[] ability) {
        this(null, ability);
    }

    public boolean usable() {
        return abilityMap.get(PropUsageType.USE) != null && abilityMap.get(PropUsageType.USE).length > 0;
    }

    public Ability[] getAbilities(PropUsageType usageType) {
        return abilityMap.get(usageType);
    }
}

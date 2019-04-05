package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums.*;

import java.util.*;

/**
 * Created by deacon on 2019/4/1.
 */
public class AllAtOnce implements PropGenerator {

    private Game game;

    private boolean finished = false;

    private Map<PropType, Integer> propMap = new HashMap<>();

    public AllAtOnce(Game game) {
        this.game = game;
    }

    public AllAtOnce set(PropType propType, int count) {
        if (!finished) propMap.put(propType, count);
        return this;
    }

    @Override
    public List<EnvProp> getProp() {
        if (finished) return Collections.emptyList();
        List<EnvProp> envPropList = new LinkedList<>();

        propMap.entrySet().forEach(e -> {
            for (int i = 0; i < e.getValue(); i ++) {
                envPropList.add(new EnvProp(e.getKey(), -1,
                        game.gameMap.getRandomRoadPoint(RoadPointType.CENTER).getPoint().toPoint3()));
            }
        });
        finished = true;
        return envPropList;
    }
}

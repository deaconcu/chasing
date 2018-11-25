package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.PropConfig;
import com.prosper.chasing.game.util.Enums.*;

import java.util.Map;

/**
 * Created by deacon on 2018/11/23.
 */
public class TerrainTransferConfigs {

    public static Map<TerrainType, TerrainTransferConfig> configMap;

    public static class TerrainTransferConfig {
        public TerrainType current;
        public TerrainType target;
        public ResourceType resourceType;
        public int id;
        public int count;

        TerrainTransferConfig(TerrainType current, TerrainType target, ResourceType resourceType, int id, int count) {
            this.current = current;
            this.target = target;
            this.resourceType = resourceType;
            this.id = id;
            this.count = count;
        }
    }

    static {
        addConfig(TerrainType.RIVER, TerrainType.RIVER_WITH_BRIDGE, ResourceType.PROP, PropConfig.WOOD, 5);
        addConfig(TerrainType.GATE, TerrainType.GATE_OPEN, ResourceType.MONEY, 0, 1);
        addConfig(TerrainType.FIRE, TerrainType.FIRE_PUT_OUT, ResourceType.PROP, PropConfig.RAIN_CLOUD, 1);
    }

    public static void addConfig(
            TerrainType current, TerrainType target, ResourceType resourceType, int id, int count) {
        configMap.put(current, new TerrainTransferConfig(current, target, resourceType, id, count));
    }

    public static TerrainTransferConfig getConfig(TerrainType current) {
        return configMap.get(current);
    }
}

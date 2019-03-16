package com.prosper.chasing.game.base;

import java.util.List;

/**
 * 假设地图上的NPC是指定初始位置和路径的
 */
public class GameNPCConfigMap {

    public static class GameNPCConfig {
        Position initPosition;
        List<Point3> path;
    }

}

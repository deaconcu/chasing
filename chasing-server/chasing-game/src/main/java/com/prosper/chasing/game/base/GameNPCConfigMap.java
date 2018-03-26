package com.prosper.chasing.game.base;

import com.prosper.chasing.game.navmesh.Point;

import java.util.List;

/**
 * 假设地图上的NPC是指定初始位置和路径的
 */
public class GameNPCConfigMap {

    public static class GameNPCConfig {
        Position initPosition;
        List<Point> path;
    }

}

package com.prosper.chasing.game.base;

/**
 * 地形
 * Created by deacon on 2018/4/20.
 */
public class TerrainBlock extends GameObject {

    // 标识符
    short id;

    // 类型id
    byte typeId;

    // 位置
    Point3 point3;

    @Override
    public Point3 getPoint3() {
        return point3;
    }
}

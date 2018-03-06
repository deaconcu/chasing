package com.prosper.chasing.game.base;

import com.prosper.chasing.game.navmesh.Point;

import java.util.Deque;

public class Prop extends Movable {

    // 标识id
    public int id;

    // 类型id
    public byte typeId;

    // 创建时间
    public long createTime;

    // 泯灭时间
    public long vanishTime;

    /**
     * 获取剩余生存时间, 单位为秒
     */
    public int getRemainSecond() {
        int remainSecond = (int)((vanishTime - System.currentTimeMillis()) / 1000);
        return remainSecond > 0 ? remainSecond : 0;
    }

}

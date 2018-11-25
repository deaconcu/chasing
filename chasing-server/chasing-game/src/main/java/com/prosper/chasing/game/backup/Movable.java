package com.prosper.chasing.game.backup;

import com.prosper.chasing.game.base.GameObject;
import com.prosper.chasing.game.base.Point;

/**
 * 可以运动的对象基类
 * 1. 可以追踪别的物体，同时被追逐物体只能有一个
 * 2. 可以被追逐，可以被多个物体追逐
 * Created by deacon on 2018/9/7.
 */
public class Movable extends GameObject {

    // 物体当前目标
    private GameObject target;
    // 物体运动速度，单位为毫米/秒
    private int speed;
    // 上次移动的时间戳
    private long lastMoveTimestamp;

    public Movable(int speed) {
        this.speed = speed;
    }

    public void setTarget(GameObject target) {
        this.target = target;
    }

    public void removeTarget() {
        this.target = null;
    }

    /**
     * 移动
     */
    public void move() {
        if (target == null) return;

        long timestamp = java.lang.System.currentTimeMillis();
        long timeGap = timestamp - lastMoveTimestamp;
        long distance = target.getPoint().distance(getPoint());

        if (distance <= timeGap * speed) {
            setPoint(target.getPoint());
        } else {
            Point vector = target.getPoint().subtraction(getPoint()).normalized();
            move(vector, (int)(speed * timeGap / 1000));
        }
        lastMoveTimestamp = timestamp;
    }

}

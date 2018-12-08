package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums;
import com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2018/3/14.
 */
public class GameObject {

    private int id;

    // 当前位置
    private Point point;

    // 朝向
    private int rotateY;

    // 是否移动
    private boolean isMoved;

    // 是否移动到了另一个分区
    private boolean isCrossZone;

    // 是否为活着的对象
    private boolean isAlive;

    // 生成时间
    private long createTime;

    // 同步的动作, 有时候需要根据不同的同步动作同步不一样的信息
    private SyncAction SyncAction;

    public GameObject() {
        this.isMoved = true;
        this.isAlive = true;
        this.SyncAction = SyncAction.BORN;
        this.createTime = System.currentTimeMillis();
    }

    public GameObject(int id, Point point, int rotateY) {
        this();
        this.id = id;
        this.point = point;
        this.rotateY = rotateY;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        if (point.equals(this.point)) return;
        this.point = point;
        isMoved = true;

        if (!this.point.sameZone(point)) {
            isCrossZone = true;
        }
    }

    public int getRotateY() {
        return rotateY;
    }

    public void setRotateY(int rotateY) {
        this.rotateY = rotateY;
        setMoved(true);
    }

    public void move(Point vector, int speed) {
        if (vector.isZero()) return;
        this.point.x += vector.x * speed;
        this.point.y += vector.y * speed;
        this.point.z += vector.z * speed;
        isMoved = true;
    }

    // 获取对象是否移动
    public boolean isMoved() {
        return isMoved;
    }

    public void setMoved(boolean moved) {
        isMoved = moved;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SyncAction getSyncAction() {
        return SyncAction;
    }

    public void setSyncAction(Enums.SyncAction syncAction) {
        SyncAction = syncAction;
    }

    public long getCreateTime() {
        return createTime;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public boolean isCrossZone() {
        return isCrossZone;
    }

    public void setCrossZone(boolean crossZone) {
        isCrossZone = crossZone;
    }
}

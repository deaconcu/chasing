package com.prosper.chasing.game.base;

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

    // 生成时间
    private long createTime;

    // 是否为新创建的对象
    private GameObjectLifeAction lifeAction;

    public GameObject(int id, Point point, int rotateY) {
        this.id = id;
        this.point = point;
        this.rotateY = rotateY;
        this.isMoved = true;
        this.lifeAction = GameObjectLifeAction.BORN;
        this.createTime = System.currentTimeMillis();
    }

    public void synced() {
        lifeAction = GameObjectLifeAction.ALIVE;
    }

    public void dead() {
        lifeAction = GameObjectLifeAction.DEAD;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        if (point.equals(this.point)) return;
        this.point = point;
        isMoved = true;
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

    public boolean isBorn() {
        return (lifeAction == GameObjectLifeAction.BORN);
    }

    public boolean isAlive() {
        return (lifeAction == GameObjectLifeAction.ALIVE);
    }

    public boolean isDead() {
        return (lifeAction == GameObjectLifeAction.DEAD);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public GameObjectLifeAction getLifeAction() {
        return lifeAction;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}

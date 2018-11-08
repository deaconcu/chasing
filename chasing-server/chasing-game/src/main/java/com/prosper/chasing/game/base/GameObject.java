package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2018/3/14.
 */
public class GameObject {

    protected int id;

    // 当前位置
    protected Point point;

    // 朝向
    protected int rotateY;

    // 是否移动
    private boolean isMoved;

    public GameObject() {}

    public GameObject(int id, Point point, int rotateY) {
        this.id = id;
        this.point = point;
        this.rotateY = rotateY;
        this.isMoved = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Point getPosition() {
        return point;
    }

    public void setPosition(Point point) {
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
}

package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums;
import com.prosper.chasing.game.util.Enums.*;

import java.util.LinkedList;

/**
 * 场景中所有物体的基类，应该包含如下几种子类：
 * User 游戏中的玩家
 * Prop 场景中的道具
 * Animal 场景中生成的可以追踪玩家的活动体
 * Stationary 场景中静止不动的一些物体，比如商店，火墙，玩家可以和一部分物体交互
 * Created by deacon on 2018/3/14.
 */
public class GameObject {

    private int id;

    // 当前位置
    private Point3 point3;

    // 朝向
    private int rotateY;

    // 是否移动
    private boolean isMoved;

    // 是否移动到了另一个分区, 这个值是为了降低计算频率
    // 有的时候不需要每次在用户移动的时候都进行计算，只需要在用户跨越了分区才进行判断，比如是否进入下雨路段
    private boolean isCrossZone;

    // 是否为活着的对象
    private boolean isAlive;

    // 生成时间
    private long createTime;

    // 同步的动作, 有时候需要根据不同的同步动作同步不一样的信息
    // 比如出生的时候客户端需要创建，死亡的时候客户端需要销毁，其他时间客户端只需要同步数据
    private SyncAction SyncAction;

    private LinkedList<Point3> path;

    private long lastMoveTimestamp;

    private int speed;

    public GameObject() {
        this.isMoved = true;
        this.isAlive = true;
        this.SyncAction = SyncAction.BORN;
        this.createTime = System.currentTimeMillis();
        this.lastMoveTimestamp = createTime;
    }

    public GameObject(int id, Point3 point3, int rotateY) {
        this();
        this.id = id;
        this.point3 = point3;
        this.rotateY = rotateY;
    }

    public void move() {
        if (path == null || path.size() == 0) return;

        long currentTimeMillis = System.currentTimeMillis();
        int distance = (int)((float)speed * (currentTimeMillis - lastMoveTimestamp) / 1000);
        Point3 currPoint3 = point3;
        int nextPointDistance = currPoint3.distance(path.peek());
        while (nextPointDistance < distance) {
            distance -= nextPointDistance;
            currPoint3 = path.removeFirst();
            if (path.size() > 0) {
                nextPointDistance = currPoint3.distance(path.peek());
            } else {
                break;
            }
        }

        if (path.size() == 0) {
            setPath(null);
            point3 = currPoint3;
        } else {
            nextPointDistance = currPoint3.distance(path.peek());
            point3 = currPoint3.add(path.peek().x - currPoint3.x, 0, path.peek().z - currPoint3.z,
                    (double)distance / nextPointDistance);
        }


        if (path.peek() != null) {
            double deltaX = path.peek().x - point3.x;
            double deltaY = path.peek().z - point3.z;

            int targetRotateY = 0;
            if (deltaX == 0 && deltaY > 0) targetRotateY = 90000;
            else if (deltaX == 0 && deltaY < 0) targetRotateY = 270000;
            else if (deltaX > 0 && deltaY == 0) targetRotateY = 0;
            else if (deltaX < 0 && deltaY == 0) targetRotateY = 180000;

            double sin = Math.abs(deltaY) / Math.sqrt(deltaX * deltaX + deltaY * deltaY);
            double absTargetRotateY = Math.toDegrees(Math.asin(sin));

            if (deltaX > 0 && deltaY > 0) targetRotateY = (int)absTargetRotateY * 1000;
            else if (deltaX < 0 && deltaY > 0) targetRotateY = (180 - (int)absTargetRotateY) * 1000;
            else if (deltaX < 0 && deltaY < 0) targetRotateY = (180 + (int)absTargetRotateY) * 1000;
            else if (deltaX > 0 && deltaY < 0) targetRotateY = (360 - (int)absTargetRotateY) * 1000;

            if (targetRotateY > rotateY) rotateY += 1000;
            else rotateY -= 1000;
        }
        this.lastMoveTimestamp = System.currentTimeMillis();
        isMoved = true;
    }

    public Point3 getPoint3() {
        return point3;
    }

    public void setPoint3(Point3 point3) {
        if (point3.equals(this.point3)) return;
        this.point3 = point3;
        isMoved = true;

        if (!this.point3.sameZone(point3)) {
            setCrossZone(true);
        }
    }

    public int getRotateY() {
        return rotateY;
    }

    public void setRotateY(int rotateY) {
        this.rotateY = rotateY;
        setMoved(true);
    }

    public void move(Point3 vector, int speed) {
        if (vector.isZero()) return;
        this.point3.x += vector.x * speed;
        this.point3.y += vector.y * speed;
        this.point3.z += vector.z * speed;
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

    public LinkedList<Point3> getPath() {
        return path;
    }

    public void setPath(LinkedList<Point3> path) {
        this.path = path;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}

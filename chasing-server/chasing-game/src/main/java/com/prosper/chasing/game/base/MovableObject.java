package com.prosper.chasing.game.base;

import com.prosper.chasing.game.navmesh.Point;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.prosper.chasing.game.util.Constant.ChasingConfig.DISTANCE_CATCHING;
import static com.prosper.chasing.game.util.Constant.ChasingConfig.DISTANCE_CHASING;

/**
 * 可移动的物体，包括可移动的道具，NPC等游戏对象，可以被玩家追逐，是整个游戏的核心对象
 * 有以下特点:
 * 1.没有被追逐的时候，这些对象大部分时间是静止的，也会有一些随机运动的几率
 * 2.玩家在追逐的时候这些物体会运动，会有速度变化
 * 3.可以被多个玩家追逐，玩家在追逐时，头顶有圆形进度条，显示离完成还剩多少，先到先得
 * 4.途中物体会尝试摆脱，被捕获前必定会尝试摆脱
 */
public abstract class MovableObject implements GameObject {

    private static final int WALK_SPEED = 3; // 无人追逐的情况下运动速度
    private static final int RUN_BASE_SPEED = 6; // 移动基础速度
    private static final int WALK_RATE = 10; // 没人打扰下运动频率，百分制，比如值为10的时候表示10分钟会运动1分钟
    private static final float CHASE_SPEED_RATE = 1; // 加速状态下最大加速比率

    private static final int PERIOD_MIN_SECOND = 5; // 运动模式持续时间最小值
    private static final int PERIOD_MAX_SECOND = 20; // 运动模式持续时间最大值

    // 标识id
    protected int id;

    protected Game game;

    protected Set<User> chasingUserSet = new HashSet<>();

    // 是否可移动
    protected boolean movable;

    // 移动路径
    protected Deque<Point> path;

    // 当前位置
    protected Position position;

    // 移动速度
    protected int speed;

    // 是否位置有变化
    private boolean isPositionChanged;

    // 上次移动的时间
    private long lastMoveMillis = 0;

    // 当前运动结束时间
    private long currentMoveEndMillis = 0;

    // 当前状态
    protected MoveState state = MoveState.WANDERING;

    // 是否是第一次同步信息，有些数据只同步一次，比如id，名称之类的
    protected boolean firstSync = true;

    public enum MoveState {
        WANDERING, CHASING
    }

    public MovableObject(Game game) {
        this.game = game;
    }

    public void setPositionChanged(boolean positionChanged) {
        isPositionChanged = positionChanged;
    }

    public boolean isPositionChanged() {
        return isPositionChanged;
    }

    public void setPath(Deque<Point> path) {
        this.path = path;
    }

    public Point getPositionPoint() {
        return position.point;
    }

    /**
     * 生成下一段运动的模式
     */
    public void generateNextMoveSpeed() {
        if (state == MoveState.WANDERING) {
            if (isInChasingArea()) {
                state = MoveState.CHASING;
                speed = getNextRunSpeed();
                currentMoveEndMillis = System.currentTimeMillis() +
                        ThreadLocalRandom.current().nextInt(PERIOD_MIN_SECOND ,  PERIOD_MAX_SECOND + 1) * 1000;
            } else if (currentMoveEndMillis <= System.currentTimeMillis()) {
                speed = getNextWalkSpeed();
                currentMoveEndMillis = System.currentTimeMillis() +
                        ThreadLocalRandom.current().nextInt(PERIOD_MIN_SECOND,  PERIOD_MAX_SECOND + 1) * 1000;
            }
        } else {
            if (!isInChasingArea()) {
                state = MoveState.WANDERING;
                speed = getNextWalkSpeed();
                currentMoveEndMillis = System.currentTimeMillis() +
                        ThreadLocalRandom.current().nextInt(PERIOD_MIN_SECOND,  PERIOD_MAX_SECOND + 1) * 1000;
            } else if (currentMoveEndMillis <= System.currentTimeMillis()) {
                speed = getNextRunSpeed();
                currentMoveEndMillis = System.currentTimeMillis() +
                        ThreadLocalRandom.current().nextInt(PERIOD_MIN_SECOND,  PERIOD_MAX_SECOND + 1) * 1000;
            }
        }
    }

    /**
     * 获取下一个周期的步行速度
     * 该值受步行概率影响
     */
    public int getNextWalkSpeed() {
        int probability = ThreadLocalRandom.current().nextInt(100);
        if (probability < WALK_RATE) {
            return WALK_SPEED;
        }
        return 0;
    }

    /**
     * 获取下一个周期的跑步速度
     * 该值受目标和玩家距离，加速最大比例影响
     * 距离越近，产生高速度的几率越大
     */
    public int getNextRunSpeed() {
        double rate = ThreadLocalRandom.current().nextDouble();
        float distanceLevel = getDistanceLevel();
        if (distanceLevel == 0) return WALK_SPEED;
        else {
            double probability = Math.pow(rate, getDistanceLevel());
            return (int)(RUN_BASE_SPEED + CHASE_SPEED_RATE * RUN_BASE_SPEED * probability);
        }
    }

    /**
     * 获得距离等级
     * return 0 表示距离在追逐范围之外
     * level = [0.125, 0.25, 0.5, 1, 2, 4, 8]
     */
    private float getDistanceLevel() {
        int minDistance = DISTANCE_CHASING + 1;
        for (User user: chasingUserSet) {
            int distance = user.getPosition().point.distance(position.point);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }

        int step = (DISTANCE_CHASING - DISTANCE_CATCHING) / 6;
        if (minDistance > DISTANCE_CHASING) return 0;
        else if (minDistance > DISTANCE_CHASING - step) return 8;
        else if (minDistance > DISTANCE_CHASING - step * 2) return 4;
        else if (minDistance > DISTANCE_CHASING - step * 3) return 2;
        else if (minDistance > DISTANCE_CHASING - step * 4) return 1;
        else if (minDistance > DISTANCE_CHASING - step * 5) return 0.5f;
        else if (minDistance > DISTANCE_CHASING - step * 6) return 0.25f;
        else return 0.125f;
    }

    /**
     * 执行捕获以后的动作
     */
    protected abstract void catched(User user);

    /**
     * 判断追逐的玩家是否接近
     */
    public boolean isInChasingArea() {
        int minDistance = DISTANCE_CHASING + 1;
        for (User user: chasingUserSet) {
            int distance = user.getPosition().point.distance(position.point);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }

        if (minDistance <= DISTANCE_CHASING) return true;
        return false;
    }

    /**
     * 移动
     */
    public void move() {
        if (!movable) return;
        generateNextMoveSpeed();
        long time = System.currentTimeMillis() - lastMoveMillis;

        long distance = time * speed;
        if (distance != 0) {
            while (true) {
                Point nextPoint = path.peek();
                if (nextPoint == null) break;
                long pointDistance = nextPoint.distance(position.point);
                if (pointDistance < distance) {
                    distance = distance - pointDistance;
                    position.point = path.pollFirst();
                } else if (pointDistance == distance) {
                    position.point = path.pollFirst();
                    setPositionChanged(true);
                    break;
                } else {
                    double ratio = (double) distance / pointDistance;
                    Point vector = new Point(
                            nextPoint.x - position.point.x,
                            nextPoint.y - position.point.y,
                            nextPoint.z - position.point.z);
                    position.point = position.point.add(vector, ratio);
                    setPositionChanged(true);
                    break;
                }
            }
        }
        lastMoveMillis = System.currentTimeMillis();
    }

    /**
     * 判断路径是不是为空
     */
    public boolean isPathEmpty() {
        if (movable && (path == null || path.size() == 0)) return true;
        return false;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
}

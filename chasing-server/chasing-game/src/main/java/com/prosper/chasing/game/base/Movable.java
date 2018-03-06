package com.prosper.chasing.game.base;

import com.prosper.chasing.game.navmesh.Point;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 可移动的物体，包括可移动的道具，NPC等游戏对象，可以被玩家追逐，是整个游戏的核心对象
 * 有以下特点:
 * 1.没有被追逐的时候，这些对象大部分时间是静止的，也会有一些随机运动的几率
 * 2.玩家在追逐的时候这些物体会运动，会有速度变化
 * 3.可以被多个玩家追逐，玩家在追逐时，头顶有圆形进度条，显示离完成还剩多少，先到先得
 * 4.途中物体会尝试摆脱，被捕获前必定会尝试摆脱
 */
public class Movable {

    private static final int WALK_SPEED = 5; // 无人追逐的情况下运动速度
    private static final int RUN_BASE_SPEED = 10; // 移动基础速度
    private static final int WALK_RATE = 10; // 没人打扰下运动频率，百分制，比如值为10的时候表示10分钟会运动1分钟
    private static final float CHASE_SPEED_RATE = 1; // 加速状态下最大加速比率

    private static final int PERIOD_MIN_SECOND = 5; // 运动模式持续时间最小值
    private static final int PERIOD_MAX_SECOND = 20; // 运动模式持续时间最大值

    private static final int DISTANCE_CHASING = 10;  // 追逐距离
    private static final int DISTANCE_CATCHING = 2;   // 捕获距离

    private static final int SECOND_CHASING = 10;  // 追逐保持时间
    private static final int SECOND_CATCHING = 10;  // 捕获保持时间

    protected Map<User, Progress> progressMap = new HashMap<>();

    // 移动路径
    protected Deque<Point> path;

    // 当前位置
    protected Position position;

    // 移动速度
    private int speed;

    // 是否位置有变化
    private boolean isPositionChanged;

    // 上次移动的时间
    private long lastMoveMillis = 0;

    // 当前运动结束时间
    private long currentMoveEndMillis = 0;

    // 当前状态
    private MoveState state = MoveState.WANDERING;

    public enum MoveState {
        WANDERING, CHASING
    }

    public enum ChasingStep {
        CHASING, CATCHING
    }

    public static class Progress {
        int percent;
        long startTime;
        ChasingStep step;
    }

    public void setPositionChanged(boolean positionChanged) {
        isPositionChanged = positionChanged;
    }

    /**
     * 生成下一段运动的模式
     */
    public void generateNextMoveSpeed() {
        if (state == MoveState.WANDERING) {
            if (isNear()) {
                state = MoveState.CHASING;
                speed = getNextRunSpeed();
                currentMoveEndMillis = System.currentTimeMillis() +
                        ThreadLocalRandom.current().nextInt(PERIOD_MIN_SECOND,  PERIOD_MAX_SECOND + 1);
            } else if (currentMoveEndMillis <= System.currentTimeMillis()) {
                speed = getNextWalkSpeed();
                currentMoveEndMillis = System.currentTimeMillis() +
                        ThreadLocalRandom.current().nextInt(PERIOD_MIN_SECOND,  PERIOD_MAX_SECOND + 1);
            }
        } else {
            if (!isNear()) {
                state = MoveState.WANDERING;
                speed = getNextWalkSpeed();
                currentMoveEndMillis = System.currentTimeMillis() +
                        ThreadLocalRandom.current().nextInt(PERIOD_MIN_SECOND,  PERIOD_MAX_SECOND + 1);
            } else if (currentMoveEndMillis <= System.currentTimeMillis()) {
                speed = getNextRunSpeed();
                currentMoveEndMillis = System.currentTimeMillis() +
                        ThreadLocalRandom.current().nextInt(PERIOD_MIN_SECOND,  PERIOD_MAX_SECOND + 1);
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
        for (User user: progressMap.keySet()) {
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
     * 计算追逐进度
     */
    public void countProgress() {
        for(Iterator<Map.Entry<User, Progress>> it = progressMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<User, Progress> entry = it.next();
            User user = entry.getKey();
            Progress progress = entry.getValue();

            int distance = user.getPosition().point.distance(position.point);
            if (distance > DISTANCE_CHASING) {
                it.remove();
            } else if (distance > DISTANCE_CATCHING && progress.step == ChasingStep.CHASING) {
                float percent = (float)(System.currentTimeMillis() - progress.startTime) / SECOND_CHASING;
                progress.percent = percent > 1 ? 1 : (int)percent * 100;
            } else if (distance > DISTANCE_CATCHING && progress.step == ChasingStep.CATCHING) {
                progress.step = ChasingStep.CHASING;
                progress.percent = 0;
            } else if (distance < DISTANCE_CATCHING && progress.step == ChasingStep.CHASING) {
                progress.step = ChasingStep.CATCHING;
                progress.percent = 0;
            } else {
                if (progress.percent < 1) {
                    float percent = (float)(System.currentTimeMillis() - progress.startTime) / SECOND_CATCHING;
                    progress.percent = percent > 1 ? 1 : (int)percent * 100;
                }

                if (progress.percent == 1) {
                    catched(user);
                }
            }
        }
    }

    /**
     * 执行捕获以后的动作
     */
    protected void catched(User user) {
        // for override
    }

    /**
     * 判断追逐的玩家是否接近
     */
    public boolean isNear() {
        int minDistance = DISTANCE_CHASING + 1;
        for (User user: progressMap.keySet()) {
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
        generateNextMoveSpeed();
        long time = System.currentTimeMillis() - lastMoveMillis;
        long distance = time * speed;

        while (true) {
            Point nextPoint = path.peek();
            if (nextPoint == null) break;
            long pointDistance = nextPoint.distance(position.point);
            if (pointDistance < distance) {
                distance = distance - pointDistance;
                position.point = path.pollFirst();
            } else if (pointDistance == distance) {
                position.point = path.pollFirst();
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
        lastMoveMillis = System.currentTimeMillis();
    }

}

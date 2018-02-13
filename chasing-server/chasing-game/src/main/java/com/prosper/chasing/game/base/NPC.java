package com.prosper.chasing.game.base;

import com.prosper.chasing.game.navmesh.Point;

import java.util.*;

import static java.lang.Math.random;

/**
 * Created by deacon on 2017/12/30.
 */
public class NPC {

    private static Random random = new Random();

    public static class NPCConfig {
        public short id;
        public int count;
        public int speed;

        public NPCConfig(short id , int count, int speed) {
            this.id = id;
            this.count = count;
            this.speed = speed;
        }
    }

    private int seqId;
    private short id;
    private Position position;
    private int speed;
    private boolean isPositionChanged;
    private Deque<Point> path;

    public NPC(int seqId, short id, Position position, int speed) {
        this.seqId = seqId;
        this.id = id;
        this.position = position;
        this.speed = speed;
        this.isPositionChanged = true;
        this.path = new LinkedList<>();
    }

    public void move(long time) {
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
                break;
            }
        }
    }

    public boolean isPathEmpty() {
        if (path == null || path.size() == 0) return true;
        return false;
    }

    public short getId() {
        return id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public int getSeqId() {
        return seqId;
    }

    public void setSeqId(int seqId) {
        this.seqId = seqId;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public boolean isPositionChanged() {
        return isPositionChanged;
    }

    public void setPositionChanged(boolean positionChanged) {
        isPositionChanged = positionChanged;
    }

    public Deque<Point> getPath() {
        return path;
    }

    public void setPath(Deque<Point> path) {
        this.path = path;
    }

}

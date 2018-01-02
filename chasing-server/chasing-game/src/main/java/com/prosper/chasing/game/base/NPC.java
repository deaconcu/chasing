package com.prosper.chasing.game.base;

import java.util.Random;

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

    public NPC(int seqId, short id, Position position, int speed) {
        this.seqId = seqId;
        this.id = id;
        this.position = position;
        this.speed = speed;
        this.isPositionChanged = true;
    }

    public void move(long time) {
        // TODO 有些地方是不能去的
        long distance = speed * time / 1000;
        int rotation = random.nextInt(11) - 5 + position.rotateY;
        int x = (int) (Math.sin(rotation) * distance);
        int z = (int) (Math.cos(rotation) * distance);
        position.positionPoint.x += x;
        position.positionPoint.z += z;
        position.rotateY = rotation;
        isPositionChanged = true;
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
}

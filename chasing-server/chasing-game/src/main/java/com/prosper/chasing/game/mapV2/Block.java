package com.prosper.chasing.game.mapV2;

import com.prosper.chasing.game.util.Enums;
import static com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2019/5/13.
 */
public class Block {

    /**
     * block的边长
     */
    public static float WIDTH = 100f;

    private int id;

    private int x;

    private int y;

    boolean[] bridges;

    public Block(int id, int x, int  y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.bridges = new boolean[4];
    }

    public void clear() {
        this.bridges = new boolean[4];
    }

    public void merge(Block block) {
        for (int i = 0; i < 4; i ++) {
            if (block.bridges[i]) bridges[i] = true;
        }
    }

    public boolean hasBridge(BlockDirection direction) {
        if(direction == BlockDirection.RIGHT) return bridges[0];
        else if (direction == BlockDirection.DOWN) return bridges[1];
        else if (direction == BlockDirection.LEFT) return bridges[2];
        else if (direction == BlockDirection.UP) return bridges[3];
        return false;
    }

    public void setBridges(BlockDirection direction, boolean value) {
        if(direction == BlockDirection.RIGHT) bridges[0] = value;
        else if (direction == BlockDirection.DOWN) bridges[1] = value;
        else if (direction == BlockDirection.LEFT) bridges[2] = value;
        else if (direction == BlockDirection.UP) bridges[3] = value;
    }

    public int bridgeCount() {
        int count = 0;
        for (boolean hasBridge: bridges) {
            if (hasBridge) count ++;
        }
        return count;
    }

    public BlockDirection getDirection(Block block) {
        if (block.x - x == 1 && block.y - y == 0) return BlockDirection.RIGHT;
        if (block.x - x == 0 && block.y - y == -1) return BlockDirection.DOWN;
        if (block.x - x == -1 && block.y - y == 0) return BlockDirection.LEFT;
        if (block.x - x == 0 && block.y - y == 1) return BlockDirection.UP;
        return null;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int coordinateX() {
        return (int)((x - 1) * WIDTH + WIDTH / 2) * 1000;
        //return (int)(((x + (y + 1) * 0.5f - (y + 1) / 2) * (INNER_RADIUS * 2f) + randomX) * 1000);
    }

    public int coordinateY() {
        return (int)((y - 1) * WIDTH + WIDTH / 2) * 1000;
        //return (int)((y * (OUTER_RADIUS * 1.5f) + randomY) * 1000);
    }

    public float coordinateXInFloat() {
        return (int)((x - 1) * WIDTH + WIDTH / 2);
    }

    public float coordinateYInFloat() {
        return (int)((y - 1) * WIDTH + WIDTH / 2);
    }

    @Override
    public String toString() {
        return getId() + ": [" + getX() + "," + getY() + "]";
    }

}

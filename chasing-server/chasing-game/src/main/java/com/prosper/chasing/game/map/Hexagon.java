package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2;
import com.prosper.chasing.game.util.Enums;
import com.prosper.chasing.game.util.Enums.HexagonDirection;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2019/3/5.
 */
public class Hexagon {

    /**
     * 六边形中心点到顶点的距离
     */
    public static float OUTER_RADIUS = 100f;

    /**
     * 六边形中心点到边的最短距离，在地图上基本等于一个segment的一半
     */
    public static float INNER_RADIUS = OUTER_RADIUS * 0.866025404f;

    private int id;

    private int x;

    private int y;

    boolean[] bridges;

    private float randomX;

    private float randomY;

    private int openAreaId;

    public Hexagon(int id, int x, int  y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.bridges = new boolean[]{false, false, false, false, false, false};

        randomX = ThreadLocalRandom.current().nextFloat() * 50 - 25;
        randomY = ThreadLocalRandom.current().nextFloat() * 50 - 25;
    }

    public void clear() {
        this.bridges = new boolean[]{false, false, false, false, false, false};
    }

    public Point2 coordinatePoint() {
        return new Point2(coordinateX(), coordinateY());
    }

    public int coordinateX() {
        return (int)(((x + (y + 1) * 0.5f - (y + 1) / 2) * (INNER_RADIUS * 2f) + randomX) * 1000);
    }

    public int coordinateY() {
        return (int)((y * (OUTER_RADIUS * 1.5f) + randomY) * 1000);
    }

    public float coordinateXInFloat() {
        return (x + (y + 1) * 0.5f - (y + 1) / 2) * (INNER_RADIUS * 2f) + randomX;
    }

    public float coordinateYInFloat() {
        return y * (OUTER_RADIUS * 1.5f) + randomY;
    }

    public float originXInFloat() {
        return (x + (y + 1) * 0.5f - (y + 1) / 2) * (INNER_RADIUS * 2f);
    }

    public float originYInFloat() {
        return y * (OUTER_RADIUS * 1.5f);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Hexagon) {
            Hexagon hexagon = (Hexagon) o;
            if (hexagon.getId() == id) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "objectId: " + id + ", x: " + x + ", y: " + y + ", bridge: " + Arrays.toString(bridges);
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

    public int bridgeCount() {
        int count = 0;
        for (boolean hasBridge: bridges) {
            if (hasBridge) count ++;
        }
        return count;
    }

    public boolean getBridge(HexagonDirection direction) {
        if(direction == HexagonDirection.RIGHT) return bridges[0];
        else if (direction == HexagonDirection.DOWN_RIGHT) return bridges[1];
        else if (direction == HexagonDirection.DOWN_LEFT) return bridges[2];
        else if (direction == HexagonDirection.LEFT) return bridges[3];
        else if (direction == HexagonDirection.UP_LEFT) return bridges[4];
        else return bridges[5];
    }

    public void setBridges(HexagonDirection direction, boolean value) {
        if(direction == HexagonDirection.RIGHT) bridges[0] = value;
        else if (direction == HexagonDirection.DOWN_RIGHT) bridges[1] = value;
        else if (direction == HexagonDirection.DOWN_LEFT) bridges[2] = value;
        else if (direction == HexagonDirection.LEFT) bridges[3] = value;
        else if (direction == HexagonDirection.UP_LEFT) bridges[4] = value;
        else if (direction == HexagonDirection.UP_RIGHT) bridges[5] = value;
    }

    public HexagonDirection getDirection(Hexagon hexagon) {
        if (hexagon.getY() % 2 == 1) {
            if (hexagon.x - x == 1 && hexagon.y - y == 0) return HexagonDirection.RIGHT;
            if (hexagon.x - x == 1 && hexagon.y - y == -1) return HexagonDirection.DOWN_RIGHT;
            if (hexagon.x - x == 0 && hexagon.y - y == -1) return HexagonDirection.DOWN_LEFT;
            if (hexagon.x - x == -1 && hexagon.y - y == 0) return HexagonDirection.LEFT;
            if (hexagon.x - x == 0 && hexagon.y - y == 1) return HexagonDirection.UP_LEFT;
            if (hexagon.x - x == 1 && hexagon.y - y == 1) return HexagonDirection.UP_RIGHT;
        } else {
            if (hexagon.x - x == 1 && hexagon.y - y == 0) return HexagonDirection.RIGHT;
            if (hexagon.x - x == 0 && hexagon.y - y == -1) return HexagonDirection.DOWN_RIGHT;
            if (hexagon.x - x == -1 && hexagon.y - y == -1) return HexagonDirection.DOWN_LEFT;
            if (hexagon.x - x == -1 && hexagon.y - y == 0) return HexagonDirection.LEFT;
            if (hexagon.x - x == -1 && hexagon.y - y == 1) return HexagonDirection.UP_LEFT;
            if (hexagon.x - x == 0 && hexagon.y - y == 1) return HexagonDirection.UP_RIGHT;
        }
        return null;
    }

    public HexagonDirection getDirection(int id) {
        if (id == 0) return HexagonDirection.RIGHT;
        if (id == 1) return HexagonDirection.DOWN_RIGHT;
        if (id == 2) return HexagonDirection.DOWN_LEFT;
        if (id == 3) return HexagonDirection.LEFT;
        if (id == 4) return HexagonDirection.UP_LEFT;
        if (id == 5) return HexagonDirection.UP_RIGHT;
        return null;
    }

    public HexagonDirection[] getRoadDirection() {
        HexagonDirection[] directions = new HexagonDirection[bridgeCount()];
        int index = 0;
        for (int i = 0; i < 6; i ++) {
            if (bridges[i]) directions[index ++] = getDirection(i);
        }
        return directions;
    }

    public List<HexagonDirection> getPossibleRoadDirection() {
        List<HexagonDirection> validDirectionList = new LinkedList<>();
        boolean[] occupiedDirections = Arrays.copyOf(bridges, bridges.length);
        int start = 0;
        for (int i = 0; i < 6; i ++) {
            if (occupiedDirections[i] == true) {
                start = i + 2;
                break;
            }
        }
        if (start > 5) start = start - 6;

        for (int i = start; i < start + 6; i ++) {
            if (!occupiedDirections[i % 6]) {
                boolean isValid = true;
                for (int j = i - 1; j <= i + 1; j ++) {
                    int m = j;
                    if (j < 0) m = j + 6;
                    if (j > 5) m = (j - 6) % 6;

                    if (occupiedDirections[m]) {
                        isValid = false;
                        break;
                    }
                }

                if (isValid) {
                    validDirectionList.add(getDirection(i % 6));
                    occupiedDirections[i % 6] = true;
                }
            }
        }
        return validDirectionList;
    }

    public void merge(Hexagon hexagon) {
        for (int i = 0; i < 6; i ++) {
            if (hexagon.bridges[i]) bridges[i] = true;
        }
    }

    public Enums.HexagonOccupiedType getType() {
        if (getOpenAreaId() == 0) return Enums.HexagonOccupiedType.ROAD;
        else return Enums.HexagonOccupiedType.OPEN_AREA;
    }

    public static void main(String... args) {
        System.out.println((-5) % 6);
    }

    public int getOpenAreaId() {
        return openAreaId;
    }

    public void setOpenAreaId(int openAreaId) {
        this.openAreaId = openAreaId;
    }
}

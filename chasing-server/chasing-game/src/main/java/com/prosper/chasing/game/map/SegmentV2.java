package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.base.Point2;
import com.prosper.chasing.game.util.Util;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2019/3/9.
 */
public class SegmentV2 {

    private static final int ROAD_DISTANCE_MAX = 30 * 1000;

    private Hexagon h1;

    private Hexagon h2;

    private Point2[] roadPoints;

    private int roadPointDistance;

    public static class LightSection {
        Point2 p1;
        Point2 p2;

        Point2 p1Edge;
        Point2 p2Edge;

        List<Point2> roadPoints;
    }

    public SegmentV2(Hexagon h1, Hexagon h2) {
        this.h1 = h1;
        this.h2 = h2;

        generateRoadPoints();
    }

    private void generateRoadPoints() {
        int x1 = h1.coordinateX();
        int x2 = h2.coordinateX();
        int y1 = h1.coordinateY();
        int y2 = h2.coordinateY();
        int distance = Util.distance(x1, y1, x2, y2);

        int count = (int)Math.ceil((float)distance / ROAD_DISTANCE_MAX);
        roadPointDistance = distance / count;
        roadPoints = new Point2[count - 1];

        for (int i = 0; i < count - 1; i ++) {
            roadPoints[i] = new Point2(
                    (x1 + (x2 - x1) / count * (i + 1) +
                            ThreadLocalRandom.current().nextInt(1000)  * 10),
                    (y1 + (y2 - y1) / count * (i + 1) +
                            ThreadLocalRandom.current().nextInt(1000)  * 10));
        }
    }

    public int lampSize() {
        return roadPoints.length;
    }

    public Point2 getPoint(int distance) {
        for (int i = 0; i <= roadPoints.length; i ++) {
            int nextRPointDistance = distance(i);
            if (nextRPointDistance <= distance) {
                distance -= nextRPointDistance;
                continue;
            }
            return getPoint(i, distance);
        }
        throw new RuntimeException("distance is out of range");
    }

    private Point2 getPoint(int i, int distance) {
        if (i == 0)
            return getPoint(h1.coordinateX(), h1.coordinateY(),
                    roadPoints[0].x, roadPoints[0].y, distance);
        else if (i == roadPoints.length)
            return getPoint(roadPoints[roadPoints.length - 1].x, roadPoints[roadPoints.length - 1].y,
                    h2.coordinateX(), h2.coordinateY(), distance);
        else
            return getPoint(roadPoints[i - 1].x, roadPoints[i - 1].y,
                    roadPoints[i].x, roadPoints[i].y, distance);
    }

    private Point2 getPoint(int x1, int y1, int x2, int y2, int distance) {
        int wholeDistance = Util.distance(x1, y1, x2, y2);
        if (distance < 0 || distance > wholeDistance)
            throw new RuntimeException("distance is out of range");

        float percent = (float)distance / wholeDistance;
        return new Point2((int)(x1 + (x2 - x1) * percent), (int)(y1 + (y2 - y1) * percent));
    }

    public int getAdjacentPointIndex(int id) {
        if (h1.getId() == id) return 0;
        if (h2.getId() == id) return roadPoints.length - 1;
        return -1;
    }

    public int getId() {
        if (h1.getId() <= h2.getId()) return h1.getId() * 10000 + h2.getId();
        else return h2.getId() * 10000 + h1.getId();
    }

    public static int getId(Hexagon h1, Hexagon h2) {
        return getId(h1.getId(), h2.getId());
    }

    public static int getId(int id1, int id2) {
        if (id1 <= id2) return id1 * 10000 + id2;
        else return id2 * 10000 + id1;
    }

    public Point2[] getRoadPoints() {
        return roadPoints;
    }

    public Hexagon getH1() {
        return h1;
    }

    public Hexagon getH2() {
        return h2;
    }

    private int distance() {
        if (roadPoints.length == 0) return distance(0);

        int distance = 0;
        for (int i = 0; i <= roadPoints.length; i ++) {
            distance += distance(i);
        }
        return distance;
    }

    private int distance(int index) {
        if (roadPoints.length == 0) return Util.distance(
                h1.coordinateX(), h1.coordinateY(), h2.coordinateX(), h2.coordinateY());
        else {
            if (index == 0) {
                return Util.distance(
                        h1.coordinateX(), h1.coordinateY(), roadPoints[0].x, roadPoints[0].y);
            } else if (index == roadPoints.length) {
                return Util.distance(
                        roadPoints[roadPoints.length - 1].x, roadPoints[roadPoints.length - 1].y,
                        h2.coordinateX(), h2.coordinateY());
            } else {
                return Util.distance(
                        roadPoints[index - 1].x, roadPoints[index - 1].y,
                        roadPoints[index].x, roadPoints[index].y);
            }
        }
    }
}

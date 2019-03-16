package com.prosper.chasing.game.base;

import com.prosper.chasing.common.util.Pair;
import com.prosper.chasing.game.util.Util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2018/4/20.
 */
public class Point2 {

    public int x;
    public int y;

    public Point2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private static int distance(Point2 p1, Point2 p2) {
        return (int)Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    public static int distance(Point2 start, Point2 end, Point2[] wayPoints) {
        if (wayPoints.length == 0) return distance(start, end, wayPoints, 0);

        int distance = 0;
        for (int i = 0; i <= wayPoints.length; i ++) {
            distance += distance(start, end, wayPoints, i);
        }
        return distance;
    }

    public static int distance(Point2 start, Point2 end, Point2[] wayPoints, int index) {
        if (wayPoints.length == 0) return Util.distance(start.x, start.y, end.x, end.y);
        else {
            if (index == 0) {
                return Util.distance(start.x, start.y, wayPoints[0].x, wayPoints[0].y);
            } else if (index == wayPoints.length) {
                int lastIndex = wayPoints.length - 1;
                return Util.distance(wayPoints[lastIndex].x, wayPoints[lastIndex].y, end.x, end.y);
            } else {
                return Util.distance(wayPoints[index - 1].x, wayPoints[index - 1].y,
                        wayPoints[index].x, wayPoints[index].y);
            }
        }
    }

    public static Point2[] getPointInSegment(Point2 start, Point2 end, int count, int maxOffset) {
        Point2[] pointSegments = new Point2[count];
        maxOffset ++;
        for (int i = 0; i < count; i ++) {
            pointSegments[i] = new Point2(
                    (start.x + (end.x - start.x) / (count + 1) * (i + 1) +
                            ThreadLocalRandom.current().nextInt(maxOffset)  * 10),
                    (start.y + (end.y - start.y) / (count + 1) * (i + 1) +
                            ThreadLocalRandom.current().nextInt(maxOffset)  * 10));
        }
        return pointSegments;
    }

    public static Pair<Point2, Integer> getPoint(
            Point2 start, Point2 end, Point2[] wayPoints, int distance) {
        for (int i = 0; i <= wayPoints.length; i ++) {
            int nextRPointDistance = distance(start, end, wayPoints, i);
            if (nextRPointDistance <= distance) {
                distance -= nextRPointDistance;
                continue;
            }
            return new Pair<>(getPoint(start, end, wayPoints, i, distance), i - 1);
        }
        throw new RuntimeException("distance is out of range");
    }

    public static Point2 getPoint(
            Point2 start, Point2 end, Point2[] wayPoints, int index, int distance) {
        if (index == 0)
            return getPoint(start, wayPoints[0], distance);
        else if (index == wayPoints.length)
            return getPoint(wayPoints[wayPoints.length - 1], end, distance);
        else
            return getPoint(wayPoints[index - 1], wayPoints[index], distance);
    }

    public static Point2 getPoint(Point2 p1, Point2 p2, int distance) {
        int wholeDistance = distance(p1, p2);
        if (distance < 0 || distance > wholeDistance)
            throw new RuntimeException("distance is out of range");

        float percent = (float)distance / wholeDistance;
        return new Point2(
                (int)(p1.x + (p2.x - p1.x) * percent),
                (int)(p1.y + (p2.y - p1.y) * percent));
    }

    public static double getDegree(Point2 p1, Point2 p2) {
        //return Math.atan2(p2.y, p2.x) - Math.atan2(p1.y, p1.x);
        double value = Math.atan2(p2.y - p1.y, p2.x - p1.x);
        if (value < 0) return 2 * Math.PI + value;
        return value;
        //else return value;
    }

    public static Point2[] getEdge(Point2 p, double radian, int distance) {
        int offsetX = (int)(distance * Math.cos(radian + Math.PI / 2));
        int offsetY = (int)(distance * Math.sin(radian + Math.PI / 2));
        return new Point2[] {
                new Point2(p.x + offsetX, p.y + offsetY),
                new Point2(p.x - offsetX, p.y - offsetY)};
    }

    /**
     * 计算两点的距离
     */
    public int distance (Point2 point) {
        return distance(point.x, point.y);
    }

    public int distance (int x, int y) {
        return (int)Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
    }

    public Point2 center(Point2 point2) {
        return new Point2((x + point2.x) / 2, (y + point2.y) / 2);
    }

    public Point3 toPoint3() {
        return new Point3(x, 0, y);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point2)) {
            return false;
        }

        Point2 point = (Point2) o;
        if (point.x == x && point.y == y) return true;
        return false;
    }

    @Override
    public int hashCode() {
        return new Long(x).hashCode() + new Long(y).hashCode();
    }

    @Override
    public String toString() {
        return "x:" + x + ", y:" + y;
    }

    public float getXInFloat() {
        return (float)x / 1000;
    }

    public float getYInFloat() {
        return (float)y / 1000;
    }
}

package com.prosper.chasing.game.map;

import com.prosper.chasing.common.util.Pair;
import com.prosper.chasing.game.base.Point2;
import com.prosper.chasing.game.base.RoadPoint;
import com.prosper.chasing.game.util.Enums;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2019/3/13.
 */
public class RoadSection {

    public static final int SUB_SECTION_SIZE = 8;
    public static final int ROAD_WIDTH = 7 * 1000;

    private RoadPoint start;
    private RoadPoint end;
    private RoadPoint[] between;

    private RoadPoint[] edgeOfStart;
    private RoadPoint[] edgeOfEnd;
    private RoadPoint[][] edgeOfBetween;

    private Point2[] wayPoints;

    public RoadSection(RoadSection previous, Point2 start, Point2 end, List<Point2> wayPointList) {
        wayPoints = new Point2[wayPointList.size()];
        wayPoints = wayPointList.toArray(wayPoints);

        if (wayPointList.size() == 0) {
            double pointAngle = Point2.getDegree(start, end);
            if (previous != null) {
                pointAngle = (pointAngle + previous.getEnd().getDeflection()) / 2;
                previous.resetP2Deflection(pointAngle);
            }
            this.start = new RoadPoint(start, pointAngle, 0);
            this.end = new RoadPoint(end, pointAngle, 0);

            Point2[] points = Point2.getPointInSegment(start, end, SUB_SECTION_SIZE - 1, 0);
            between =  new RoadPoint[points.length];
            for (int i = 0; i < between.length; i ++) {
                between[i] = new RoadPoint(points[i], pointAngle, i + 1);
            }
        } else {
            double startPointAngle = Point2.getDegree(start, wayPoints[0]);
            if (previous != null) {
                if (Math.abs(previous.getEnd().getDeflection() - startPointAngle) > 0.1) {
                    int a = 1;
                }
                startPointAngle = (startPointAngle + previous.getEnd().getDeflection()) / 2;
                previous.resetP2Deflection(startPointAngle);
            }
            this.start = new RoadPoint(start, startPointAngle, 0);
            double endPointAngle = Point2.getDegree(wayPoints[wayPoints.length - 1], end);
            this.end = new RoadPoint(end, endPointAngle, 0);

            between =  new RoadPoint[SUB_SECTION_SIZE - 1];
            int distanceGap = Point2.distance(start, end, wayPoints) / SUB_SECTION_SIZE;
            for (int i = 0; i < SUB_SECTION_SIZE - 1; i ++) {
                Pair<Point2, Integer> pointInfo = Point2.getPoint(
                        start, end, wayPoints, distanceGap * (i + 1) );
                double centerPointAngle;
                if (pointInfo.getY() == -1) {
                    centerPointAngle = Point2.getDegree(start, pointInfo.getX());
                } else {
                    centerPointAngle = Point2.getDegree(
                            wayPoints[pointInfo.getY()], pointInfo.getX());
                }
                between[i] = new RoadPoint(pointInfo.getX(), centerPointAngle, i + 1);
            }
        }

        Point2[] p1EdgePoints = Point2.getEdge(this.start.getPoint(), this.start.getDeflection(), ROAD_WIDTH);
        edgeOfStart = new RoadPoint[] {
                new RoadPoint(p1EdgePoints[0], this.start.getDeflection()),
                new RoadPoint(p1EdgePoints[1], this.start.getDeflection())};
        Point2[] p2EdgePoints = Point2.getEdge(this.end.getPoint(), this.end.getDeflection(), ROAD_WIDTH);
        edgeOfEnd = new RoadPoint[] {
                new RoadPoint(p2EdgePoints[0], this.end.getDeflection()),
                new RoadPoint(p2EdgePoints[1], this.end.getDeflection())};

        edgeOfBetween = new RoadPoint[SUB_SECTION_SIZE - 1][];
        for (int i = 0; i < between.length; i ++) {
            Point2[] pCenterEdgePoints = Point2.getEdge(
                    between[i].getPoint(), between[i].getDeflection(), ROAD_WIDTH);
            edgeOfBetween[i] = new RoadPoint[] {
                    new RoadPoint(pCenterEdgePoints[0], between[i].getDeflection()),
                    new RoadPoint(pCenterEdgePoints[1], between[i].getDeflection())};
        }
    }

    public void resetP2Deflection(double deflection) {
        end.setDeflection(deflection);
        Point2[] p2EdgePoints = Point2.getEdge(end.getPoint(), end.getDeflection(), ROAD_WIDTH);
        edgeOfEnd = new RoadPoint[] {
                new RoadPoint(p2EdgePoints[0], end.getDeflection()),
                new RoadPoint(p2EdgePoints[1], end.getDeflection())};
    }

    public void resetP1Deflection(double deflection) {
        start.setDeflection(deflection);
        Point2[] p1EdgePoints = Point2.getEdge(start.getPoint(), start.getDeflection(), ROAD_WIDTH);
        edgeOfStart = new RoadPoint[] {
                new RoadPoint(p1EdgePoints[0], start.getDeflection()),
                new RoadPoint(p1EdgePoints[1], start.getDeflection())};
    }

    /**
     * get random road center point
     * @return
     */
    public RoadPoint getRandomRoadPoint(Enums.RoadPointType type) {
        if (type == Enums.RoadPointType.CENTER) {
            return between[ThreadLocalRandom.current().nextInt(between.length)];
        } else {
            RoadPoint[] roadPoints = edgeOfBetween[ThreadLocalRandom.current().nextInt(edgeOfBetween.length)];
            if (roadPoints[0].getPoint().distance(0, 0) > roadPoints[1].getPoint().distance(0, 0)) return roadPoints[0];
            else return roadPoints[1];
        }
    }

    public RoadPoint getRandomLightPoint(Enums.RoadPointType type) {
        boolean isStart = ThreadLocalRandom.current().nextBoolean();
        if (type == Enums.RoadPointType.CENTER) {
            return isStart ? start :end;
        } else {
            RoadPoint[] roadPoints = isStart ? edgeOfStart : edgeOfEnd;
            if (roadPoints[0].getPoint().distance(0, 0) > roadPoints[1].getPoint().distance(0, 0)) return roadPoints[0];
            else return roadPoints[1];
        }
    }

    public RoadPoint getMiddleRPoint() {
        return between[between.length / 2];
    }

    public RoadPoint getStart() {
        return start;
    }

    public RoadPoint getEnd() {
        return end;
    }

    public RoadPoint[] getBetween() {
        return between;
    }

    public RoadPoint[] getEdgeOfStart() {
        return edgeOfStart;
    }

    public RoadPoint[] getEdgeOfEnd() {
        return edgeOfEnd;
    }

    public RoadPoint[][] getEdgeOfBetween() {
        return edgeOfBetween;
    }

    public void setStart(RoadPoint start) {
        this.start = start;
    }

    public void setEnd(RoadPoint end) {
        this.end = end;
    }

    public void setBetween(RoadPoint[] between) {
        this.between = between;
    }

    public void setEdgeOfStart(RoadPoint[] edgeOfStart) {
        this.edgeOfStart = edgeOfStart;
    }

    public void setEdgeOfEnd(RoadPoint[] edgeOfEnd) {
        this.edgeOfEnd = edgeOfEnd;
    }

    public void setEdgeOfBetween(RoadPoint[][] edgeOfBetween) {
        this.edgeOfBetween = edgeOfBetween;
    }

}

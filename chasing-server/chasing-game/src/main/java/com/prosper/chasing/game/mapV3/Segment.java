package com.prosper.chasing.game.mapV3;

import com.prosper.chasing.game.base.Point2;
import com.prosper.chasing.game.base.RoadPoint;
import com.prosper.chasing.game.util.Enums;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2019/3/9.
 */
public class Segment {

    //private static final int LAMP_DISTANCE_MAX = 60 * 1000;

    private Hexagon h1;

    private Hexagon h2;

    /*
    private Point2[] wayPoints;

    private Point2[] terrainPoints;

    private Point2[] lampPoints;

    private RoadSection[] roadSections;

    private int lampDistance;
    */

    public Segment(Hexagon h1, Hexagon h2) {
        this.h1 = h1;
        this.h2 = h2;

        //generateRoadPoints();
        //generateLampPoints();
        //generateLampSections();
    }


    public Hexagon getH1() {
        return h1;
    }

    public Hexagon getH2() {
        return h2;
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

    /*
    private void generateRoadPoints() {
        wayPoints = Point2.getPointInSegment(h1.coordinatePoint(), h2.coordinatePoint(), 5, 1000);
    }

    private void generateLampPoints() {
        int distance = distance();
        int count = (int)Math.ceil((float)distance / LAMP_DISTANCE_MAX);
        lampDistance = distance / (count * 10);

        lampPoints = new Point2[count - 1];
        terrainPoints = new Point2[count * 10 - 1];
        for (int i = 0; i < terrainPoints.length; i ++) {
             Point2 point = Point2.getPoint(h1.coordinatePoint(), h2.coordinatePoint(),
                    wayPoints, lampDistance * (i + 1)).getX();
             terrainPoints[i] = point;
             if ((i + 1) % 10 == 0) lampPoints[(i + 1) / 10 - 1] = point;
        }
    }

    private void generateLampSections() {
        int distance = distance();

        float[] wayPointPercents = new float[wayPoints.length];
        int currDistance = 0;
        for (int i = 0; i < wayPoints.length; i ++) {
            currDistance += distance(i);
            wayPointPercents[i] = (float)currDistance / distance;
        }

        float[] lampPointPercents = new float[lampSize()];
        float lampPointPercent = (float) 1 / (lampSize() + 1);
        for (int i = 0; i < lampSize(); i ++) {
            lampPointPercents[i] = lampPointPercent * (i + 1);
        }

        roadSections = new RoadSection[lampSize() + 1];
        Point2 previousPoint = h1.coordinatePoint();
        List<Point2> wayPointList = new LinkedList<>();
        RoadSection previousRoadSection = null;
        for (int i = 0, j = 0; i < wayPointPercents.length || j < lampPointPercents.length; ) {
            if (i == wayPointPercents.length) {
                RoadSection newRoadSection = new RoadSection(
                        previousRoadSection, previousPoint, lampPoints[j], wayPointList);
                roadSections[j] = newRoadSection;
                previousRoadSection = newRoadSection;
                previousPoint = lampPoints[j ++];
                wayPointList.clear();
                continue;
            }

            if (j == lampPointPercents.length || wayPointPercents[i] <= lampPointPercents[j]) {
                wayPointList.add(wayPoints[i ++]);
                continue;
            } else {
                RoadSection newRoadSection = new RoadSection(
                        previousRoadSection, previousPoint, lampPoints[j], wayPointList);
                roadSections[j] = newRoadSection;
                previousRoadSection = newRoadSection;
                previousPoint = lampPoints[j ++];
                wayPointList.clear();
            }
        }
        roadSections[roadSections.length - 1] =
                new RoadSection(previousRoadSection, previousPoint, h2.coordinatePoint(), wayPointList);
    }

    public RoadSection getLastRoadSection() {
        return roadSections[roadSections.length - 1];
    }

    public RoadSection getFirstRoadSection() {
        return roadSections[0];
    }

    public RoadSection getMiddleRoadSection() {
        return roadSections[roadSections.length / 2];
    }

    public int lampSize() {
        return lampPoints.length;
    }

    public int getAdjacentPointIndex(int id) {
        if (h1.getId() == id) return 0;
        if (h2.getId() == id) return wayPoints.length - 1;
        return -1;
    }





    public Point2[] getWayPoints() {
        return wayPoints;
    }

    public Point2[] getTerrainPoints() {
        return terrainPoints;
    }

    public Point2[] getLampPoints() {
        return lampPoints;
    }

    public RoadSection[] getRoadSections() {
        return roadSections;
    }

    public List<RoadPoint> getAllRoadPoint() {
        List<RoadPoint> roadPointList = new LinkedList<>();
        RoadSection lastRoadSection = null;
        for (RoadSection roadSection: roadSections) {
            roadPointList.add(roadSection.getStart());
            roadPointList.addAll(Arrays.asList(roadSection.getBetween()));
            lastRoadSection = roadSection;
        }
        roadPointList.add(lastRoadSection.getEnd());
        return roadPointList;
    }

    public Hexagon getH1() {
        return h1;
    }

    public Hexagon getH2() {
        return h2;
    }

    private int distance() {
        return Point2.distance(h1.coordinatePoint(), h2.coordinatePoint(), wayPoints);
    }

    private int distance(int index) {
        return Point2.distance(h1.coordinatePoint(), h2.coordinatePoint(), wayPoints, index);
    }

    public RoadPoint getRandomRoadPoint(Enums.RoadPointType type) {
        RoadSection roadSection = getRoadSections()[
                ThreadLocalRandom.current().nextInt(getRoadSections().length)];
        return roadSection.getRandomRoadPoint(type);
    }

    public RoadPoint getRandomLampPoint(Enums.RoadPointType type) {
        RoadSection roadSection = getRoadSections()[
                ThreadLocalRandom.current().nextInt(getRoadSections().length)];
        return roadSection.getRandomLightPoint(type);
    }
    */

}

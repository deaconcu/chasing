package com.prosper.chasing.game.util;

import com.prosper.chasing.game.base.Point2;
import com.prosper.chasing.game.base.RoadPoint;
import com.prosper.chasing.game.base.View;
import com.prosper.chasing.game.map.*;
import com.prosper.chasing.game.map.Branch;
import com.prosper.chasing.game.map.Hexagon;
import com.prosper.chasing.game.map.MapSkeleton;
import com.prosper.chasing.game.map.OpenArea;
import com.prosper.chasing.game.map.RoadSection;
import com.prosper.chasing.game.map.Segment;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by deacon on 2019/3/5.
 */
public class Canvas {

    private MapSkeleton mapSkeleton;

    public Canvas(MapSkeleton mapSkeleton) {
        this.mapSkeleton = mapSkeleton;
    }

    protected void paintGraph(Graphics2D graph2) {
        graph2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graph2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graph2.setStroke(new BasicStroke(8));

        for (Hexagon hexagon: mapSkeleton.occupiedMap.values()) {
            if (hexagon == null) continue;
            graph2.setColor(Color.lightGray);
            fillCenteredHexagon(graph2, hexagon.originXInFloat(), hexagon.originYInFloat(),
                    Hexagon.OUTER_RADIUS, Hexagon.INNER_RADIUS);
        }

        printFixture(graph2);

        paintOpenArea(graph2, mapSkeleton.openAreaList);
        //paintFreeHexgon(graph2, mapSkeleton.freeMap);

        // 画边
        for (Hexagon hexagon: mapSkeleton.occupiedMap.values()) {
            java.util.List<Hexagon> roadSiblingList = mapSkeleton.getRoadSibling(hexagon);
            paintEdge(graph2, hexagon, roadSiblingList);
        }

        // 画所有顶点的位置和信息
        for (Hexagon hexagon: mapSkeleton.occupiedMap.values()) {
            if (!mapSkeleton.vertexSet.contains(hexagon)) {
                //paintNormal(graph2, hexagon);
            } else {
                paintVertex(graph2, hexagon);
            }
        }

        // 画支路的信息：长度和绕路距离
        for (Branch branch: mapSkeleton.branchSet) {
            //paintBranch(graph2, branch);
        }

        /*
        // 画特殊路段填充点的位置
        for (Point2 point: mapSkeleton.pointMap.keySet()) {
            graph2.setColor(Color.blue);
            //fillCenteredCircle(graph2, 1000, 1000, 100);
            fillCenteredCircle(graph2, point.getXInFloat(), point.getYInFloat(), 1);
        }
        */

        for (Segment segment: mapSkeleton.segmentMap.values()) {
            paintSegment(graph2, segment);
        }

        for (Segment segment: mapSkeleton.segmentMap.values()) {
            for (RoadSection roadSection: segment.getRoadSections()) {
                paintRoadSection(graph2, roadSection);
            }
        }

        for (Lamp lamp: mapSkeleton.lampMap.values()) {
            paintLamp(graph2, lamp);
        }

        /*
        for (Hexagon hexagon: mapSkeleton.occupiedMap.values()) {
            if (!mapSkeleton.crossBlockSet.contains(hexagon)) {
                //paintNormal(graph2, hexagon);
            }
        }
        */

        for (View view: mapSkeleton.viewMap.values()) {
            paintView(graph2, view);
        }

        Map<Hexagon, RoadPoint[]> crossRoadPointMap = mapSkeleton.randomBranchCrossList(1.0f);
        for (Map.Entry<Hexagon, RoadPoint[]> entry : crossRoadPointMap.entrySet()) {
            for (RoadPoint roadPoint : entry.getValue()) {
                graph2.setColor(Color.blue);
                //fillCenteredCircle(graph2, 1000, 1000, 100);
                fillCenteredCircle(graph2, roadPoint.getPoint().getXInFloat(), roadPoint.getPoint().getYInFloat(), 15);
            }

            RoadPoint farRoadPoint = null;
            int distance = 0;
            for (RoadPoint roadPoint : entry.getValue()) {
                int currDistance = roadPoint.getPoint().distance(0, 0);
                if (currDistance > distance) {
                    farRoadPoint = roadPoint;
                    distance = currDistance;
                }
            }
            graph2.setColor(Color.red);
            //fillCenteredCircle(graph2, 1000, 1000, 100);
            fillCenteredCircle(graph2, farRoadPoint.getPoint().getXInFloat(), farRoadPoint.getPoint().getYInFloat(), 15);

            Enums.HexagonDirection direction = mapSkeleton.getRoadDirectionToEnd(entry.getKey().getId());
            int degree = Util.getDegree(direction) / 1000;

            graph2.setColor(Color.green);
            fillCenteredCircle(graph2, Math.round(entry.getKey().coordinateXInFloat() + 50 * (float)Math.cos(Math.toRadians(degree))),
                    Math.round(entry.getKey().coordinateYInFloat() + 50 * (float) Math.sin(Math.toRadians(degree))), 12);

        }

        /*
        for (SpecialSection specialSection: mapSkeleton.specialSectionMap.values()) {
            if (!specialSection.isSingle()) continue;

            RoadPoint roadPoint = specialSection.getRoadPoints()[0];
            graph2.setColor(Color.red);
            int degree = roadPoint.getDegree() / 1000;
            drawCenteredString(graph2, Integer.toString(degree) , new Rectangle(
                    (int)roadPoint.getPoint().getXInFloat(), (int)(roadPoint.getPoint().getYInFloat()), 0, 2), 22);
        }
        */



    }

    private void printFixture(Graphics2D g) {
        for (MapSkeleton.Fixture fixture: mapSkeleton.fixtureList) {
            Hexagon hexagon1 = mapSkeleton.hexagonMap.get(fixture.id1);
            Hexagon hexagon2 = mapSkeleton.hexagonMap.get(fixture.id2);

            if (fixture.fixtureType == Enums.FixtureType.GATE) g.setColor(Color.red);
            else if (fixture.fixtureType == Enums.FixtureType.GRASS) g.setColor(Color.yellow);
            else if (fixture.fixtureType == Enums.FixtureType.WHEEL) g.setColor(Color.green);
            else if (fixture.fixtureType == Enums.FixtureType.STORE) g.setColor(Color.blue);
            else if (fixture.fixtureType == Enums.FixtureType.PORTAL) g.setColor(Color.cyan);
            if (hexagon1 != null) {
                fillCenteredHexagon(g, hexagon1.originXInFloat(), hexagon1.originYInFloat(),
                        Hexagon.OUTER_RADIUS, Hexagon.INNER_RADIUS);
            }
            if (hexagon2 != null) {
                fillCenteredHexagon(g, hexagon2.originXInFloat(), hexagon2.originYInFloat(),
                        Hexagon.OUTER_RADIUS, Hexagon.INNER_RADIUS);
            }
        }
    }

    private void paintOpenArea(Graphics2D g, java.util.List<OpenArea> openAreas) {

        for (OpenArea openArea: openAreas) {
            for (Point2[] connectPoint: openArea.getConnectPoints()) {
                g.drawLine(Math.round(connectPoint[0].getXInFloat()), Math.round(connectPoint[0].getYInFloat()),
                        Math.round(connectPoint[1].getXInFloat()), Math.round(connectPoint[1].getYInFloat()));
            }

            for (Hexagon hexagon: openArea.getArea()) {
                if (hexagon == null) continue;
                g.setColor(Color.orange);
                fillCenteredHexagon(g, hexagon.originXInFloat(), hexagon.originYInFloat(),
                        Hexagon.OUTER_RADIUS, Hexagon.INNER_RADIUS);
                g.setColor(Color.black);
                drawCenteredString(g, Integer.toString(hexagon.getOpenAreaId()), new Rectangle(
                        (int)hexagon.originXInFloat(), (int)hexagon.originYInFloat(), 0, 2), 42);
            }
        }
    }

    private void paintFreeHexgon(Graphics2D g, Map<Integer, Hexagon> freeMap) {
        g.setColor(Color.gray);

        for (Hexagon hexagon: freeMap.values()) {
            if (hexagon == null) continue;
            fillCenteredHexagon(g, hexagon.originXInFloat(), hexagon.originYInFloat(),
                    Hexagon.OUTER_RADIUS, Hexagon.INNER_RADIUS);
        }
    }

    private void paintBranch(Graphics2D g, Branch branch) {
        g.setColor(Color.white);

        Point2 point = branch.getCenterPoint();
        fillCenteredCircle(g, point.getXInFloat(), point.getYInFloat(), 45);

        String text1 = Integer.toString(branch.distance());
        String text2 = Integer.toString(branch.detourDistance);

        g.setColor(Color.darkGray);
        drawCenteredString(g, text1, new Rectangle(
                (int)point.getXInFloat(), (int)(point.getYInFloat() - 10), 0, 2), 22);
        drawCenteredString(g, text2, new Rectangle(
                (int)point.getXInFloat(), (int)point.getYInFloat() + 10, 0, 2), 22);
    }

    private void paintSegment(Graphics2D g, Segment segment) {
        g.setColor(Color.green);
        for (Point2 point: segment.getWayPoints()) {
            //fillCenteredCircle(g, point.getXInFloat(), point.getYInFloat(), 10);
        }
        for (Point2 point: segment.getTerrainPoints()) {
            //fillCenteredCircle(g, point.getXInFloat(), point.getYInFloat(), 1);
        }
    }

    private void paintLamp(Graphics2D g, Lamp lamp) {
        g.setColor(Color.red);
        fillCenteredCircle(g, lamp.getPoint3().getXInFloat(), lamp.getPoint3().getZInFloat(), 2);
    }

    private void paintView(Graphics2D g, View view) {
        g.setColor(Color.ORANGE);
        fillCenteredCircle(g, view.getPoint3().getXInFloat(), view.getPoint3().getZInFloat(), 4);
    }

    private void paintRoadSection(Graphics2D g, RoadSection roadSection) {
        g.setColor(Color.green);
        RoadPoint start = roadSection.getStart();
        fillCenteredCircle(g, start.getPoint().getXInFloat(), start.getPoint().getYInFloat(), 1);
        drawCenteredString(g, Integer.toString(roadSection.getStart().getDegree() / 1000), new Rectangle(
                Math.round(start.getPoint().getXInFloat()), Math.round(start.getPoint().getYInFloat()), 0, 2), 15);

        RoadPoint end = roadSection.getStart();
        fillCenteredCircle(g, end.getPoint().getXInFloat(), end.getPoint().getYInFloat(), 1);

        for (int i = 0; i < roadSection.getBetween().length; i ++) {
            fillCenteredCircle(g, roadSection.getBetween()[i].getPoint().getXInFloat(),
                    roadSection.getBetween()[i].getPoint().getYInFloat(), 1);
        }

        for (RoadPoint roadPoint: roadSection.getEdgeOfStart()) {
            fillCenteredCircle(g, roadPoint.getPoint().getXInFloat(), roadPoint.getPoint().getYInFloat(), 1);
        }
        for (RoadPoint roadPoint: roadSection.getEdgeOfEnd()) {
            fillCenteredCircle(g, roadPoint.getPoint().getXInFloat(), roadPoint.getPoint().getYInFloat(), 1);
        }
        for (int i = 0; i < roadSection.getEdgeOfBetween().length; i ++) {
            for (RoadPoint roadPoint : roadSection.getEdgeOfBetween()[i]) {
                fillCenteredCircle(g, roadPoint.getPoint().getXInFloat(), roadPoint.getPoint().getYInFloat(), 1);
            }
        }
    }

    private void paintEdge(Graphics2D g, Hexagon hexagon, java.util.List<Hexagon> roadSibling) {
        for (Hexagon sibling: roadSibling) {
            g.setColor(Color.darkGray);
            if (sibling == null || hexagon.getId() > sibling.getId()) continue;
            int segmentId = Segment.getId(hexagon, sibling);
            Segment segment = mapSkeleton.segmentMap.get(segmentId);
            if  (segment.getSsType() == Enums.SpecialSectionType.RAIN) {
                g.setColor(Color.YELLOW);
            } else if  (segment.getSsType() == Enums.SpecialSectionType.SNOW) {
                g.setColor(Color.blue);
            } else if  (segment.getSsType() == Enums.SpecialSectionType.DREAM) {
                g.setColor(Color.cyan);
            } else if  (segment.getSsType() == Enums.SpecialSectionType.WATER) {
                g.setColor(Color.GREEN);
            } else if  (segment.getSsType() == Enums.SpecialSectionType.GRAVEYARD) {
                g.setColor(Color.RED);
            } else if  (segment.getSsType() == Enums.SpecialSectionType.INVISIBLE) {
                g.setColor(Color.ORANGE);
            } else if  (segment.getSsType() == Enums.SpecialSectionType.GATES) {
                g.setColor(Color.pink);
            }

            for (int i = 0; i <= segment.getWayPoints().length; i ++) {
                if (i == 0) {
                    g.drawLine(Math.round(hexagon.coordinateXInFloat()), Math.round(hexagon.coordinateYInFloat()),
                            Math.round(segment.getWayPoints()[i].x / 1000), Math.round(segment.getWayPoints()[i].y / 1000));
                } else if (i == segment.getWayPoints().length) {
                    g.drawLine(Math.round(segment.getWayPoints()[i - 1].x / 1000), Math.round(segment.getWayPoints()[i - 1].y / 1000),
                            Math.round(sibling.coordinateXInFloat()), Math.round(sibling.coordinateYInFloat()));
                } else {
                    g.drawLine(Math.round(segment.getWayPoints()[i - 1].x / 1000), Math.round(segment.getWayPoints()[i - 1].y / 1000),
                            Math.round(segment.getWayPoints()[i].x / 1000), Math.round(segment.getWayPoints()[i].y / 1000));
                }
            }
        }
    }

    private void paintVertex(Graphics2D g, Hexagon hexagon) {
        g.setColor(Color.WHITE);
        g.fillOval(Math.round(hexagon.coordinateXInFloat()) - 50, Math.round(hexagon.coordinateYInFloat() - 50), 100, 100);
        g.setColor(Color.BLACK);
        g.drawOval(Math.round(hexagon.coordinateXInFloat()) - 50, Math.round(hexagon.coordinateYInFloat()) - 50, 100, 100);

        String text1 = Integer.toString(hexagon.getId());
        String text2 = Integer.toString(mapSkeleton.getDistanceToEnd(hexagon.getId()));

        g.setColor(Color.darkGray);
        drawCenteredString(g, text1, new Rectangle(
                Math.round(hexagon.coordinateXInFloat()), Math.round(hexagon.coordinateYInFloat() - 18), 0, 2), 30);
        drawCenteredString(g, text2, new Rectangle(
                Math.round(hexagon.coordinateXInFloat()), Math.round(hexagon.coordinateYInFloat() + 18), 0, 2), 30);

        Enums.HexagonDirection direction = mapSkeleton.getRoadDirectionToEnd(hexagon.getId());
        int degree2 = Util.getDegree(direction) / 1000;

        g.setColor(Color.orange);
        fillCenteredCircle(g, Math.round(hexagon.coordinateXInFloat() + 50 * (float)Math.cos(Math.toRadians(degree2))),
                Math.round(hexagon.coordinateYInFloat() + 50 * (float) Math.sin(Math.toRadians(degree2))), 12);
    }

    private void paintNormal(Graphics2D g, Hexagon hexagon) {
        g.setColor(Color.black);
        String text = Integer.toString(hexagon.getId());
        drawCenteredString(g, text, new Rectangle(
                Math.round(hexagon.coordinateXInFloat()) + 5, Math.round(hexagon.coordinateYInFloat() + 5), 0, 2), 12);
        //g.fillRect(Math.round(hexagon.coordinateXInFloat()) - squareW / 2, Math.round(hexagon.coordinateYInFloat() - squareH / 2),
                //squareW,squareH);
        //g.setColor(Color.BLACK);
        //g.drawRect(Math.round(hexagon.coordinateXInFloat()),Math.round(hexagon.coordinateYInFloat()),squareW,squareH);
    }

    public void drawCenteredString(Graphics g, String text, Rectangle rect, int size) {
        Font font = new Font("Arial", Font.BOLD, size);
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

    public void fillCenteredCircle(Graphics2D g, float x, float y, float r) {
        int positionX = (int)(x-(r/2));
        int positionY = (int)(y-(r/2));
        //g.fillOval(x,y,r,r);
        g.fillOval(positionX, positionY, (int)r, (int)r);
    }

    public void fillCenteredHexagon(Graphics2D g, float x, float y, float r1, float r2) {
        int[] xs = new int[] {(int)(x - r2), (int)x, (int)(x + r2), (int)(x + r2), (int)x, (int)(x -r2)};
        int[] ys = new int[] {
                (int)(y + r1 * Math.sin(Util.getRadians(30))), (int)(y + r1),
                (int)(y + r1 * Math.sin(Util.getRadians(30))), (int)(y - r1 * Math.sin(Util.getRadians(30))),
                (int)(y - r1), (int)(y - r1 * Math.sin(Util.getRadians(30)))
        };

        g.fillPolygon(xs, ys, 6);
    }

    public void drawCenteredHexagon(Graphics2D g, float x, float y, float r1, float r2) {
        int[] xs = new int[] {(int)(x - r2), (int)x, (int)(x + r2), (int)(x + r2), (int)x, (int)(x -r2)};
        int[] ys = new int[] {
                (int)(y + r1 * Math.sin(Util.getRadians(30))), (int)(y + r1),
                (int)(y + r1 * Math.sin(Util.getRadians(30))), (int)(y - r1 * Math.sin(Util.getRadians(30))),
                (int)(y - r1), (int)(y - r1 * Math.sin(Util.getRadians(30)))
        };

        g.drawPolygon(xs, ys, 6);
    }

    public void fillCenteredRect(Graphics2D g, int x, int y, int r) {
        int positionX = (int)(x-(r/2));
        int positionY = (int)(y-(r/2));
        g.fillRect(positionX, positionY, (int)r, (int)r);
    }

    public static void createAndShowGUI(MapSkeleton mapSkeleton) {
        try {
            System.out.println("print map ....");
            Canvas canvas = new Canvas(mapSkeleton);
            BufferedImage bi = new BufferedImage(12000, 12000, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = bi.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, 12000, 12000);

            canvas.paintGraph(g);

            g.dispose();
            ImageIO.write(bi, "png", new File("/Users/deacon/Desktop/table.png"));
            System.out.println("print map done!");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        MapSkeleton mapSkeleton = new MapSkeleton(60, 100)
                .merge(new MapSkeleton(60, 100))
                .merge(new MapSkeleton(60, 100))
                .merge(new MapSkeleton(60, 100))
                .merge(new MapSkeleton(60, 100));

        mapSkeleton.optimize();
        mapSkeleton.generateTerrainV3();
        mapSkeleton.generateFixture();
        mapSkeleton.toBytesV2();
        //mapSkeleton.toBytes();

        createAndShowGUI(mapSkeleton);
    }
}

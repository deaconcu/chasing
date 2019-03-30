package com.prosper.chasing.game.util;

import com.prosper.chasing.game.base.InteractiveObjects;
import com.prosper.chasing.game.base.Point2;
import com.prosper.chasing.game.base.RoadPoint;
import com.prosper.chasing.game.base.View;
import com.prosper.chasing.game.map.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

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
            paintBranch(graph2, branch);
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
            if (!mapSkeleton.vertexSet.contains(hexagon)) {
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
            int degree = Util.getDegree(direction);

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
        fillCenteredCircle(g, roadSection.getStart().getPoint().getXInFloat(),
                roadSection.getStart().getPoint().getYInFloat(), 1);
        fillCenteredCircle(g, roadSection.getEnd().getPoint().getXInFloat(),
                roadSection.getEnd().getPoint().getYInFloat(), 1);

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
        g.setColor(Color.darkGray);

        for (Hexagon sibling: roadSibling) {
            if (sibling == null || hexagon.getId() > sibling.getId()) continue;
            int segmentId = Segment.getId(hexagon, sibling);
            Segment segment = mapSkeleton.segmentMap.get(segmentId);
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
        int degree2 = Util.getDegree(direction);

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
            throw new RuntimeException();
        }
    }

    public static void main(String[] args) throws IOException {
        MapSkeleton mapSkeleton = new MapSkeleton(30, 100)
                .merge(new MapSkeleton(30, 100))
                .merge(new MapSkeleton(30, 100))
                .merge(new MapSkeleton(30, 100))
                .merge(new MapSkeleton(30, 100));

        mapSkeleton.optimize();
        mapSkeleton.generateTerrain();
        mapSkeleton.toBytes();

        createAndShowGUI(mapSkeleton);
    }
}

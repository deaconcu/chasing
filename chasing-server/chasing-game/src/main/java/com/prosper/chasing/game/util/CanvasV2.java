package com.prosper.chasing.game.util;

import com.prosper.chasing.game.mapV2.Block;
import com.prosper.chasing.game.mapV2.GameMap;
import com.prosper.chasing.game.mapV2.Segment;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by deacon on 2019/5/14.
 */
public class CanvasV2 {

    private GameMap gameMap;

    public CanvasV2(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    protected void paintGraph(Graphics2D graph2) {
        graph2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graph2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graph2.setStroke(new BasicStroke(8));

        // 画边
        paintEdge(graph2);

        /*
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

        // 画特殊路段填充点的位置
        for (Point2 point: mapSkeleton.pointMap.keySet()) {
            graph2.setColor(Color.blue);
            //fillCenteredCircle(graph2, 1000, 1000, 100);
            fillCenteredCircle(graph2, point.getXInFloat(), point.getYInFloat(), 1);
        }

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
        */

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

    /**
     * 绘制所有道路段
     */
    private void paintEdge(Graphics2D g) {
        for (Block block: gameMap.occupiedMap.values()) {
            g.setColor(Color.darkGray);
            for (Block sibling: gameMap.getRoadSibling(block)) {
                if (sibling == null || block.getId() > sibling.getId()) continue;
                int segmentId = Segment.getId(block, sibling);
                Segment segment = gameMap.segmentMap.get(segmentId);
                g.drawLine(
                        Math.round(segment.getH1().coordinateXInFloat()),
                        Math.round(segment.getH1().coordinateYInFloat()),
                        Math.round(segment.getH2().coordinateXInFloat()),
                        Math.round(segment.getH2().coordinateYInFloat())
                );
            }
        }
    }

    public static void createAndShowGUI(GameMap gameMap) {
        try {
            System.out.println("print map ....");
            CanvasV2 canvas = new CanvasV2(gameMap);
            BufferedImage bi = new BufferedImage(12000, 12000, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = bi.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, 12000, 12000);

            canvas.paintGraph(g);

            g.dispose();
            ImageIO.write(bi, "png", new File("/Users/deacon/Desktop/game_map.png"));
            System.out.println("print map done!");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        GameMap gameMap = new GameMap(30, 100)
                .merge(new GameMap(30, 100))
                .merge(new GameMap(30, 100))
                .merge(new GameMap(30, 100))
                .merge(new GameMap(30, 100));

        gameMap.optimize();
        //gameMap.generateTerrain();
        //gameMap.toBytes();

        createAndShowGUI(gameMap);
    }
}

package com.prosper.chasing.game.util;

import com.prosper.chasing.game.base.Point2D;
import com.prosper.chasing.game.map.Hexagon;
import com.prosper.chasing.game.map.MapSkeleton;
import com.prosper.chasing.game.map.Branch;
import com.prosper.chasing.game.map.Segment;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by deacon on 2019/3/5.
 */
public class Canvas  extends JPanel {

    private MapSkeleton mapSkeleton;

    private int squareW = 4;
    private int squareH = 4;

    public Canvas(MapSkeleton mapSkeleton) {
        this.mapSkeleton = mapSkeleton;
        setBackground(Color.WHITE);
    }

    public Dimension getPreferredSize() {
        return new Dimension(12000, 12000);
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graph2 = (Graphics2D) g;
        graph2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graph2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graph2.setStroke(new BasicStroke(8));

        for (Hexagon hexagon: mapSkeleton.occupiedMap.values()) {
            java.util.List<Hexagon> roadSiblingList = mapSkeleton.getRoadSibling(hexagon);
            paintEdge(graph2, hexagon, roadSiblingList);
        }

        for (Hexagon hexagon: mapSkeleton.occupiedMap.values()) {
            if (!mapSkeleton.vertexSet.contains(hexagon)) {
                //paintNormal(graph2, hexagon);
            } else {
                paintVertex(graph2, hexagon);
            }
        }

        for (Branch segment: mapSkeleton.branchSet) {
            paintSegment(graph2, segment);
        }
    }

    private void paintSegment(Graphics2D g, Branch segment) {
        g.setColor(Color.white);

        Point2D point = segment.getCenterPoint();
        fillCenteredCircle(g, point.x, point.y, 45);

        String text1 = Integer.toString(segment.distance());
        String text2 = Integer.toString(segment.detourDistance);

        g.setColor(Color.darkGray);
        drawCenteredString(g, text1, new Rectangle(point.x, point.y - 10, 0, 2), 22);
        drawCenteredString(g, text2, new Rectangle(point.x, point.y + 10, 0, 2), 22);
    }

    private void paintEdge(Graphics2D g, Hexagon hexagon, java.util.List<Hexagon> roadSibling) {
        g.setColor(Color.darkGray);

        for (Hexagon sibling: roadSibling) {
            if (sibling == null || hexagon.getId() > sibling.getId()) continue;
            int segmentId = Segment.getId(hexagon, sibling);
            Segment segment = mapSkeleton.segmentMap.get(segmentId);
            for (int i = 0; i <= segment.getPoints().length; i ++) {
                if (i == 0) {
                    g.drawLine(Math.round(hexagon.coordinateX()), Math.round(hexagon.coordinateY()),
                            Math.round(segment.getPoints()[i].x / 1000), Math.round(segment.getPoints()[i].y / 1000));
                } else if (i == segment.getPoints().length) {
                    g.drawLine(Math.round(segment.getPoints()[i - 1].x / 1000), Math.round(segment.getPoints()[i - 1].y / 1000),
                            Math.round(sibling.coordinateX()), Math.round(sibling.coordinateY()));
                } else {
                    g.drawLine(Math.round(segment.getPoints()[i - 1].x / 1000), Math.round(segment.getPoints()[i - 1].y / 1000),
                            Math.round(segment.getPoints()[i].x / 1000), Math.round(segment.getPoints()[i].y / 1000));
                }
            }
        }
    }

    private void paintVertex(Graphics2D g, Hexagon hexagon) {
        g.setColor(Color.WHITE);
        g.fillOval(Math.round(hexagon.coordinateX()) - 50, Math.round(hexagon.coordinateY() - 50), 100, 100);
        g.setColor(Color.BLACK);
        g.drawOval(Math.round(hexagon.coordinateX()) - 50, Math.round(hexagon.coordinateY()) - 50, 100, 100);

        String text1 = Integer.toString(hexagon.getId());
        String text2 = Integer.toString(mapSkeleton.getDistanceToEnd(hexagon.getId()));

        g.setColor(Color.darkGray);
        drawCenteredString(g, text1, new Rectangle(
                Math.round(hexagon.coordinateX()), Math.round(hexagon.coordinateY() - 18), 0, 2), 30);
        drawCenteredString(g, text2, new Rectangle(
                Math.round(hexagon.coordinateX()), Math.round(hexagon.coordinateY() + 18), 0, 2), 30);

        Enums.HexagonDirection direction = mapSkeleton.getRoadDirectionToEnd(hexagon.getId());
        int degree2 = Util.getDegree(direction);

        g.setColor(Color.orange);
        fillCenteredCircle(g, Math.round(hexagon.coordinateX() + 50 * (float)Math.cos(Math.toRadians(degree2))),
                Math.round(hexagon.coordinateY() + 50 * (float) Math.sin(Math.toRadians(degree2))), 12);
    }

    private void paintNormal(Graphics2D g, Hexagon hexagon) {
        g.setColor(Color.darkGray);
        g.fillRect(Math.round(hexagon.coordinateX()) - squareW / 2, Math.round(hexagon.coordinateY() - squareH / 2),
                squareW,squareH);
        //g.setColor(Color.BLACK);
        //g.drawRect(Math.round(hexagon.coordinateX()),Math.round(hexagon.coordinateY()),squareW,squareH);
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

    public void fillCenteredCircle(Graphics2D g, int x, int y, int r) {
        x = x-(r/2);
        y = y-(r/2);
        g.fillOval(x,y,r,r);
    }

    public void fillCenteredRect(Graphics2D g, int x, int y, int r) {
        x = x-(r/2);
        y = y-(r/2);
        g.fillRect(x,y,r,r);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    createAndShowGUI();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void createAndShowGUI() throws IOException {
        MapSkeleton mapSkeleton = new MapSkeleton(30, 100)
                .merge(new MapSkeleton(30, 100))
                .merge(new MapSkeleton(30, 100))
                .merge(new MapSkeleton(30, 100))
                .merge(new MapSkeleton(30, 100));

        mapSkeleton.optimize();
        mapSkeleton.toBytes();
        //mapSkeleton = mapSkeleton.expand();
        Canvas canvas = new Canvas(mapSkeleton);
        System.out.println("ok");

        System.out.println("Created GUI on EDT? "+
                SwingUtilities.isEventDispatchThread());
        JFrame f = new JFrame("Swing Paint Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(canvas);
        f.pack();

        BufferedImage bi = new BufferedImage(12000, 12000, BufferedImage.TYPE_INT_RGB);

        Graphics g = bi.createGraphics();
        canvas.paint(g);

        g.dispose();
        //JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(bi)));
        ImageIO.write(bi, "png", new File("/Users/deacon/Desktop/table.png"));

        System.exit(0);
        //f.setVisible(true);
    }
}

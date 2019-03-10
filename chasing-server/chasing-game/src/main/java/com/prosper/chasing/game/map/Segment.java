package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2019/3/9.
 */
public class Segment {

    private Hexagon h1;

    private Hexagon h2;

    private Point2D[] points;

    public Segment(Hexagon h1, Hexagon h2) {
        this.h1 = h1;
        this.h2 = h2;

        points = new Point2D[5];

        float x1 = h1.coordinateX();
        float x2 = h2.coordinateX();
        float y1 = h1.coordinateY();
        float y2 = h2.coordinateY();

        for (int i = 0; i < 5; i ++) {
            points[i] = new Point2D(
                    (int)((x1 + (x2 - x1) / 6 * (i + 1) + ThreadLocalRandom.current().nextFloat()  * 10) * 1000),
                    (int)((y1 + (y2 - y1) / 6 * (i + 1) + ThreadLocalRandom.current().nextFloat()  * 10) * 1000));
        }
    }

    public int getAdjacentPointIndex(int id) {
        if (h1.getId() == id) return 0;
        if (h2.getId() == id) return points.length - 1;
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

    public Point2D[] getPoints() {
        return points;
    }

    public Hexagon getH1() {
        return h1;
    }

    public Hexagon getH2() {
        return h2;
    }

}

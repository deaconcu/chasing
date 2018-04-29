package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2018/4/20.
 */
public class Point2D {

    public int x;
    public int y;

    public Point2D(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point2D)) {
            return false;
        }

        Point2D point = (Point2D) o;
        if (point.x == x && point.y == y) return true;
        return false;
    }

    @Override
    public int hashCode() {
        return new Integer(x).hashCode() + new Integer(y).hashCode();
    }

    @Override
    public String toString() {
        return "x:" + x + ", y:" + y;
    }
}

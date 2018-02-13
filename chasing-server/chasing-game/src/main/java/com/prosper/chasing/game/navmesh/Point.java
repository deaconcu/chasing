package com.prosper.chasing.game.navmesh;

/**
 * Created by deacon on 2018/2/3.
 */
public class Point {
    public int x;
    public int y;
    public int z;

    public Point () {
    }

    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point add(Point point, double ratio) {
        return add(point.x, point.y, point.z, ratio);
    }

    public Point add(int x, int y, int z, double ratio) {
        return new Point(this.x + (int) (x * ratio), this.y + (int) (y * ratio), this.z + (int) (z * ratio));
    }

    public int distance (Point point) {
        return (int)Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2) + Math.pow(point.z - z, 2));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point)) {
            return false;
        }

        Point point = (Point) o;
        if (x == point.x && y == point.y && z == point.z) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (Integer.valueOf(x)).hashCode() + (Integer.valueOf(y)).hashCode() + (Integer.valueOf(z)).hashCode();
    }

    @Override
    public String toString() {
        return "x: " + x + ", y: " + y + ", z: " + z;

    }
}

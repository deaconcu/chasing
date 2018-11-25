package com.prosper.chasing.game.base;

/**
 * 地图上的位置, 类型为int，为实际位置(float) * 1000
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

    /**
     * 移动
     * @param point 移动方向
     * @param ratio 移动比例
     * @return 移动后的位置
     */
    public Point add(Point point, double ratio) {
        return add(point.x, point.y, point.z, ratio);
    }

    /**
     * @see this.add(Point point, double ratio)
     */
    public Point add(int x, int y, int z, double ratio) {
        return new Point(this.x + (int) (x * ratio), this.y + (int) (y * ratio), this.z + (int) (z * ratio));
    }

    /**
     * 计算两点的距离
     */
    public int distance (Point point) {
        return distance(point.x, point.y, point.z);
    }

    public int distance(int pointX, int pointY, int pointZ) {
        return (int)Math.sqrt(Math.pow(pointX - x, 2) + Math.pow(pointY - y, 2) + Math.pow(pointZ - z, 2));
    }

    /**
     * 计算在x,y坐标轴上的最小距离
     * @param point
     * @return
     */
    public int minDistanceOfAxis(Point point) {
        int distanceX = point.x - x;
        int distanceY = point.y - y;
        return distanceX <= distanceY ? distanceX : distanceY;
    }

    /**
     * 计算两点的向量
     */
    public Point subtraction (Point point) {
        return new Point(point.x - x, point.y - y, point.z - z);
    }

    /**
     * 获得向量长度
     */
    public int length() {
        return (int)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    /**
     * 获得单位向量
     */
    public Point normalized(){
        int length = length();
        return new Point(x / length, y / length, z / length);
    }

    /**
     * 是否向量为0
     */
    public boolean isZero() {
        if (x == 0 && y == 0 && z == 0) return true;
        return false;
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

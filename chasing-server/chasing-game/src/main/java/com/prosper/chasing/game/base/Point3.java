package com.prosper.chasing.game.base;

/**
 * 地图上的位置, 类型为int，为实际位置(float) * 1000
 */
public class Point3 {
    public int x;
    public int y;
    public int z;

    public Point3() {
    }

    public Point3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * 移动
     * @param point3 移动方向
     * @param ratio 移动比例
     * @return 移动后的位置
     */
    public Point3 add(Point3 point3, double ratio) {
        return add(point3.x, point3.y, point3.z, ratio);
    }

    /**
     * @see this.add( Point3 point, double ratio)
     */
    public Point3 add(int x, int y, int z, double ratio) {
        return new Point3(this.x + (int) (x * ratio), this.y + (int) (y * ratio), this.z + (int) (z * ratio));
    }

    public float getXInFloat() {
        return (float)x / 1000;
    }

    public float getZInFloat() {
        return (float)z / 1000;
    }

    /**
     * 计算两点的距离
     */
    public int distance (Point3 point3) {
        return distance(point3.x, point3.y, point3.z);
    }

    public int distance(int pointX, int pointY, int pointZ) {
        return (int)Math.sqrt(Math.pow(pointX - x, 2) + Math.pow(pointY - y, 2) + Math.pow(pointZ - z, 2));
    }

    /**
     * 计算在x,y坐标轴上的最小距离
     * @param point3
     * @return
     */
    public int minDistanceOfAxis(Point3 point3) {
        int distanceX = point3.x - x;
        int distanceY = point3.y - y;
        return distanceX <= distanceY ? distanceX : distanceY;
    }

    /**
     * 计算两点的向量
     */
    public Point3 subtraction (Point3 point3) {
        return new Point3(point3.x - x, point3.y - y, point3.z - z);
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
    public Point3 normalized(){
        int length = length();
        return new Point3(x / length, y / length, z / length);
    }

    /**
     * 是否向量为0
     */
    public boolean isZero() {
        if (x == 0 && y == 0 && z == 0) return true;
        return false;
    }

    /**
     * 是否在同一分区
     */
    public boolean sameZone(Point3 point3) {
        if (point3.x / 1000 == x && point3.y / 1000 == y && point3.z / 1000 == z) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point3)) {
            return false;
        }

        Point3 point3 = (Point3) o;
        if (x == point3.x && y == point3.y && z == point3.z) {
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

package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2019/3/13.
 */
public class RoadPoint {

    private Point2 point;
    private double deflection;
    private boolean isQuarter;

    public RoadPoint(Point2 point, double deflection, boolean isQuarter) {
        this.point = point;
        this.deflection = deflection;
        this.isQuarter = isQuarter;
    }

    public RoadPoint(Point2 point, double deflection) {
        this(point, deflection, false);
    }

    public int getDegree() {
        int degree = (int)Math.toDegrees(deflection) * 1000;
        if (degree < 0) return degree % 360 + 360;
        else return degree % 360;
    }

    public Point2 getPoint() {
        return point;
    }

    public double getDeflection() {
        return deflection;
    }

    public void setPoint(Point2 point) {
        this.point = point;
    }

    public void setDeflection(double deflection) {
        this.deflection = deflection;
    }

    public boolean isQuarter() {
        return isQuarter;
    }
}

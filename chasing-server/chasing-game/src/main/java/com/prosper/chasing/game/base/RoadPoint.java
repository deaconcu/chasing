package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2019/3/13.
 */
public class RoadPoint {

    private Point2 point;
    private double deflection;
    private int divisionPos;

    public RoadPoint(Point2 point, double deflection, int divisionPos) {
        this.point = point;
        this.deflection = deflection;
        this.divisionPos = divisionPos;
    }

    public RoadPoint(Point2 point, double deflection) {
        this(point, deflection, -1);
    }

    public int getDegree() {
        double degree = Math.toDegrees(deflection);
        if (degree < 0) return (int)((degree % 360 + 360) * 1000);
        else return (int)(degree % 360) * 1000;
    }

    /**
     * 路边道具的角度只能在 (-135，45）之间，不然道具会背对着道路
     * @return
     */
    public int getStationaryDegree() {
        double degree = Math.toDegrees(deflection);
        if (degree < 0) degree = degree % 360 + 360;
        else degree = degree % 360;

        if (degree < 45) ;
        else if (degree < 135) degree += 180;
        else if (degree < 225) degree -= 180;
        return (int)(degree * 1000);
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

    public int getDivisionPos() {
        return divisionPos;
    }

}

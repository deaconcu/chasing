package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * 主路的Branch head和tail都是null，支路的Branch blockList不包括主路上的交叉节点，head和tail为交叉节点
 * Created by deacon on 2018/4/28.
 */
public class Branch implements Comparable<Branch> {

    public List<Hexagon> hexagonList;
    public Hexagon head;
    public Hexagon tail;
    public int detourDistance;
    //public boolean repeated;

    public Branch(Hexagon head) {
        hexagonList = new ArrayList<>();
        this.head = head;
        tail = null;
    }

    public Branch(Hexagon head, Hexagon tail) {
        this(head);
        this.tail = tail;
    }

    public int distance() {
        return hexagonList.size() + 1;
    }

    public int getExtraDistance() {
        return detourDistance - distance();
    }

    public  int getExtraDistanceRate() {
        return getExtraDistance() * 100 / distance();
    }

    public void add(Hexagon hexagon) {
        hexagonList.add(hexagon);
    }

    public Point2D getCenterPoint() {
        int distance = distance();
        if (distance == 1) return new Point2D(
                Math.round(head.coordinateX() + tail.coordinateX()) / 2,
                Math.round(head.coordinateY() + tail.coordinateY()) / 2);
        else if (distance % 2 == 0) {
            Hexagon hexagon = hexagonList.get(hexagonList.size() / 2);
            return new Point2D(
                    Math.round(hexagon.coordinateX()), Math.round(hexagon.coordinateY()));
        } else {
            Hexagon hexagon1 = hexagonList.get((hexagonList.size() - 1) / 2);
            Hexagon hexagon2 = hexagonList.get(hexagonList.size() / 2);
            return new Point2D(
                    Math.round(hexagon1.coordinateX() + hexagon2.coordinateX()) / 2,
                    Math.round(hexagon1.coordinateY() + hexagon2.coordinateY()) / 2);
                    //(hexagon1.getX() + hexagon2.getX()) / 2, (hexagon1.getY() + hexagon2.getY()) / 2);
        }
    }

    @Override
    public int compareTo(Branch branch) {
        //return new Integer(Math.abs(segment.getShort())).compareTo(Math.abs(this.getShort()));
        // TODO
        return -1;
    }

    @Override
    public String toString() {
        return head.getId()+ "[" + head.getX() + "," + head.getY() + "], "
                + "\t" + tail.getId()+ "[" + tail.getX() + "," + tail.getY() + "], "
                + "\tdistance: " + distance() + ", \tdetour distance: " + detourDistance;
    }


}

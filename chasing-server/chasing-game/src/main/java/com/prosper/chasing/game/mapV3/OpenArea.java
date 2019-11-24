package com.prosper.chasing.game.mapV3;

import com.prosper.chasing.game.base.Point2;
import com.prosper.chasing.game.util.ByteBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by deacon on 2019/5/2.
 */
public class OpenArea {

    private static int next_id = 1;

    /**
     * 标识符
     */
    private int id;

    /**
     * 这个开阔区域包含的六边形列表
     */
    private List<Hexagon> area;

    /**
     * 开阔区域与道路的连接点
     */
    private List<Point2[]> connectPoints = new LinkedList<>();

    public OpenArea(Hexagon hexagon) {
        id = next_id ++;
        area = new LinkedList<>();
        area.add(hexagon);

        hexagon.setOpenAreaId(id);
    }

    public OpenArea(List<Hexagon> area) {
        id = next_id ++;
        this.area = area;

        for (Hexagon hexagon: area) {
            hexagon.setOpenAreaId(id);
        }
    }

    public OpenArea(Set<Hexagon> hSet) {
        id = next_id ++;

        area = new LinkedList<>();
        area.addAll(hSet);

        for (Hexagon hexagon: area) {
            hexagon.setOpenAreaId(id);
        }
    }

    public void addConnectPoint(Point2[] connectPoints) {
        if (connectPoints.length != 2) throw new RuntimeException("connect points must be 2");
        this.connectPoints.add(connectPoints);
    }

    /**
     * 是否为单个六边形的开阔地带
     * @return
     */
    public boolean isSingle() {
        if (area == null) return false;
        return area.size() == 1 ? true : false;
    }

    public int getId() {
        return id;
    }

    public List<Hexagon> getArea() {
        return area;
    }

    public List<Point2[]> getConnectPoints() {
        return connectPoints;
    }

    public void appendBytes(ByteBuilder byteBuilder) {
        byteBuilder.append((byte)id);
        byteBuilder.append((byte)area.size());
        for (Hexagon hexagon: area) {
            byteBuilder.append(hexagon.coordinateX());
            byteBuilder.append(hexagon.coordinateY());
            byteBuilder.append(hexagon.getOpenAreaSiblingsByte());
        }
        byteBuilder.append(connectPoints.size());
        for (Point2[] connectPoint: connectPoints) {
            byteBuilder.append(connectPoint[0].x);
            byteBuilder.append(connectPoint[0].y);
            byteBuilder.append(connectPoint[1].x);
            byteBuilder.append(connectPoint[1].y);
        }
    }
}

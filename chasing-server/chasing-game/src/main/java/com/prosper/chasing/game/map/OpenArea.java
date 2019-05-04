package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.RoadPoint;

import java.util.LinkedList;
import java.util.List;

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
    private List<RoadPoint> connectPoint;

    public OpenArea(Hexagon hexagon) {
        id = next_id ++;
        area = new LinkedList<>();
        area.add(hexagon);
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

}

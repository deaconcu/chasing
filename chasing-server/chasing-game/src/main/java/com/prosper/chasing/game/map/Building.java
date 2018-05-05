package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;

/**
 * Created by deacon on 2018/5/5.
 */
public class Building {

    public int id;

    public byte type;

    public Point2D point2D;

    public Building(int id, byte type, Point2D point2D) {
        this.id = id;
        this.type = type;
        this.point2D = point2D;
    }
}

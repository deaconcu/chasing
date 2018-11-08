package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;
import com.prosper.chasing.game.util.ByteBuilder;

import static com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2018/5/5.
 */
public class Building {

    public int id;

    public BuildingType buildingType;

    public Point2D point2D;

    public Direction direction;

    public Building(int id, BuildingType buildingType, Point2D point2D) {
        this(id, buildingType, point2D, Direction.FREE);
    }

    public Building(int id, BuildingType buildingType, Point2D point2D, Direction direction) {
        this.id = id;
        this.buildingType = buildingType;
        this.point2D = point2D;
        this.direction = direction;
    }

    public byte[] getBytes() {
        ByteBuilder bb = new ByteBuilder();
        bb.append(id);
        bb.append(buildingType.getValue());
        bb.append(point2D.x);
        bb.append(point2D.y);
        bb.append(direction.getValue());
        return bb.getBytes();
    }

}

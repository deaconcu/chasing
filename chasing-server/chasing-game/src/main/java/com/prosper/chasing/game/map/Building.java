package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2;
import com.prosper.chasing.game.util.ByteBuilder;

import static com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2018/5/5.
 */
public class Building {

    public int id;

    public BuildingType buildingType;

    public Point2 point2;

    public Direction direction;

    public Building(int id, BuildingType buildingType, Point2 point2) {
        this(id, buildingType, point2, Direction.FREE);
    }

    public Building(int id, BuildingType buildingType, Point2 point2, Direction direction) {
        this.id = id;
        this.buildingType = buildingType;
        this.point2 = point2;
        this.direction = direction;
    }

    public byte[] getBytes() {
        ByteBuilder bb = new ByteBuilder();
        bb.append(id);
        bb.append(buildingType.getValue());
        bb.append(point2.x);
        bb.append(point2.y);
        bb.append(direction.getValue());
        return bb.getBytes();
    }

}

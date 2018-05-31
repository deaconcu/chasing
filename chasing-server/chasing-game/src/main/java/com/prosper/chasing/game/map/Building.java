package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;
import static com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2018/5/5.
 */
public class Building {

    public int id;

    public BuildingType buildingType;

    public Point2D point2D;

    public Orientation orientation;

    public Building(int id, BuildingType buildingType, Point2D point2D) {
        this(id, buildingType, point2D, Orientation.FREE);
    }

    public Building(int id, BuildingType buildingType, Point2D point2D, Orientation orientation) {
        this.id = id;
        this.buildingType = buildingType;
        this.point2D = point2D;
        this.orientation = orientation;
    }
}

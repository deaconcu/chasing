package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.util.Enums;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2019/3/14.
 */
public class SpecialSection {

    public static int MIN_SIZE = 4;

    private short id;
    private Enums.SpecialSectionType type;
    private RoadPoint[] roadPoints;

    public SpecialSection(short id, Enums.SpecialSectionType type, RoadPoint[] roadPoints) {
        this.id = id;
        this.type = type;
        this.roadPoints = roadPoints;
    }

    public short getId() {
        return id;
    }

    public Enums.SpecialSectionType getType() {
        return type;
    }

    public RoadPoint[] getRoadPoints() {
        return roadPoints;
    }

    public boolean isSingle() {
        if (roadPoints.length == 1) return true;
        return false;
    }
}

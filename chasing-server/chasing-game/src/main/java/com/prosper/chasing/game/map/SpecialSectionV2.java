package com.prosper.chasing.game.map;

import com.prosper.chasing.game.util.Enums;

import java.util.List;

/**
 * Created by deacon on 2019/12/6.
 */
public class SpecialSectionV2 {

    private int id;

    private Enums.SpecialSectionType type;

    private List<Hexagon> hexagonList;

    public SpecialSectionV2(int id, Enums.SpecialSectionType type, List<Hexagon> hexagonList) {
        this.id = id;
        this.type = type;
        this.hexagonList = hexagonList;
    }

    public boolean hasSegment(Segment segment)
    {
        if (hexagonList.contains(segment.getH1()) && hexagonList.contains(segment.getH2())) {
            return true;
        }
        return false;
    }

    public List<Hexagon> getHexagonList() {
        return hexagonList;
    }

    public int getId() {
        return id;
    }

    public Enums.SpecialSectionType getType() {
        return type;
    }
}

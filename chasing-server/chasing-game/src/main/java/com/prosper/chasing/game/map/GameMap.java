package com.prosper.chasing.game.map;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by deacon on 2018/4/28.
 */
public class GameMap {

    public Branch mainRoad;

    public List<Branch> shortcutList;

    public GameMap() {
        this.shortcutList = new LinkedList<>();
    }

    public void createMultipleTerrain() {
    }
}

package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums;

/**
 * Created by deacon on 2019/3/15.
 */
public class View extends GameObject {

    public static int nextId = 1;

    private Enums.ViewType type;

    public View(Enums.ViewType type, Point3 point3, int rotateY) {
        super(nextId ++, point3, rotateY);
        this.type = type;
    }

    public Enums.ViewType getType() {
        return type;
    }
}

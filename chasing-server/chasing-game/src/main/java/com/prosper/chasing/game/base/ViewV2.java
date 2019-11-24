package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums;

/**
 * Created by deacon on 2019/5/19.
 */
public class ViewV2 {

    private Enums.ViewTypeV2 type;

    private Point2 position;

    private int rotateY;

    public ViewV2(Enums.ViewTypeV2 type, Point2 position, int rotateY) {
        this.position = position;
        this.rotateY =  rotateY;
        this.type = type;
    }

    public Enums.ViewTypeV2 getType() {
        return type;
    }

    public Point2 getPosition() {
        return position;
    }

    public int getRotateY() {
        return rotateY;
    }
}

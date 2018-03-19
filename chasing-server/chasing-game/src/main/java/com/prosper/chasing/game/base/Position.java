package com.prosper.chasing.game.base;

import com.prosper.chasing.game.navmesh.Point;

public class Position {

    public byte moveState;
    public Point point;
    public int rotateY;

    public Position() {
        moveState = 0;
        point = new Point(0, 0, 0);
        rotateY = 0;
    }

    public Position(byte moveState, Point point, int rotateY) {
        this.moveState = moveState;
        this.point = point;
        this.rotateY = rotateY;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) {
            return false;
        }
        Position position = (Position) obj;
        if (moveState == position.moveState && point.equals(position.point) && rotateY == position.rotateY) {
            return true;
        }
        return false;
    }
    
}

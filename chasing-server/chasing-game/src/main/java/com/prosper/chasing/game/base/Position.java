package com.prosper.chasing.game.base;

public class Position {

    public byte moveState;
    public Point3 point3;
    public int rotateY;

    public Position() {
        moveState = 0;
        point3 = new Point3(0, 0, 0);
        rotateY = 0;
    }

    public Position(byte moveState, Point3 point3, int rotateY) {
        this.moveState = moveState;
        this.point3 = point3;
        this.rotateY = rotateY;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) {
            return false;
        }
        Position position = (Position) obj;
        if (moveState == position.moveState && point3.equals(position.point3) && rotateY == position.rotateY) {
            return true;
        }
        return false;
    }
    
}

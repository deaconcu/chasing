package com.prosper.chasing.game.base;

public class Position {

    public byte moveState;
    public Game.PositionPoint positionPoint;
    public int rotateY;

    public Position(byte moveState, Game.PositionPoint positionPoint, int rotateY) {
        this.moveState = moveState;
        this.positionPoint = positionPoint;
        this.rotateY = rotateY;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) {
            return false;
        }
        Position position = (Position) obj;
        if (moveState == position.moveState && positionPoint.equals(((Position) obj).positionPoint)
                && position.rotateY == position.rotateY) {
            return true;
        }
        return false;
    }
    
}

package com.prosper.chasing.game.base;

public class Position {

    public byte moveState;
    public int positionX;
    public int positionY;
    public int positionZ;
    public int rotateY;

    public Position(byte moveState, int positionX, int positionY, int positionZ, int rotateY) {
        this.moveState = moveState;
        this.positionX = positionX;
        this.positionY = positionY;
        this.positionZ = positionZ;
        this.rotateY = rotateY;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) {
            return false;
        }
        Position position = (Position) obj;
        if (moveState == position.moveState && positionX == position.positionX && positionY == position.positionY
                && positionZ == position.positionZ && position.rotateY == position.rotateY) {
            return true;
        }
        return false;
    }
    
}

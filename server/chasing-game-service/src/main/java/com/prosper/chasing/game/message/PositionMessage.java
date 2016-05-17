package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Position;

public class PositionMessage extends Message {

    private float latitude;
    private float longtitude;
    
    public PositionMessage(Message message) {
        
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongtitude() {
        return longtitude;
    }

}

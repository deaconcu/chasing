package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Position;

public class PositionMessage extends UserMessage {

    private float latitude;
    private float longtitude;
    
    public PositionMessage(UserMessage message) {
        super(message);
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongtitude() {
        return longtitude;
    }

}

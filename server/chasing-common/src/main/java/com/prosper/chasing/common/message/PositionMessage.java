package com.prosper.chasing.common.message;

import com.prosper.chasing.common.interfaces.Message;

public class PositionMessage extends Message {
    
    private long userId;
    private String gameId;
    private int distance;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

}

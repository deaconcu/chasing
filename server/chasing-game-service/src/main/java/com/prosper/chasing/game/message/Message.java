package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

public class Message {

    private int gameId;
    private int userId;
    private ByteBuffer content;

    public Message() {
    }
    
    public Message(Message message) {
        this.gameId = message.getGameId();
        this.userId = message.getUserId();
        this.content = message.getContent();
    }
    
    public Message(int gameId, int userId, ByteBuffer content) {
        this.gameId = gameId;
        this.userId = userId;
        this.content = content;
    }
    
    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public ByteBuffer getContent() {
        return content;
    }

    public void setContent(ByteBuffer content) {
        this.content = content;
    }
    
}

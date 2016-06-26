package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

/**
 * 用户发送的消息
 */
public class UserMessage extends Message {

    private int gameId;
    private int userId;
    private ByteBuffer content;

    public UserMessage() {
    }
    
    public UserMessage(UserMessage message) {
        this.gameId = message.getGameId();
        this.userId = message.getUserId();
        this.content = message.getContent();
    }
    
    public UserMessage(int gameId, int userId, ByteBuffer content) {
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

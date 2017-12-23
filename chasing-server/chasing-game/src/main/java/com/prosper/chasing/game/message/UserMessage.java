package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

/**
 * 用户发送的消息
 */
public class UserMessage extends Message {

    private int userId;
    private int seqId;
    private ByteBuffer content;

    public UserMessage() {
    }
    
    public UserMessage(UserMessage message) {
        setGameId(message.getGameId());
        this.userId = message.getUserId();
        this.content = message.getContent();
        this.seqId = message.seqId;
    }
    
    public UserMessage(int gameId, int userId, ByteBuffer content) {
        setGameId(gameId);
        this.userId = userId;
        this.content = content;
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

    public int getSeqId() {
        return seqId;
    }

    public void setSeqId(int seqId) {
        this.seqId = seqId;
    }
}

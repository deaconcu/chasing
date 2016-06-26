package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

/**
 * 服务器响应的消息
 */
public class ReplyMessage {
    
    private int userId;
    private ByteBuffer content;
    
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

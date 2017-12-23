package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

/**
 * 服务器响应的消息
 */
public class ReplyMessage {
    
    private int userId;
    private int seqId;
    private ByteBuffer content;
    private long timestamp;

    public ReplyMessage() {}

    public ReplyMessage(int userId, int seqId, ByteBuffer content) {
        this.userId = userId;
        this.seqId = seqId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
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

    public long getTimestamp() {
        return timestamp;
    }
}

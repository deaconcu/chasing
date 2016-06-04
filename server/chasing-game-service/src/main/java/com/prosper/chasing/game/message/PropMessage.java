package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

public class PropMessage extends UserMessage {
    
    public static int USE_SELF = -1;
    
    /**
     * 道具id
     */
    private int propId;
    
    /**
     * 道具使用对象的id， -1表示用在自己身上
     */
    private int toUserId;

    public PropMessage(UserMessage message) {
        ByteBuffer content = message.getContent();
        this.propId = content.getInt();
        this.toUserId = content.getInt();
    }

    public int getPropId() {
        return propId;
    }

    public int getToUserId() {
        return toUserId;
    }
}

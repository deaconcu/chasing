package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

public class PropMessage extends Message {
    
    public static int USE_SELF = -1;
    
    /**
     * 道具id
     */
    private int propId;
    
    /**
     * 道具使用对象的id， -1表示用在自己身上
     */
    private int userId;

    public PropMessage(Message message) {
        ByteBuffer content = message.getContent();
        this.propId = content.getInt();
        this.userId = content.getInt();
    }

    public int getPropId() {
        return propId;
    }

    public int getUserId() {
        return userId;
    }
}

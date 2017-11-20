package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Position;

/**
 * 格式：
 * messageType(4)|time(8)|moveState(4)|positionX(4)|positionY(4)|positionZ(4)
 */
public class PositionMessage extends UserMessage {

    public long time;
    public int moveState;
    public int positionX;
    public int positionY;
    public int positionZ;
    
    public PositionMessage(UserMessage message) {
        super(message);
        time = message.getContent().getLong(1);
        moveState = message.getContent().getInt(9);
        positionX = message.getContent().getInt(13);
        positionY = message.getContent().getInt(17);
        positionZ = message.getContent().getInt(21);
    }

}

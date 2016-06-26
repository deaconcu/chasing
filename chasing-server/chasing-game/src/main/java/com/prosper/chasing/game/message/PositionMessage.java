package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Position;

/**
 * 格式：
 * messageType(4)|moveState(4)|positionX(4)|positionY(4)|positionZ(4)
 */
public class PositionMessage extends UserMessage {

    public int moveState;
    public int positionX;
    public int positionY;
    public int positionZ;
    
    public PositionMessage(UserMessage message) {
        super(message);
        moveState = message.getContent().getInt(4);
        positionX = message.getContent().getInt(8);
        positionY = message.getContent().getInt(12);
        positionZ = message.getContent().getInt(16);
    }

}

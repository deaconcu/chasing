package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Position;

/**
 * 格式：
 * seqId(4)|messageType(1)|time(8)|moveState(4)|positionX(4)|positionY(4)|positionZ(4)|rotationY(4)|steps(2)
 */
public class PositionMessage extends UserMessage {

    public long time;
    public byte moveState;
    public int positionX;
    public int positionY;
    public int positionZ;
    public int rotationY;
    public short steps;
    
    public PositionMessage(UserMessage message) {
        super(message);
        time = message.getContent().getLong(5);
        moveState = message.getContent().get(13);
        positionX = message.getContent().getInt(14);
        positionY = message.getContent().getInt(18);
        positionZ = message.getContent().getInt(22);
        rotationY = message.getContent().getInt(26);
        steps = message.getContent().getShort(30);
    }

}

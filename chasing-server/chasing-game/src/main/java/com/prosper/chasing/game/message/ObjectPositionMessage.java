package com.prosper.chasing.game.message;

/**
 * 格式：
 * seqId(4)|messageType(1)|time(8)|moveState(4)|positionX(4)|positionY(4)|positionZ(4)|rotationY(4)|steps(2)
 */
public class ObjectPositionMessage extends UserMessage {

    public long time;
    public byte moveState;
    public int objectId;
    public int positionX;
    public int positionY;
    public int positionZ;
    public int rotationY;

    public ObjectPositionMessage(UserMessage message) {
        super(message);
        //time = message.getContent().getLong(5);
        //moveState = message.getContent().get(13);
        objectId = message.getContent().getInt(5);
        positionX = message.getContent().getInt(9);
        positionY = message.getContent().getInt(13);
        positionZ = message.getContent().getInt(17);
        rotationY = message.getContent().getInt(21);
    }

}

package com.prosper.chasing.game.message;

/**
 * 格式：
 * messageType(4)|blockGroupId(2)
 */
public class InteractionMessage extends UserMessage {

    public short blockGroupId;

    public InteractionMessage(UserMessage message) {
        super(message);
        blockGroupId = message.getContent().getShort();
    }

}

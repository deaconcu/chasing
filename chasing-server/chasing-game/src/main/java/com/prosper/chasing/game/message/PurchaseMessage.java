package com.prosper.chasing.game.message;

/**
 * 格式：
 * messageType(4)|itemId(2)
 */
public class PurchaseMessage extends UserMessage {

    public byte itemId;

    public PurchaseMessage(UserMessage message) {
        super(message);
        itemId = message.getContent().get();
    }

}

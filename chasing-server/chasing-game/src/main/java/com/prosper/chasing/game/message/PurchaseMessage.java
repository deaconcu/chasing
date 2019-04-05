package com.prosper.chasing.game.message;

import com.prosper.chasing.game.util.Enums.*;

/**
 * 格式：
 * messageType(4)|itemId(2)
 */
public class PurchaseMessage extends UserMessage {

    public PropType propType;

    public PurchaseMessage(UserMessage message) {
        super(message);
        propType = PropType.getPropType(message.getContent().getShort());
    }

}

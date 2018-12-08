package com.prosper.chasing.game.message;

import com.prosper.chasing.game.util.Enums;

/**
 * 格式：
 * messageType(4)|interaciveObjectId(2)
 */
public class InteractionMessage extends UserMessage {

    public Enums.GameObjectType interactiveObjectType;

    public int interactiveObjectId;

    public InteractionMessage(UserMessage message) {
        super(message);
        byte type = message.getContent().get();
        if (type == Enums.GameObjectType.NPC.getValue()) {
            interactiveObjectType = Enums.GameObjectType.NPC;
        } else if (type == Enums.GameObjectType.INTERACT.getValue()) {
                interactiveObjectType = Enums.GameObjectType.INTERACT;
        } else if (type == Enums.GameObjectType.DYNAMIC.getValue()) {
            interactiveObjectType = Enums.GameObjectType.DYNAMIC;
        } else if (type == Enums.GameObjectType.PROP.getValue()) {
            interactiveObjectType = Enums.GameObjectType.PROP;
        }
        interactiveObjectId = message.getContent().getInt();
    }

}

package com.prosper.chasing.game.message;

import com.prosper.chasing.game.util.Enums;

/**
 * 交互消息，暂时定义为两种消息：
 * 1：玩家拾取游戏场景中的道具
 * 2：玩家和游戏场景中的可交互对象进行交互，比如打开木门，架设桥梁等
 *
 * 格式：
 * messageType(1)|objectType(1)|objectId(2)|objectState(1)
 */
public class InteractionMessage extends UserMessage {

    /**
     * 交互的对象类型
     */
    public Enums.GameObjectType objectType;

    /**
     * 交互的对象id
     */
    public int objectId;

    /**
     * 交互的对象状态，只对interactive object有效
     */
    public byte objectState;

    public InteractionMessage(UserMessage message) {
        super(message);

        byte objectType = message.getContent().get();
        if (objectType == Enums.GameObjectType.INTERACTIVE.getValue()) {
            this.objectType = Enums.GameObjectType.INTERACTIVE;
        } else if (objectType == Enums.GameObjectType.PROP.getValue()) {
            this.objectType = Enums.GameObjectType.PROP;
        }

        objectId = message.getContent().getInt();
        if (this.objectType == Enums.GameObjectType.INTERACTIVE) {
            objectState = message.getContent().get();
        }
    }
}

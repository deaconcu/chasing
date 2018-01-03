package com.prosper.chasing.game.message;

/**
 * 格式：
 * messageType(4)|taskId(2)
 */
public class TaskMessage extends UserMessage {

    public short taskId;

    public TaskMessage(UserMessage message) {
        super(message);
        taskId = message.getContent().getShort();
    }

}

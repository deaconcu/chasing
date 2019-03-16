package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

public class FocusMessage extends UserMessage {

    // 目标用户的id
    private int id;

    public FocusMessage(UserMessage message) {
        super(message);
        ByteBuffer content = message.getContent();
        this.id = content.getInt();
    }

    public int getId() {
        return id;
    }
}

package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Point;

import java.nio.ByteBuffer;

import static com.prosper.chasing.game.util.Constant.TargetType.TYPE_POSITION;

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

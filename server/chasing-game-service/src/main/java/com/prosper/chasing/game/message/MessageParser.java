package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Component;

@Component
public class MessageParser {

    private static int POSITION_MESSAGE = 1;
    private static int PROP_MESSAGE = 2;
    private static int SKILL_MESSAGE = 3;

    /**
     * 解析消息
     */
    public Message parse(Message message) {
        ByteBuffer content = message.getContent();
        int type = content.getInt();
        if (type == POSITION_MESSAGE) {
            return new PositionMessage(message);
        } else if (type == PROP_MESSAGE) {
            return new PropMessage(message);
        } else if (type == SKILL_MESSAGE) {
            return new SkillMessage(message);
        } else {
            throw new RuntimeException("type is not supported, type" + type);
        }
    }

}

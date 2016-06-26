package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Component;

@Component
public class MessageParser {

    private static int QUIT_MESSAGE = 1;
    private static int POSITION_MESSAGE = 2;
    private static int PROP_MESSAGE = 3;
    private static int SKILL_MESSAGE = 4;

    /**
     * 解析消息
     */
    public UserMessage parseUserMessage(UserMessage userMessage) {
        ByteBuffer content = userMessage.getContent();
        int type = content.getInt();
        if (type == QUIT_MESSAGE) {
            return new QuitMessage(userMessage);
        } else if (type == POSITION_MESSAGE) {
            return new PositionMessage(userMessage);
        } else if (type == PROP_MESSAGE) {
            return new PropMessage(userMessage);
        } else if (type == SKILL_MESSAGE) {
            return new SkillMessage(userMessage);
        } else {
            throw new RuntimeException("type is not supported, type" + type);
        }
    }

}

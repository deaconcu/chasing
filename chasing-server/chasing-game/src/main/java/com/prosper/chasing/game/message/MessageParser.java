package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Component;

@Component
public class MessageParser {

    private static int CONNECT_MESSAGE = 1;
    private static int QUIT_MESSAGE = 2;
    private static int POSITION_MESSAGE = 3;
    private static int PROP_MESSAGE = 4;
    private static int TARGET_MESSAGE = 5;
    private static int ECHO_MESSAGE = 6;
    private static int PURCHASE_MESSAGE = 7;
    private static int INTERACT_MESSAGE = 8;
    private static int OBJECT_MESSAGE = 9;

    /**
     * 解析消息
     */
    public UserMessage parseUserMessage(UserMessage userMessage) {
        ByteBuffer content = userMessage.getContent();
        int seqId = content.getInt();
        userMessage.setSeqId(seqId);

        int type = content.get();
        if (type == CONNECT_MESSAGE) {
            return new ConnectMessage(userMessage);
        } else if (type == QUIT_MESSAGE) {
            return new QuitMessage(userMessage);
        } else if (type == POSITION_MESSAGE) {
            return new PositionMessage(userMessage);
        } else if (type == PROP_MESSAGE) {
            return new PropMessage(userMessage);
        } else if (type == TARGET_MESSAGE) {
            return new TargetMessage(userMessage);
        } else if (type == ECHO_MESSAGE) {
            return new EchoMessage(userMessage);
        } else if (type == PURCHASE_MESSAGE) {
            return new PurchaseMessage(userMessage);
        } else if (type == INTERACT_MESSAGE) {
            return new InteractionMessage(userMessage);
        } else if (type == OBJECT_MESSAGE) {
            return new ObjectPositionMessage(userMessage);
        } else {
            throw new RuntimeException("type is not supported, type" + type);
        }

    }

}

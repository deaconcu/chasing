package com.prosper.chasing.game.message;

import java.nio.ByteBuffer;

public class TransferPropMessage extends UserMessage {

    public static final byte GIVE = 1;
    public static final byte TAKE = 2;

    // 道具id
    private byte propId;

    // 交易道具的用户id
    private int targetUserId;

    // 交易类型
    private byte type;

    // 交易数量
    private short count;

    public TransferPropMessage(UserMessage message) {
        super(message);
        ByteBuffer content = message.getContent();
        this.type = content.get();
        this.targetUserId = content.getInt();
        this.propId = content.get();
        this.count = content.getShort();
    }

    public byte getPropId() {
        return propId;
    }

    public int getTargetUserId() {
        return targetUserId;
    }

    public byte getType() {
        return type;
    }

    public short getCount() {
        return count;
    }
}

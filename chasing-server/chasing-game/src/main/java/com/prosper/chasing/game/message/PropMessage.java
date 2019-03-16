package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Point3;

import java.nio.ByteBuffer;

public class PropMessage extends UserMessage {
    
    public static byte TYPE_SELF = 0; // 对自己使用
    public static byte TYPE_FRIEND = 0; // 对自己阵容别的玩家使用
    public static byte TYPE_ENEMY = 1; // 对其他玩家使用
    public static byte TYPE_PROP = 2; // 对道具使用
    public static byte TYPE_POSITION = 3; // 对某一个位置使用
    public static byte TYPE_NONE = 4; // 不能使用,比如重生

    // 道具id
    private short propId;

    // 使用类型 1：用户，2：道具，3：位置
    private byte type;

    // 道具使用对象的用户id， -1表示用在自己身上
    private int toUserId;

    // 道具使用对象的道具id
    private int toPropId;

    // 道具使用位置
    private Point3 point3;

    public PropMessage(UserMessage message) {
        super(message);
        ByteBuffer content = message.getContent();
        this.propId = content.getShort();
        this.type = content.get();
        if (type == TYPE_ENEMY || type == TYPE_FRIEND) {
            this.toUserId = content.getInt();
        } else if (type == TYPE_PROP) {
            this.toPropId = content.getInt();
        } else if (type == TYPE_POSITION) {
            this.point3 = new Point3(content.getInt(), content.getInt(), content.getInt());
        }
    }

    public short getPropTypeId() {
        return propId;
    }

    public byte getType() {
        return type;
    }

    public int getToUserId() {
        return toUserId;
    }

    public int getToPropId() {
        return toPropId;
    }

    public Point3 getPoint3() {
        return point3;
    }

}

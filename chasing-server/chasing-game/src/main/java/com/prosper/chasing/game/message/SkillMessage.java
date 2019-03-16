package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Point3;

import java.nio.ByteBuffer;

public class SkillMessage extends UserMessage {

    public static byte TYPE_NONE = 0;
    public static byte TYPE_USER = 1;
    public static byte TYPE_NPC = 2;
    public static byte TYPE_POSITION = 3;

    // 使用类型 1：用户，2：NPCOld，3：位置
    private byte type;

    // 道具使用对象的用户id， -1表示用在自己身上
    private int toUserId;

    // 道具使用对象的道具seqId
    private int toNPCSeqId;

    // 道具使用位置
    private Point3 point3;

    private short skillId;

    public SkillMessage(UserMessage message) {
        super(message);
        ByteBuffer content = message.getContent();
        this.skillId = content.get();
        this.type = content.get();
        if (type == TYPE_USER) {
            this.toUserId = content.getInt();
        } else if (type == TYPE_NPC) {
            this.toNPCSeqId = content.getInt();
        } else if (type == TYPE_POSITION) {
            this.point3 = new Point3(content.getInt(), content.getInt(), content.getInt());
        }
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public int getToNPCSeqId() {
        return toNPCSeqId;
    }

    public void setToNPCSeqId(int toNPCSeqId) {
        this.toNPCSeqId = toNPCSeqId;
    }

    public short getSkillId() {
        return skillId;
    }

    public void setSkillId(short skillId) {
        this.skillId = skillId;
    }

    public Point3 getPoint3() {
        return point3;
    }

    public void setPoint3(Point3 point3) {
        this.point3 = point3;
    }
}

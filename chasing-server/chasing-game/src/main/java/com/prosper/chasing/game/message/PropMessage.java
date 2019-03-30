package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.util.Enums;

import java.nio.ByteBuffer;

public class PropMessage extends UserMessage {
    
    // 道具id
    private short propId;

    // 使用类型 1：用户，2：道具，3：位置
    private Enums.TargetType type;

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
        this.type = Enums.TargetType.getTargetType(content.get());
        if (type == Enums.TargetType.USER) {
            this.toUserId = content.getInt();
        } else if (type == Enums.TargetType.PROP) {
            this.toPropId = content.getInt();
        } else if (type == Enums.TargetType.POSITION) {
            this.point3 = new Point3(content.getInt(), content.getInt(), content.getInt());
        }
    }

    public short getPropTypeId() {
        return propId;
    }

    public Enums.TargetType getType() {
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

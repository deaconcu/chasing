package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.util.Enums;
import com.prosper.chasing.game.util.Enums.*;

import java.nio.ByteBuffer;

public class PropMessage extends UserMessage {
    
    // 道具类型
    private PropType propType;

    // 使用类型 1：用户，2：道具，3：位置
    private TargetType targetType;

    // 道具使用对象的用户id， -1表示用在自己身上
    private int toUserId;

    // 道具使用对象的道具id
    private int toPropId;

    // 道具使用位置
    private Point3 point3;

    public PropMessage(UserMessage message) {
        super(message);
        ByteBuffer content = message.getContent();
        this.propType = PropType.getPropType(content.getShort());
        this.targetType = Enums.TargetType.getTargetType(content.get());
        if (targetType == Enums.TargetType.PLAYER) {
            this.toUserId = content.getInt();
        } else if (targetType == Enums.TargetType.PROP) {
            this.toPropId = content.getInt();
        } else if (targetType == Enums.TargetType.POSITION) {
            this.point3 = new Point3(content.getInt(), content.getInt(), content.getInt());
        }
    }

    public PropType getPropType() {
        return propType;
    }

    public Enums.TargetType getTargetType() {
        return targetType;
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

package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.util.Enums;

import java.nio.ByteBuffer;

public class TargetMessage extends UserMessage {

    // 使用类型 Constant.TargetType
    private Enums.TargetType type;

    // 目标对象的id
    private int id;

    // 位置
    private Point3 point3;

    public TargetMessage(UserMessage message) {
        super(message);
        ByteBuffer content = message.getContent();
        this.type = Enums.TargetType.getTargetType(content.get());
        if (type != Enums.TargetType.POSITION) {
            this.id = content.getInt();
        } else {
            this.point3 = new Point3(content.getInt(), content.getInt(), content.getInt());
        }
    }

    public Enums.TargetType getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public Point3 getPoint3() {
        return point3;
    }

}

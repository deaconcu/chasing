package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Point3;
import java.nio.ByteBuffer;
import static com.prosper.chasing.game.util.Constant.TargetType.*;

public class TargetMessage extends UserMessage {

    // 使用类型 Constant.TargetType
    private byte type;

    // 目标对象的id
    private int id;

    // 道具使用位置
    private Point3 point3;

    public TargetMessage(UserMessage message) {
        super(message);
        ByteBuffer content = message.getContent();
        this.type = content.get();
        if (type != TYPE_POSITION) {
            this.id = content.getInt();
        } else {
            this.point3 = new Point3(content.getInt(), content.getInt(), content.getInt());
        }
    }

    public byte getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public Point3 getPoint3() {
        return point3;
    }

}

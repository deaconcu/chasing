package com.prosper.chasing.game.message;

import com.prosper.chasing.game.navmesh.Point;
import java.nio.ByteBuffer;
import static com.prosper.chasing.game.util.Constant.TargetType.*;

public class TargetMessage extends UserMessage {

    // 使用类型 1：用户，2：道具，3：位置
    private byte type;

    // 目标对象的id
    private int id;

    // 道具使用位置
    private Point point;

    public TargetMessage(UserMessage message) {
        super(message);
        ByteBuffer content = message.getContent();
        this.type = content.get();
        if (type != TYPE_POSITION) {
            this.id = content.getInt();
        } else {
            this.point = new Point(content.getInt(), content.getInt(), content.getInt());
        }
    }

    public byte getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public Point getPoint() {
        return point;
    }

}

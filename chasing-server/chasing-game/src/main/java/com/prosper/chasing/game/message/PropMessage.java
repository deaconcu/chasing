package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Prop;
import com.prosper.chasing.game.service.PropService;

import java.nio.ByteBuffer;

public class PropMessage extends UserMessage {
    
    public static int USE_SELF = -1;
    
    // 道具id
    private int propId;
    
    // 道具使用对象的id， -1表示用在自己身上
    private int toUserId;

    // 使用道具的参数
    private Object[] values;

    public PropMessage(UserMessage message) {
        super(message);
        ByteBuffer content = message.getContent();
        this.propId = content.getInt();
        this.toUserId = content.getInt();
        if (propId == PropService.TRANSPORT) {
            values = new Object[3];
            values[0] = content.getInt();
            values[1] = content.getInt();
            values[2] = content.getInt();
        } else if (propId == PropService.MOVE_50_METER) {
            values = new Object[1];
            values[0] = content.get();
        }
    }

    public int getPropId() {
        return propId;
    }

    public int getToUserId() {
        return toUserId;
    }

    public Object[] getValues() {
        return values;
    }
}

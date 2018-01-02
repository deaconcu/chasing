package com.prosper.chasing.game.message;

import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.Prop;
import com.prosper.chasing.game.service.PropService;

import java.nio.ByteBuffer;

public class PropMessage extends UserMessage {
    
    public static byte TYPE_NONE = 0;
    public static byte TYPE_USER = 1;
    public static byte TYPE_PROP = 2;
    public static byte TYPE_POSITION = 3;
    
    // 道具id
    private byte propId;

    // 使用类型 1：用户，2：道具，3：位置
    private byte type;

    // 道具使用对象的用户id， -1表示用在自己身上
    private int toUserId;

    // 道具使用对象的道具id
    private int toPropId;

    // 道具使用位置
    private Game.PositionPoint positionPoint;

    public PropMessage(UserMessage message) {
        super(message);
        ByteBuffer content = message.getContent();
        this.propId = content.get();
        this.type = content.get();
        if (type == TYPE_USER) {
            this.toUserId = content.getInt();
        } else if (type == TYPE_PROP) {
            this.toPropId = content.getInt();
        } else if (type == TYPE_POSITION) {
            this.positionPoint = new Game.PositionPoint(content.getInt(), content.getInt(), content.getInt());
        }
    }

    public byte getPropId() {
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

    public Game.PositionPoint getPositionPoint() {
        return positionPoint;
    }

}

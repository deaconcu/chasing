package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2018/11/29.
 */
public class InteractiveObject extends GameObject {

    // 位置相关
    public static final byte RIVER = 1; // 河流
    public static final byte RIVER_WITH_BRIDGE = 2;   // 有桥的河流
    public static final byte GATE = 3;   // 不能通过的门
    public static final byte GATE_OPEN = 4;   // 可以通过的门
    public static final byte FIRE = 5;   // 火焰
    public static final byte FIRE_PUT_OUT = 6;   // 已经熄灭的火焰

    public InteractiveObject(int id, byte typeId, Point point, int rotateY) {
        super(id, point, rotateY);
        this.typeId = typeId;
    }

    private byte typeId;

    public byte getTypeId() {
        return typeId;
    }

    public void setTypeId(byte typeId) {
        this.typeId = typeId;
    }

    protected boolean consumeMoney(User user, int count) {
        if (user.getMoney() < count) return false;
        user.modifyMoney( - count);
        return true;
    }

    protected boolean consumeProp(User user, short propId, int count) {
        if (user.getProp(propId) < count) return false;
        user.reduceProp(propId, (short)count);
        return true;
    }

    public void interact(Game game, User user) {
        if (typeId == RIVER) {
            if (consumeProp(user, PropConfig.BRIDGE, 0)) {
                setTypeId(RIVER_WITH_BRIDGE);
                game.interactiveObjectChangedSet.add(this);
            }
        } else if (typeId == GATE) {
            if (consumeMoney(user, 0)) {
                setTypeId(GATE_OPEN);
                game.interactiveObjectChangedSet.add(this);
            }
        } else if (typeId == FIRE) {
            if (consumeMoney(user, 0)) {
                setTypeId(FIRE_PUT_OUT);
                game.interactiveObjectChangedSet.add(this);
            }
        }
    }
}

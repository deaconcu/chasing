package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums;

/**
 * Created by deacon on 2018/11/29.
 */
public class Stationary extends GameObject {

    public static int nextId = 1;

    // 位置相关
    public static final byte RIVER = 1; // 河流
    public static final byte RIVER_WITH_BRIDGE = 2;   // 有桥的河流
    public static final byte GATE = 3;   // 不能通过的门
    public static final byte GATE_OPEN = 4;   // 可以通过的门
    public static final byte FIRE = 5;   // 火焰
    public static final byte FIRE_PUT_OUT = 6;   // 已经熄灭的火焰

    /**
     * 类型
     */
    private Enums.StationaryType type;

    /**
     * 生存时间, 单位为秒, 值为负数表示生存时间为无穷大
     */
    private short lifeTime;

    public Stationary(Enums.StationaryType type, Point3 point3, int rotateY) {
        this(type, point3, rotateY, (short)-1);
    }

    public Stationary(Enums.StationaryType type, Point3 point3, int rotateY, short lifeTime) {
        super(nextId ++, point3, rotateY);
        this.type = type;
        this.lifeTime = lifeTime;
    }

    public Enums.StationaryType getType() {
        return type;
    }

    public void setType(Enums.StationaryType type) {
        this.type = type;
    }


    public Enums.StationaryType getTypeId() {
        return type;
    }

    public long getEndTime() {
        if (lifeTime < 0) return System.currentTimeMillis() + 10000000;
        return getCreateTime() + lifeTime * 1000;
    }

    public int getRemainSec() {
        long millis = getEndTime() - System.currentTimeMillis();
        return millis < 0 ? 0 : (int)Math.ceil((float)millis / 1000);
    }

    public int getLifeTime() {
        return lifeTime;
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

    public void appendPrefixBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(Enums.GameObjectType.STATIONARY.getValue());
        byteBuilder.append(getId());
    }

    public void appendBornBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(getType().getValue());
        byteBuilder.append(getPoint3().x);
        byteBuilder.append(getPoint3().y);
        byteBuilder.append(getPoint3().z);
        byteBuilder.append(getRotateY());
        byteBuilder.append(getRemainSec());
    }

    public void appendAliveBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(getRemainSec());
    }

    /*
    public void interact(Game game, User user) {
        if (type == Enums.StationaryType.RIVER) {
            if (consumeProp(user, PropConfig.BRIDGE, 0)) {
                setType(Enums.StationaryType.RIVER_WITH_BRIDGE);
                game.stationaryChangedSet.add(this);
            }
        } else if (type == Enums.StationaryType.GATE) {
            if (consumeMoney(user, 100)) {
                setType(Enums.StationaryType.GATE_OPEN);
                game.stationaryChangedSet.add(this);
            }
        } else if (type == Enums.StationaryType.FIRE_FENCE) {
            if (consumeMoney(user, 100)) {
                setType(Enums.StationaryType.FIRE_FENCE_PUT_OUT);
                game.stationaryChangedSet.add(this);
            }
        } else if (type == Enums.StationaryType.STONES) {
            if (consumeMoney(user, 100)) {
                setType(Enums.StationaryType.STONES_BROKEN);
                game.stationaryChangedSet.add(this);
            }
        }
    }
    */
}

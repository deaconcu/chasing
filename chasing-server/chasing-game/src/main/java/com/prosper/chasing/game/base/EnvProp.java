package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums;

public class EnvProp extends GameObject {

    // 环境道具的id顺序号
    public static int nextId = 0;

    // 类型id
    public Enums.PropType type;

    // 道具持续时间, 秒
    private int last;

    // 创建时间, 毫秒
    public long createTime;

    /**
     * 初始化EnvProp
     * @param type 道具类型
     * @param point3 位置
     * @param last 存活时间，定义-1为永久存活
     */
    public EnvProp(Enums.PropType type, int last, Point3 point3) {
        super(nextId ++, point3, 0);
        this.type = type;
        this.last = last;
        this.createTime = System.currentTimeMillis();
    }

    /**
     * 获取剩余生存时间, 单位为秒
     */
    public int getRemainSecond() {
        if (last < 0) return Integer.MAX_VALUE;
        int remainSecond = (int)((createTime + last * 1000 - System.currentTimeMillis()) / 1000);
        return remainSecond > 0 ? remainSecond : 0;
    }

    public void appendPrefixBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(Enums.GameObjectType.PROP.getValue());
        byteBuilder.append(getId());
    }

    @Override
    public Enums.GameObjectType getObjectType() {
        return Enums.GameObjectType.PROP;
    }

    public void appendBornBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(type.getValue());
        byteBuilder.append(getPoint3().x);
        byteBuilder.append(getPoint3().y);
        byteBuilder.append(getPoint3().z);
        byteBuilder.append(getRemainSecond());
    }

    public void appendAliveBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(getRemainSecond());
    }

    public String toString() {
        return String.format("created prop: {}:{}-{}:{}:{}",
                getId(), type, getPoint3().x, getPoint3().y, getPoint3().z);
    }
}

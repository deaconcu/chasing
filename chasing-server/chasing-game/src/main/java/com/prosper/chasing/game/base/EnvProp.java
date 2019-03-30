package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums;

public class EnvProp extends GameObject {

    // 类型id
    public short typeId;

    // 创建时间
    public long createTime;

    // 泯灭时间
    public long vanishTime;

    public EnvProp(Game game) {
        super();
    }

    /**
     * 获取剩余生存时间, 单位为秒
     */
    public int getRemainSecond() {
        int remainSecond = (int)((vanishTime - System.currentTimeMillis()) / 1000);
        return remainSecond > 0 ? remainSecond : 0;
    }

    /**
     * 捕获之后的处理
     */
    protected void catched(User user) {
        user.setProp(typeId, (byte)(user.getProp(typeId) + 1));
        vanishTime = System.currentTimeMillis();
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
        byteBuilder.append(typeId);
        byteBuilder.append(getPoint3().x);
        byteBuilder.append(getPoint3().y);
        byteBuilder.append(getPoint3().z);
        byteBuilder.append(getRemainSecond());
    }

    public void appendAliveBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(getRemainSecond());
    }
}

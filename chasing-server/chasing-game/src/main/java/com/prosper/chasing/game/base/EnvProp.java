package com.prosper.chasing.game.base;

public class EnvProp extends MovableObject {

    // 类型id
    public short typeId;

    // 创建时间
    public long createTime;

    // 泯灭时间
    public long vanishTime;

    public EnvProp(Game game) {
        super(game);
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
    @Override
    protected void catched(User user) {
        user.setProp(typeId, (byte)(user.getProp(typeId) + 1));
        vanishTime = System.currentTimeMillis();
    }
}

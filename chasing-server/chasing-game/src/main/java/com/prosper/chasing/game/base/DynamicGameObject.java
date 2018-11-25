package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2018/9/26.
 */
public class DynamicGameObject extends GameObject {

    /**
     * 游戏物品的类型id
     */
    private byte typeId;

    /**
     * 生存时间, 单位为秒
     */
    private short lifeTime;

    /**
     * 消失时间, 单位毫秒
     */
    private long endTime;

    public DynamicGameObject(byte typeId, int id, Point point, int rotateY, short lifeTime) {
        super(id, point, rotateY);
        this.typeId = typeId;
        this.endTime = getCreateTime() + lifeTime * 1000;
    }

    public byte getTypeId() {
        return typeId;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getLifeTime() {
        return lifeTime;
    }
}

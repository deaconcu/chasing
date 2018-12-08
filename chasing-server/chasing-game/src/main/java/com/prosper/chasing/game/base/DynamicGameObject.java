package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums;

/**
 * 一些会消失和移动的游戏对象，比如动物
 * Created by deacon on 2018/9/26.
 */
public class DynamicGameObject extends GameObject {

    /**
     * 游戏物品的类型id
     */
    private Enums.DynamicGameObjectType type;

    /**
     * 生存时间, 单位为秒
     */
    private short lifeTime;

    /**
     * 消失时间, 单位毫秒
     */
    private long endTime;

    public DynamicGameObject(Enums.DynamicGameObjectType type, int id, Point point, int rotateY, short lifeTime) {
        super(id, point, rotateY);
        this.type = type;
        this.endTime = getCreateTime() + lifeTime * 1000;
    }

    public Enums.DynamicGameObjectType getTypeId() {
        return type;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getLifeTime() {
        return lifeTime;
    }
}

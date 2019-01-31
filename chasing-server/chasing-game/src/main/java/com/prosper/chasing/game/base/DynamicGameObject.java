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
    private Enums.StationaryType type;

    /**
     * 生存时间, 单位为秒
     */
    private short lifeTime;

    public DynamicGameObject(Enums.StationaryType type, int id, Point point, int rotateY, short lifeTime) {
        super(id, point, rotateY);
        this.type = type;
        this.lifeTime = lifeTime;
    }

    public Enums.StationaryType getTypeId() {
        return type;
    }

    public long getEndTime() {
        return getCreateTime() + lifeTime * 1000;
    }

    public int getLifeTime() {
        return lifeTime;
    }
}

package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2018/9/26.
 */
public class DynamicGameObject extends GameObject {

    /**
     * 游戏物品的类型id
     */
    private byte typeId;

    public DynamicGameObject(byte typeId, int id, Point point, int rotateY) {
        super(id, point, rotateY);
        this.typeId = typeId;
    }

    public byte getTypeId() {
        return typeId;
    }
}

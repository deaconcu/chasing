package com.prosper.chasing.game.base;

/**
 * 商人
 * Created by deacon on 2018/4/3.
 */
public class Merchant extends NPC {

    // 商人出售的道具id
    private short[] propIds;

    private String name;

    // 商人出售的道具是否有变化
    private boolean isPropIdSetChanged;

    public Merchant(int id, String name, boolean movable, short[] propIds, Point3 position) {
        super(id, position, 0);
        setId(id);
        setName(name);
        setPoint3(position);
        setPropIds(propIds);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short[] getPropIds() {
        return propIds;
    }

    public void setPropIds(short[] propIds) {
        this.propIds = propIds;
    }

    public boolean isPropIdsChanged() {
        return isPropIdSetChanged;
    }

    public void setPropIdSetChanged(boolean propIdSetChanged) {
        isPropIdSetChanged = propIdSetChanged;
    }

    @Override
    void logic(Game game) {

    }
}


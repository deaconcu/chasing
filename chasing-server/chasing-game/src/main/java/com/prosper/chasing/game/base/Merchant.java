package com.prosper.chasing.game.base;

/**
 * 商人
 * Created by deacon on 2018/4/3.
 */
public class Merchant extends NPCOld {

    // 商人出售的道具id
    private short[] propIds;

    private String name;

    // 商人出售的道具是否有变化
    private boolean isPropIdSetChanged;

    public Merchant(Game game) {
        super(game);
    }

    public Merchant(Game game, int id, short typeId, String name,
                    boolean movable, short[] propIds, Position position) {
        super(game);
        setId(id);
        setTypeId(typeId);
        setName(name);
        setPosition(position);
        setPropIds(propIds);
        this.movable = movable;
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
}


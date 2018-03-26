package com.prosper.chasing.game.base;

import com.prosper.chasing.game.navmesh.Point;

import java.util.*;

import static java.lang.Math.random;

/**
 * Created by deacon on 2017/12/30.
 */
public class NPC extends MovableObject {

    private short typeId;

    public NPC(Game game) {
        super(game);
    }

    @Override
    protected void catched(User user) {
    }

    protected void action(User user, byte actionId, Object[] actionValues) {

    }

    public void setTypeId(short typeId) {
        this.typeId = typeId;
    }

    public short getTypeId() {
        return typeId;
    }
}

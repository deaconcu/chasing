package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2017/12/30.
 */
public class NPCOld extends MovableObject {

    private short typeId;

    public NPCOld(Game game) {
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

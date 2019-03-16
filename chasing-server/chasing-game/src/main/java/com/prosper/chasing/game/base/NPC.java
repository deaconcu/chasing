package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2018/9/8.
 */
public abstract class NPC extends GameObject {

    public NPC(int id, Point3 point3, int rotateY) {
        super(id, point3, rotateY);
    }

    abstract void logic(Game game);

}

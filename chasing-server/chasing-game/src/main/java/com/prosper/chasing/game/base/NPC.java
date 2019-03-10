package com.prosper.chasing.game.base;

import java.util.Map;

/**
 * Created by deacon on 2018/9/8.
 */
public abstract class NPC extends GameObject {

    public NPC(int id, Point point, int rotateY) {
        super(id, point, rotateY);
    }

    abstract void logic(Game game);

}

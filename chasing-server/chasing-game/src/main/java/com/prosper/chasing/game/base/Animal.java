package com.prosper.chasing.game.base;

import java.util.Map;

/**
 * 动物有主动追逐玩家的特性
 * Created by deacon on 2018/9/7.
 */
public class Animal extends Movable implements NPC {

    public Animal(int speed) {
        super(speed);
    }

    @Override
    public void logic(Map<Integer, User> playerList) {


    }
}

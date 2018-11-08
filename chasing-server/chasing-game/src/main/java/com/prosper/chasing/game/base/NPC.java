package com.prosper.chasing.game.base;

import java.util.Map;

/**
 * Created by deacon on 2018/9/8.
 */
public interface NPC {

    void logic(Map<Integer, User> playerList);

}

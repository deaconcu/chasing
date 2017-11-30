package com.prosper.chasing.game.service;

import com.prosper.chasing.game.base.User;

/**
 * Created by deacon on 2017/11/28.
 */
public class BuffService {
    public static final byte SPEED_ADD_30_PERCENT = 1;
    public static final byte SPEED_ADD_50_PERCENT = 2;
    public static final byte SPEED_REDUCE_30_PERCENT = 3;
    public static final byte SPEED_REDUCE_50_PERCENT = 4;
    public static final byte HOLD_POSITION = 5;
    public static final byte FOLLOW = 6;
    public static final byte FOLLOWED = 6;
    public static final byte INVISIBLE = 7;
    public static final byte ANTI_INVISIBLE = 8;
    public static final byte DARK_VISION = 9;
    public static final byte IMMUNITY_ALLOW_MOVE = 10;
    public static final byte IMMUNITY_NOT_MOVE = 11;
    public static final byte NEAR_ENEMY_REMIND = 12;
    public static final byte REBOUND = 13;

    public void addBuffer(User user, byte bufferId, short last, Object[] values) {
        User.Buff buffer = new User.Buff(bufferId, last, values);
        user.setBuff(buffer);
    }

    public void addBuffer(User user, byte bufferId, short last) {
        addBuffer(user, bufferId, last, null);
    }
}

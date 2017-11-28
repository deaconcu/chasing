package com.prosper.chasing.game.prop;

import java.util.Map;

import com.prosper.chasing.game.base.User;

public class PropService {

    public static final int RETURN_TO_INIT_POSITION = 1;
    public static final int SPEED_ADD_30_PERCENT = 2;
    public static final int SPEED_ADD_50_PERCENT = 3;
    public static final int TRANSPORT = 4;
    public static final int SPEED_REDUCE_30_PERCENT = 5;
    public static final int SPEED_REDUCE_50_PERCENT = 6;
    public static final int HOLD_POSITION = 7;
    public static final int RANDOM_POSITION = 8;
    public static final int RANDOM_POSITION_ALL = 9;
    public static final int RANDOM_MOVE = 10;
    public static final int BACKWORD_50_METER = 11;
    public static final int FORWORD_50_METER = 12;
    public static final int FOLLOW = 13;
    public static final int INVISIBLE = 14;
    public static final int ANTI_INVISIBLE = 15;
    public static final int DARK_VISION = 16;
    public static final int IMMUNITY_ALLOW_MOVE = 17;
    public static final int IMMUNITY_NOT_MOVE = 18;
    public static final int NEAR_ENEMY_REMIND = 19;
    public static final int REBOUND = 20;
    public static final int POSITIVE_BUFF_BONUS = 21;
    public static final int NEGATIVE_BUFF_BONUS = 22;
    public static final int WIND_ZONE = 23;
    public static final int BLOOD_PILL = 24;
    public static final int QUESTION = 25;

    /**
     * 使用道具
     * @param user 使用者 
     * @param toUser 被使用者
     * @param userMap 用户列表
     */
    public void use(int propId, User user, User toUser, Map<Integer, User> userMap) {
        if (propId == RETURN_TO_INIT_POSITION) {
            user.setPosition(user.getInitPosition());
        } else if (propId == SPEED_ADD_30_PERCENT) {

        } else if (propId == SPEED_ADD_50_PERCENT) {

        } else if (propId == TRANSPORT) {

        } else if (propId == SPEED_REDUCE_30_PERCENT) {

        } else if (propId == SPEED_REDUCE_50_PERCENT) {

        } else if (propId == HOLD_POSITION) {

        } else if (propId == RANDOM_POSITION) {

        } else if (propId == RANDOM_POSITION_ALL) {

        } else if (propId == RANDOM_MOVE) {

        } else if (propId == BACKWORD_50_METER) {

        } else if (propId == FORWORD_50_METER) {

        } else if (propId == FOLLOW) {

        } else if (propId == INVISIBLE) {

        } else if (propId == ANTI_INVISIBLE) {

        } else if (propId == DARK_VISION) {

        } else if (propId == IMMUNITY_ALLOW_MOVE) {

        } else if (propId == IMMUNITY_NOT_MOVE) {

        } else if (propId == NEAR_ENEMY_REMIND) {

        } else if (propId == REBOUND) {

        } else if (propId == POSITIVE_BUFF_BONUS) {

        } else if (propId == NEGATIVE_BUFF_BONUS) {

        } else if (propId == WIND_ZONE) {

        } else if (propId == BLOOD_PILL) {

        } else if (propId == QUESTION) {

        }
    }
}

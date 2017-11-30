package com.prosper.chasing.game.service;

import java.util.Map;

import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.Position;
import com.prosper.chasing.game.base.User;

public class PropService {

    private BuffService buffService = new BuffService();

    // 基础道具
    public static final byte RETURN_TO_INIT_POSITION = 1;
    public static final byte SPEED_ADD_30_PERCENT = 2;
    public static final byte SPEED_ADD_50_PERCENT = 3;
    public static final byte TRANSPORT = 4;
    public static final byte SPEED_REDUCE_30_PERCENT = 5;
    public static final byte SPEED_REDUCE_50_PERCENT = 6;
    public static final byte HOLD_POSITION = 7;
    public static final byte RANDOM_POSITION = 8;
    public static final byte RANDOM_POSITION_ALL = 9;
    public static final byte RANDOM_MOVE = 10;
    public static final byte MOVE_50_METER = 11;
    public static final byte FOLLOW = 13;
    public static final byte INVISIBLE = 14;
    public static final byte ANTI_INVISIBLE = 15;
    public static final byte DARK_VISION = 16;
    public static final byte IMMUNITY_ALLOW_MOVE = 17;
    public static final byte IMMUNITY_NOT_MOVE = 18;
    public static final byte NEAR_ENEMY_REMIND = 19;
    public static final byte REBOUND = 20;
    public static final byte POSITIVE_BUFF_BONUS = 21;
    public static final byte NEGATIVE_BUFF_BONUS = 22;
    public static final byte WIND_ZONE = 23;
    public static final byte BLOOD_PILL = 24;
    public static final byte QUESTION = 25;

    public static final byte GEN = 26; // 宝石，宝石竞赛使用

    /**
     * 使用道具
     * @param user 使用者 
     * @param toUser 被使用者
     * @param userMap 用户列表
     */
    public void use(int propId, Object[] values, User user, User toUser, Map<Integer, ? extends User> userMap) {
        if (propId == RETURN_TO_INIT_POSITION) {
            user.setPosition(user.getInitPosition());
        } else if (propId == SPEED_ADD_30_PERCENT) {
            buffService.addBuffer(toUser, BuffService.SPEED_ADD_30_PERCENT, (short)20);
        } else if (propId == SPEED_ADD_50_PERCENT) {
            buffService.addBuffer(toUser, BuffService.SPEED_ADD_50_PERCENT, (short)20);
        } else if (propId == TRANSPORT) {
            int x = (Integer)values[0];
            int y = (Integer)values[1];
            int z = (Integer)values[2];
            user.setPosition(new Position((byte)0, new Game.PositionPoint(x, y , z), 0));
        } else if (propId == SPEED_REDUCE_30_PERCENT) {
            buffService.addBuffer(toUser, BuffService.SPEED_REDUCE_30_PERCENT, (short)20);
        } else if (propId == SPEED_REDUCE_50_PERCENT) {
            buffService.addBuffer(toUser, BuffService.SPEED_REDUCE_50_PERCENT, (short)20);
        } else if (propId == HOLD_POSITION) {
            buffService.addBuffer(toUser, BuffService.HOLD_POSITION, (short)20);
        } else if (propId == RANDOM_POSITION) {
            // TODO
        } else if (propId == RANDOM_POSITION_ALL) {
            // TODO
        } else if (propId == RANDOM_MOVE) {
            // TODO
        } else if (propId == MOVE_50_METER) {
            int direction = (Integer)values[0];
            Position position = toUser.getPosition();
            if (direction == 1) {
                position.positionPoint.x += 50;
            } else if (direction == 2) {
                position.positionPoint.x -= 50;
            } else if (direction == 3) {
                position.positionPoint.z += 50;
            } else if (direction == 4) {
                position.positionPoint.z -= 50;
            }
            toUser.setPosition(position);
        } else if (propId == FOLLOW) {
            buffService.addBuffer(toUser, BuffService.FOLLOWED, (short)20, new Object[]{user.getId()});
            buffService.addBuffer(user, BuffService.FOLLOW, (short)20, new Object[]{toUser.getId()});
        } else if (propId == INVISIBLE) {
            buffService.addBuffer(toUser, BuffService.INVISIBLE, (short)20);
        } else if (propId == ANTI_INVISIBLE) {
            buffService.addBuffer(toUser, BuffService.ANTI_INVISIBLE, (short)20);
        } else if (propId == DARK_VISION) {
            buffService.addBuffer(toUser, BuffService.DARK_VISION, (short)20);
        } else if (propId == IMMUNITY_ALLOW_MOVE) {
            buffService.addBuffer(toUser, BuffService.IMMUNITY_ALLOW_MOVE, (short)20);
        } else if (propId == IMMUNITY_NOT_MOVE) {
            buffService.addBuffer(toUser, BuffService.IMMUNITY_NOT_MOVE, (short)20);
        } else if (propId == NEAR_ENEMY_REMIND) {
            buffService.addBuffer(toUser, BuffService.NEAR_ENEMY_REMIND, (short)20);
        } else if (propId == REBOUND) {
            buffService.addBuffer(toUser, BuffService.REBOUND, (short)20);
        } else if (propId == POSITIVE_BUFF_BONUS) {
            // TODO

        } else if (propId == NEGATIVE_BUFF_BONUS) {
            // TODO

        } else if (propId == WIND_ZONE) {
            // TODO

        } else if (propId == BLOOD_PILL) {
            // TODO

        } else if (propId == QUESTION) {
            // TODO

        }
    }
}

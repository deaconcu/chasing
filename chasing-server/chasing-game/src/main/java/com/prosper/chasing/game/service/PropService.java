package com.prosper.chasing.game.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.Position;
import com.prosper.chasing.game.base.Prop;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.message.PropMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropService {

    private Logger log = LoggerFactory.getLogger(getClass());

    private BuffService buffService = new BuffService();

    // 以下为基础道具

    // 位置相关
    public static final byte MARK = 1; // 单人位置显示 随机标记一个人
    public static final byte INVISIBLE_LEVEL_1 = 2;   // 隐身30秒，不能被标记，可以移动
    public static final byte INVISIBLE_LEVEL_2 = 3;   // 隐身5分钟，不能被标记，不能移动
    public static final byte ANTI_INVISIBLE = 4;  // 对玩家所在地点使用，以该地点为中心点的周围200米距离内，使用了隐形药水的玩家立即显形

    // 运动相关
    public static final byte RETURN_TO_INIT_POSITION = 5;  // 单人模式下使用，回到出发点
    public static final byte TRANSPORT = 6;  // 立即传送到被标记目标2米范围内
    public static final byte RANDOM_POSITION = 7; // 随机传送到一个位置
    public static final byte MOVE_FORWARD = 8;  // 向被标记目标前进50米
    public static final byte FOLLOW = 9; // 跟随某一个目标移动，两人速度为正常值的一半

    // 速度相关
    public static final byte SPEED_UP_LEVEL_1 = 10; // 加速道具 20%
    public static final byte SPEED_UP_LEVEL_2 = 11; // 加速道具 40%
    public static final byte SPEED_DOWN_LEVEL_1 = 12; // 减速道具 20%
    public static final byte SPEED_DOWN_LEVEL_2 = 13; // 减速道具 20%
    public static final byte HOLD_POSITION = 14; // 停止移动

    // 生命相关
    public static final byte BLOOD_PILL = 15; // 加血一点
    public static final byte BLOOD_BAG = 16; // 加血到满
    public static final byte REBIRTH = 17; // 加血一点

    // 视野
    public static final byte DARK_VISION = 18; // 让目标视野变黑

    // buff
    public static final byte IMMUNITY_ALLOW_MOVE = 19; // 对所有道具免疫5分钟，不能移动
    public static final byte IMMUNITY_NOT_MOVE = 20;  // 对所有道具免疫30秒，可以移动
    public static final byte REBOUND = 21;            // 反弹，持续3分钟

    // 提醒
    public static final byte NEAR_ENEMY_REMIND = 22;     // 100米接近提醒，正常为50米

    // 攻击道具
    public static final byte PROP_BOMB = 23;  // 摧毁目标道具

    // 其他
    public static final byte MONEY = 24;  // 摧毁目标道具
    public static final byte GIFT_BOX = 25;  // 摧毁目标道具

    public static Map<Byte, byte[]> typeMap = new HashMap<>();

    static {
        typeMap.put(MARK, new byte[]{PropMessage.TYPE_USER});
        typeMap.put(INVISIBLE_LEVEL_1, new byte[]{PropMessage.TYPE_NONE, PropMessage.TYPE_USER});
        typeMap.put(INVISIBLE_LEVEL_2, new byte[]{PropMessage.TYPE_NONE, PropMessage.TYPE_USER});
        typeMap.put(ANTI_INVISIBLE, new byte[]{PropMessage.TYPE_USER});
        typeMap.put(RETURN_TO_INIT_POSITION, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(TRANSPORT, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(RANDOM_POSITION, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(MOVE_FORWARD, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(FOLLOW, new byte[]{PropMessage.TYPE_USER});
        typeMap.put(SPEED_UP_LEVEL_1, new byte[]{PropMessage.TYPE_USER, PropMessage.TYPE_NONE});
        typeMap.put(SPEED_UP_LEVEL_2, new byte[]{PropMessage.TYPE_USER, PropMessage.TYPE_NONE});
        typeMap.put(SPEED_DOWN_LEVEL_1, new byte[]{PropMessage.TYPE_USER});
        typeMap.put(SPEED_DOWN_LEVEL_2, new byte[]{PropMessage.TYPE_USER});
        typeMap.put(HOLD_POSITION, new byte[]{PropMessage.TYPE_USER});
        typeMap.put(BLOOD_PILL, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(BLOOD_BAG, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(REBIRTH, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(DARK_VISION, new byte[]{PropMessage.TYPE_USER});
        typeMap.put(IMMUNITY_ALLOW_MOVE, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(IMMUNITY_NOT_MOVE, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(REBOUND, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(NEAR_ENEMY_REMIND, new byte[]{PropMessage.TYPE_NONE});
        typeMap.put(PROP_BOMB, new byte[]{PropMessage.TYPE_PROP});
        typeMap.put(MONEY, new byte[]{PropMessage.TYPE_NONE});
    }

    private boolean checkType (byte propId, byte messageType) {
        byte[] type = typeMap.get(propId);
        for (byte i : type) {
            if (i == messageType) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用道具
     * @param user 使用者 
     * @param toUser 被使用者
     * @param userMap 用户列表
     */
    public void use(byte propId, PropMessage message, User user, User toUser,
                    Map<Integer, ? extends User> userMap, List<Prop> envPropList) {
        if (!checkType(propId, message.getType())) {
            log.warn("prop type is not right, prop id: {}, message type: {}", propId, message.getType());
            return;
        }

        doUse(propId, message, user, toUser, userMap, envPropList);
        user.useProp(propId);
    }

    public void doUse(byte propId, PropMessage message, User user, User toUser,
                    Map<Integer, ? extends User> userMap, List<Prop> envPropList) {
        if (propId == RETURN_TO_INIT_POSITION) {
            user.setPosition(user.getInitPosition());
        } else if (propId == MARK) {
        } else if (propId == INVISIBLE_LEVEL_1) {
            buffService.addBuffer(toUser, BuffService.INVISIBLE, (short)20);
        } else if (propId == INVISIBLE_LEVEL_2) {
            buffService.addBuffer(toUser, BuffService.INVISIBLE, (short)20);
        } else if (propId == ANTI_INVISIBLE) {
            buffService.addBuffer(toUser, BuffService.ANTI_INVISIBLE, (short)20);
        } else if (propId == RETURN_TO_INIT_POSITION) {
        } else if (propId == TRANSPORT) {
            user.setPosition(new Position((byte)0, message.getPoint(), 0));  //TODO
        } else if (propId == RANDOM_POSITION) {
        } else if (propId == MOVE_FORWARD) {
        } else if (propId == FOLLOW) {
            buffService.addBuffer(toUser, BuffService.FOLLOWED, (short)20, new Object[]{user.getId()});
            buffService.addBuffer(user, BuffService.FOLLOW, (short)20, new Object[]{toUser.getId()});
        } else if (propId == SPEED_UP_LEVEL_1) {
            buffService.addBuffer(toUser, BuffService.SPEED_ADD_30_PERCENT, (short)20);
        } else if (propId == SPEED_UP_LEVEL_2) {
            buffService.addBuffer(toUser, BuffService.SPEED_ADD_50_PERCENT, (short)20);
        } else if (propId == SPEED_DOWN_LEVEL_1) {
            buffService.addBuffer(toUser, BuffService.SPEED_REDUCE_30_PERCENT, (short)20);
        } else if (propId == SPEED_DOWN_LEVEL_2) {
            buffService.addBuffer(toUser, BuffService.SPEED_REDUCE_50_PERCENT, (short)20);
        } else if (propId == HOLD_POSITION) {
            buffService.addBuffer(toUser, BuffService.HOLD_POSITION, (short)20);} else if (propId == BLOOD_PILL) {
        } else if (propId == BLOOD_BAG) {
        } else if (propId == REBIRTH) {
        } else if (propId == DARK_VISION) {
            buffService.addBuffer(toUser, BuffService.DARK_VISION, (short)20);
        } else if (propId == IMMUNITY_ALLOW_MOVE) {
            buffService.addBuffer(toUser, BuffService.IMMUNITY_ALLOW_MOVE, (short)20);
        } else if (propId == IMMUNITY_NOT_MOVE) {
            buffService.addBuffer(toUser, BuffService.IMMUNITY_NOT_MOVE, (short)20);
        } else if (propId == REBOUND) {
            buffService.addBuffer(toUser, BuffService.REBOUND, (short)20);
        } else if (propId == NEAR_ENEMY_REMIND) {
            buffService.addBuffer(toUser, BuffService.NEAR_ENEMY_REMIND, (short)20);
        } else if (propId == PROP_BOMB) {
        } else if (propId == MONEY) {
            user.addMoney((new Random()).nextInt(1000));
        }
    }
}

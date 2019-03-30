package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;
import com.prosper.chasing.game.util.Enums.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by deacon on 2019/3/28.
 */
public class Abilitys {

    public interface Ability {

        /**
         * 测试功能是否能成功执行，需要在真正执行前测试一次，避免执行失败
         * @param game 游戏
         * @param user 使用者
         * @param toUser 使用对象
         * @return 是否能执行成功
         */
        boolean test(Game game, User user, User toUser);

        /**
         * 执行功能
         * @param game 游戏
         * @param user 使用者
         * @param toUser 使用对象
         */
        void apply(Game game, User user, User toUser);
    }

    /**
     * 修改速度加成
     */
    public static class SpeedRateAddOn implements Ability {

        private short value;

        public SpeedRateAddOn(short value) {
            this.value = value;
        }

        @Override
        public boolean test(Game game, User user, User toUser) {
            return true;
        }

        @Override
        public void apply(Game game, User user, User toUser) {
            toUser.setSpeedRate((short)(user.getSpeedRate() + value));
        }
    }

    /**
     * 随机标定一个玩家作为目标
     */
    public static class Mark implements Ability {

        @Override
        public boolean test(Game game, User user, User toUser) {
            return true;
        }

        @Override
        public void apply(Game game, User user, User toUser) {
            List<User> activeUserList = new LinkedList<>();
            for (User singleUser: game.getUserMap().values()) {
                // 用户必须是active的，并且用户必须没有隐身
                if ((singleUser.getState() == Constant.UserState.ACTIVE ||
                        singleUser.getState() == Constant.UserState.LOADED ||
                        singleUser.getState() == Constant.UserState.OFFLINE)
                        && !singleUser.hasBuff(BuffConfig.INVISIBLE_LEVEL_1)
                        && !singleUser.hasBuff(BuffConfig.INVISIBLE_LEVEL_2)) {
                    activeUserList.add(singleUser);
                }
            }
            if (activeUserList.size() > 0) {
                User targetUser = activeUserList.get(ThreadLocalRandom.current().nextInt(activeUserList.size()));
                user.setTarget(Enums.TargetType.USER, targetUser.getId(), null);
            }
        }
    }

    /**
     * 增加buff
     */
    public static class AddBuff implements Ability {

        private BuffType type;
        private short last;

        public AddBuff(BuffType type, short last) {
            this.type = type;
            this.last = last;
        }

        @Override
        public boolean test(Game game, User user, User toUser) {
            return true;
        }

        @Override
        public void apply(Game game, User user, User toUser) {
            user.addBuff(type.getValue(), last, true);
        }
    }

    /**
     * 增加buff
     */
    public static class RemoveBuff implements Ability {

        private BuffType type;

        public RemoveBuff(BuffType type) {
            this.type = type;
        }

        @Override
        public boolean test(Game game, User user, User toUser) {
            return true;
        }

        @Override
        public void apply(Game game, User user, User toUser) {
            user.removeBuff(type.getValue());
        }
    }

    /**
     * 随机位置
     */
    public static class RandomPosition implements Ability {

        @Override
        public boolean test(Game game, User user, User toUser) {
            return true;
        }

        @Override
        public void apply(Game game, User user, User toUser) {
            RoadPoint roadPoint = game.gameMap.getRandomPoint(Enums.RoadPointType.CENTER);
            user.resetPoint(new Point3(roadPoint.getPoint().x, 0, roadPoint.getPoint().y));
        }
    }

    /**
     * 随机增加金币
     */
    public static class AddMoney implements Ability {

        private int value;

        public AddMoney(int value) {
            this.value = value;
        }

        @Override
        public boolean test(Game game, User user, User toUser) {
            return true;
        }

        @Override
        public void apply(Game game, User user, User toUser) {
            user.modifyMoney((new Random()).nextInt(value));
        }
    }

    /**
     * 随机增加道具
     */
    public static class AddProp implements Ability {

        @Override
        public boolean test(Game game, User user, User toUser) {
            return true;
        }

        @Override
        public void apply(Game game, User user, User toUser) {
            GamePropConfigMap gamePropConfigMap = game.getGameConfig().getPropConfig();
            int index = ThreadLocalRandom.current().nextInt(gamePropConfigMap.getConfigMap().size() - 1);
            int i = 0;
            short propTypeId = 0;
            for (short typeId: gamePropConfigMap.getConfigMap().keySet()) {
                if (index == i ++)  {
                    propTypeId = typeId;
                }
            }

            if (propTypeId != 0) {
                user.increaseProp(propTypeId, (short) 1);
            }
        }
    }

    /**
     * 击杀
     */
    public static class Kill implements Ability {

        @Override
        public boolean test(Game game, User user, User toUser) {
            return true;
        }

        @Override
        public void apply(Game game, User user, User toUser) {
            toUser.reduceOnelife();
        }
    }
}

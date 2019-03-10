package com.prosper.chasing.game.base;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.navmesh.NavMeshGroup;
import com.prosper.chasing.game.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

@Component
public class PropConfig {

    private static Logger log = LoggerFactory.getLogger(PropConfig.class);

    private static String SCAN_PACKAGE = "com.prosper.chasing.game";

    /********************************
     * 以下为基础道具
     ********************************/

    // 位置相关
    public static final short MARK = 1; // 单人位置显示 标记一个离你位置最近的不同队伍的人，点亮离他最近的那一盏灯，方便在追踪的时候有一个目标
    public static final short INVISIBLE_LEVEL_1 = 2;   // 隐身30秒，不能被标记，不在地图和场景中显示，可以移动
    public static final short INVISIBLE_LEVEL_2 = 3;   // 隐身5分钟，不能被标记，不在地图和场景中显示，不能移动
    public static final short ANTI_INVISIBLE = 4;  // 对玩家所在地点使用，以该地点为中心点的周围200米距离内，使用了隐形药水的玩家立即显形

    // 运动相关
    public static final short RETURN_TO_INIT_POSITION = 5;  // 单人模式下使用，回到出发点
    public static final short RANDOM_POSITION = 6; // 随机传送到一个位置
    public static final short FLASH_LEVEL_1 = 7;  // 立即传送到被标记目标2米范围内
    public static final short FLASH_LEVEL_2 = 8;  // 向被标记目标前进50米
    public static final short FOLLOW = 9; // 跟随某一个目标移动，两人速度为正常值的一半

    // 速度相关
    public static final short SPEED_UP_LEVEL_1 = 10; // 加速道具 20%
    public static final short SPEED_UP_LEVEL_2 = 11; // 加速道具 40%
    public static final short SPEED_DOWN_LEVEL_1 = 12; // 减速道具 20%
    public static final short SPEED_DOWN_LEVEL_2 = 13; // 减速道具 20%
    public static final short HOLD_POSITION = 14; // 停止移动

    // 生命相关
    public static final short BLOOD_PILL = 15; // 加血一点
    public static final short BLOOD_BAG = 16; // 加血到满
    public static final short REBIRTH = 17; // 死亡后可以重生

    // 视野
    public static final short DARK_VISION = 18; // 让目标视野变黑

    // buff
    public static final short IMMUNITY = 19; // 对所有道具免疫5分钟，不能移动
    public static final short REBOUND = 21;            // 反弹，持续3分钟

    // 提醒
    public static final short NEAR_ENEMY_REMIND = 22;     // 100米接近提醒，正常为50米

    // 攻击道具
    public static final short PROP_BOMB = 23;  // 摧毁目标道具

    // 其他
    public static final short MONEY = 24;  // 金钱
    public static final short GIFT_BOX = 25;  // 未知道具礼盒

    // 新增
    public static final short SCEPTER = 26; // 灵魂权杖
    public static final short BRIDGE = 27; // 桥梁
    public static final short RAIN_CLOUD = 28; // 云雨
    public static final short WOOD = 29; // 云雨

    /********************************
     * 以下为子游戏:killer使用的道具
     ********************************/

    public static final byte Scepter = 50;


    public static Map<Short, Prop> typeMap = new HashMap<>();

    static {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Prop.class));

        Set<BeanDefinition> components = provider.findCandidateComponents(SCAN_PACKAGE);
        for (BeanDefinition component : components) {
            try {
                Class cls = Class.forName(component.getBeanClassName());
                Prop prop = (Prop) cls.newInstance();
                typeMap.put(prop.propTypeId, prop);
            } catch (ClassNotFoundException e) {
                log.warn("class not found: " + component.getBeanClassName());
            } catch (Exception e) {
                log.warn("class initial failed: " + component.getBeanClassName());
            }
        }
    }

    public static short getRandomPropId() {
        int index = ThreadLocalRandom.current().nextInt(typeMap.size() - 1);
        int i = 0;
        for (short typeId: typeMap.keySet()) {
            if (index == i ++)  {
                return typeId;
            }
        }
        return 0;
    }

    public static Prop getProp(short propId) {
        return typeMap.get(propId);
    }

    public static void putProp(Prop prop) {
        if (typeMap.get(prop.propTypeId) != null) {
            log.warn("prop id exist: " + prop.propTypeId);
            return;
        }
        typeMap.put(prop.propTypeId, prop);
    }

    /**
     * 获得某个商品的价格
     */
    public static int getPrice(short propId) {
        // TODO
        return -1;
    }

    public static abstract class Prop {

        private Logger log = LoggerFactory.getLogger(getClass());

        @Autowired
        private NavMeshGroup navimeshGroup;

        // 道具允许使用的对象类型
        protected byte[] allowTargetType;

        // 道具类型id
        protected short propTypeId;

        // 是否自动使用,比如killer里边的权杖,捡到就使用，不放到包里,默认为false
        protected boolean autoUse = false;

        // 是否在道具包中
        protected boolean isInPackage = true;

        protected NavMeshGroup getNavimeshGroup() {
            return navimeshGroup;
        }

        /**
         * 检查道具的使用对象是否正确
         */
        private boolean checkType (byte messageType) {
            for (byte i : allowTargetType) {
                if (i == messageType) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 使用道具
         */
        public boolean use(PropMessage message, User user, User toUser, Game game) {
            if (user == null || toUser == null)  {
                return false;
            }
            // 检查道具使用对象是否正确
            if (!checkType(message.getType())) {
                log.warn("prop type is not right, prop id: {}, message type: {}", message.getType());
                //return;
            }

            // 如果使用成功，扣除道具数量
            if (doUse(message, user, toUser, game)) {
                user.reduceProp(message.getPropTypeId(), (byte)1);
                return true;
            }
            return false;
        }

        /**
         * @param message
         * @param user
         * @param game
         * @return
         */
        public abstract boolean doUse(PropMessage message, User user, User toUser, Game game);
    }

    /**
     * 追踪:随机设定一个目标为追逐对象 ******
     */
    public static class Mark extends Prop {

        public Mark() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = MARK;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
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
                return user.setTarget(Constant.TargetType.TYPE_USER, targetUser.getId(), null);
            } else {
                return false;
            }
        }
    }

    /**
     * 一级隐形药水，
     * 类型：可以给自己, 友方阵容玩家使用
     * 隐身30秒，取消所有追踪标记，且不能被标记，移动后buff失效
     */
    public static class InvisibleLevel1 extends Prop {

        public InvisibleLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF, PropMessage.TYPE_FRIEND};
            propTypeId = INVISIBLE_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            if (toUser.hasBuff(BuffConfig.INVISIBLE_LEVEL_1) || toUser.hasBuff(BuffConfig.INVISIBLE_LEVEL_2)) {
                return false;
            }

            toUser.addBuff(BuffConfig.INVISIBLE_LEVEL_1, (short)30, true);
            for (User gameUser: game.getUserMap().values()) {
                if (toUser.equals(gameUser.getTargetObject())) {
                    gameUser.clearTarget();
                }
            }
            return true;
        }
    }

    /**
     * 二级隐形药水
     * 类型：可以给自己, 友方阵容玩家使用
     * 隐身30秒，取消所有追踪标记，且不能被标记，可以移动
     */
    public static class InvisibleLevel2 extends Prop {

        public InvisibleLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF, PropMessage.TYPE_FRIEND};
            propTypeId = INVISIBLE_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            // 如果存在隐身1级，去除该buff
            if (toUser.hasBuff(BuffConfig.INVISIBLE_LEVEL_2)) {
                return false;
            }

            if (toUser.hasBuff(BuffConfig.INVISIBLE_LEVEL_1)) {
                toUser.removeBuff(BuffConfig.INVISIBLE_LEVEL_1);
            }
            toUser.addBuff(BuffConfig.INVISIBLE_LEVEL_2, (short)30, true);
            for (User gameUser: game.getUserMap().values()) {
                if (toUser.equals(gameUser.getTargetObject())) {
                    gameUser.clearTarget();
                }
            }
            return true;
        }
    }

    /**
     * 反隐形药水
     * 类型：可以给敌方玩家使用
     * 随机让一个使用了隐形药水的玩家立即显形, 如果没有玩家使用隐形药水，该药水使用失败
     */
    public static class AntiInvisible extends Prop {

        public AntiInvisible() {
            allowTargetType = new byte[]{PropMessage.TYPE_ENEMY};
            propTypeId = ANTI_INVISIBLE;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser, Game game) {
            List<User> userList = new LinkedList<>();
            for (User gameUser: game.getUserMap().values()) {
                if (gameUser.getId() == user.getId()) {
                    continue;
                }

                if (!gameUser.hasBuff(BuffConfig.IMMUNITY) &&
                        (gameUser.hasBuff(BuffConfig.INVISIBLE_LEVEL_1) ||
                        gameUser.hasBuff(BuffConfig.INVISIBLE_LEVEL_2))) {
                    userList.add(gameUser);
                }
            }

            if (userList.size() == 0) {
                return false;
            }

            Collections.shuffle(userList);
            User chosenUser = userList.get(0);
            chosenUser.removeBuff(BuffConfig.INVISIBLE_LEVEL_1);
            chosenUser.removeBuff(BuffConfig.INVISIBLE_LEVEL_2);
            return true;
        }
    }

    /**
     * 回到出生点位置
     */
    /*
    public static class ReturnToInitPosition extends Prop {

        public ReturnToInitPosition() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = RETURN_TO_INIT_POSITION;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            // TODO
            user.setPoint(user.getInitPosition());
            return true;
        }
    }
    */

    /**
     * 随机位置：
     * 类型：可以给自己, 友方阵容玩家或者敌方阵容玩家使用
     * 传送到一个随机位置
     */
    public static class RandomPosition extends Prop {

        public RandomPosition() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF, PropMessage.TYPE_FRIEND, PropMessage.TYPE_ENEMY};
            propTypeId = RANDOM_POSITION;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            Point point = game.gameMap.getRandomRoadPosition(true);
            toUser.resetPoint(new Point(point.x * 1000, point.z * 1000, point.y * 1000));
            return true;
        }
    }

    /**
     * 闪电一级
     * 类型：可以给自己, 友方阵容玩家或者敌方阵容玩家使用
     * 速度提高为20，向当前追踪目标前进，持续10秒, 最多前进到距离目标20米,
     */
    public static class FlashLevel1 extends Prop {

        public FlashLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF, PropMessage.TYPE_FRIEND};
            propTypeId = FLASH_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            if (toUser.hasBuff(BuffConfig.FLASH_LEVEL_1) || (user.hasBuff(BuffConfig.FLASH_LEVEL_2))) {
                return false;
            }
            Object target = toUser.getTargetObject();
            if (target == null) {
                return false;
            }
            // TODO 需要前进到目标对象20米距离
            toUser.addBuff(BuffConfig.FLASH_LEVEL_1, (short)10, true);
            return true;
        }
    }

    /**
     * 闪电二级
     * 类型：可以给自己, 友方阵容玩家或者敌方阵容玩家使用
     * 速度提高为20，向当前追踪目标前进，持续30秒, 最多前进到距离目标20米,
     */
    public static class FlashLevel2 extends Prop {

        public FlashLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF, PropMessage.TYPE_FRIEND};
            propTypeId = FLASH_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            if (toUser.hasBuff(BuffConfig.FLASH_LEVEL_2)) {
                return false;
            }
            Object target = toUser.getTargetObject();
            if (target == null) {
                return false;
            }

            if (toUser.hasBuff(BuffConfig.FLASH_LEVEL_1)) {
                toUser.removeBuff(BuffConfig.FLASH_LEVEL_1);
            }
            // TODO 需要前进到目标对象20米距离
            toUser.addBuff(BuffConfig.FLASH_LEVEL_2, (short)30, true);
            return true;
        }
    }

    /**
     * 跟随  TODO
     * 类型：可以给友方阵容玩家或者敌方阵容玩家使用
     * 跟随一个玩家，当前玩家和被跟随的玩家速度降为速度和的一半
     */
    public static class Follow extends Prop {

        public Follow() {
            allowTargetType = new byte[]{PropMessage.TYPE_FRIEND, PropMessage.TYPE_ENEMY};
            propTypeId = FOLLOW;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser, Game game) {
            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            if (user.hasBuff(BuffConfig.FOLLOW) || user.hasBuff(BuffConfig.FOLLOWED)) {
                return false;
            }
            if (toUser.hasBuff(BuffConfig.FOLLOW) || toUser.hasBuff(BuffConfig.FOLLOWED)) {
                return false;
            }

            toUser.addBuff(BuffConfig.FOLLOWED, (short)60, true, user.getId());
            user.addBuff(BuffConfig.FOLLOW, (short)60, true, toUser.getId());
            return true;
        }
    }

    /**
     * 面包
     * 类型：可以给友方阵容玩家或者敌方阵容玩家使用
     * 移除所有减速道具，速度增加20%，持续时间20秒
     */
    public static class SpeedUpLevel1 extends Prop {

        public SpeedUpLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF, PropMessage.TYPE_FRIEND};
            propTypeId = SPEED_UP_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            if (toUser.hasBuff(BuffConfig.SPEED_UP_LEVEL_2)) {
                return false;
            }
            toUser.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_1);
            toUser.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_2);
            toUser.addBuff(BuffConfig.SPEED_UP_LEVEL_1, (short)20, true);
            return true;
        }
    }

    /**
     * 红酒
     * 类型：可以给友方阵容玩家或者敌方阵容玩家使用
     * 移除所有减速道具，速度增加40%，持续时间20秒
     */
    public static class SpeedUpLevel2 extends Prop {

        public SpeedUpLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF, PropMessage.TYPE_FRIEND};
            propTypeId = SPEED_UP_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            toUser.removeBuff(BuffConfig.SPEED_UP_LEVEL_1);
            toUser.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_1);
            toUser.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_2);
            toUser.addBuff(BuffConfig.SPEED_UP_LEVEL_2, (short)20, true);
            return true;
        }
    }

    /**
     * 小积雨云
     * 类型：可以给敌方阵容玩家使用
     * 移除所有加速道具，速度减少20%，持续时间20秒
     */
    public static class SpeedDownLevel1 extends Prop {

        public SpeedDownLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_ENEMY};
            propTypeId = SPEED_DOWN_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            if (toUser.hasBuff(BuffConfig.REBOUND)) toUser = user;

            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            if (toUser == null) return false;

            if (toUser.hasBuff(BuffConfig.SPEED_DOWN_LEVEL_2)) {
                return false;
            }

            toUser.removeBuff(BuffConfig.SPEED_UP_LEVEL_1);
            toUser.removeBuff(BuffConfig.SPEED_UP_LEVEL_2);
            toUser.addBuff(BuffConfig.SPEED_DOWN_LEVEL_1, (short)20, true);
            return true;
        }
    }

    /**
     * 大积雨云
     * 类型：给敌方阵容玩家使用
     * 移除所有加速道具，速度减少40%，持续时间20秒
     */
    public static class SpeedDownLevel2 extends Prop {

        public SpeedDownLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_ENEMY};
            propTypeId = SPEED_DOWN_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            if (toUser.hasBuff(BuffConfig.REBOUND)) toUser = user;
            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            toUser.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_1);
            toUser.removeBuff(BuffConfig.SPEED_UP_LEVEL_1);
            toUser.removeBuff(BuffConfig.SPEED_UP_LEVEL_2);
            toUser.addBuff(BuffConfig.SPEED_DOWN_LEVEL_2, (short)20, true);
            return true;
        }
    }

    /**
     * 禁锢
     * 类型：给敌方阵容玩家使用
     * 停止移动，持续时间20秒
     */
    public static class HoldPosition extends Prop {

        public HoldPosition() {
            allowTargetType = new byte[]{PropMessage.TYPE_ENEMY};
            propTypeId = HOLD_POSITION;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            if (toUser.hasBuff(BuffConfig.REBOUND)) toUser = user;
            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            toUser.addBuff(BuffConfig.HOLD_POSITION, (short)20, true);
            return true;
        }
    }

    /**
     * 血片 加血一点
     */
    /*
    public static class BloodPill extends Prop {

        public BloodPill() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = BLOOD_PILL;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            user.addOneLife();
            return true;
        }
    }
    */

    /**
     * 血包 加满血
     */
    /*
    public static class BloodBag extends Prop {

        public BloodBag() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = BLOOD_BAG;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            user.maxLife();
            return true;
        }
    }
    */

    /**
     * 重生卷轴
     * 类型：自己使用
     * 死亡后复活
     */
    public static class Rebirth extends Prop {

        public Rebirth() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = REBIRTH;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser, Game game) {
            Point point = game.gameMap.getRandomRoadPosition(true);
            toUser.setPoint(point);
            return true;
        }
    }

    /**
     * 黑云蔽日
     * 类型：给敌方玩家使用
     * 让某人视野变黑，持续时间20秒
     */
    public static class DarkVision extends Prop {

        public DarkVision() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = DARK_VISION;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser, Game game) {
            if (toUser.hasBuff(BuffConfig.REBOUND)) toUser = user;
            if (toUser.hasBuff(BuffConfig.IMMUNITY)) return false;
            toUser.addBuff(BuffConfig.DARK_VISION, (short)20, true);
            return true;
        }
    }

    /**
     * 免疫药水
     * 类型：给自己或者友方玩家使用
     * 去除所有buff，并对所有道具免疫，持续30秒
     */
    public static class Immunity extends Prop {

        public Immunity() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = IMMUNITY;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            toUser.clearBuff();
            toUser.addBuff(BuffConfig.IMMUNITY, (short)30, true);
            return true;
        }
    }

    /**
     * 反弹衣
     * 类型：给自己或者友方玩家使用
     * 持续1分钟，将负面单体效果反弹至使用者
     */
    public static class Rebound extends Prop {

        public Rebound() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF, PropMessage.TYPE_FRIEND};
            propTypeId = REBOUND;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            toUser.addBuff(BuffConfig.REBOUND, (short)60, true);
            return true;
        }
    }

    /**
     * 顺风耳
     * 类型：给自己或者友方玩家使用
     * 100米接近提醒，正常为50米 持续时间2分钟
     */
    public static class NearEnemyRemind extends Prop {

        public NearEnemyRemind() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF, PropMessage.TYPE_FRIEND};
            propTypeId = NEAR_ENEMY_REMIND;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            toUser.addBuff(BuffConfig.NEAR_ENEMY_REMIND, (short)120, true);
            return true;
        }
    }

    /**
     * TODO
     * 炸弹 摧毁某个道具，有效距离100米
     */
    public static class PropBomb extends Prop {

        public PropBomb() {
            allowTargetType = new byte[]{PropMessage.TYPE_PROP};
            propTypeId = PROP_BOMB;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            // TODO
            return false;
        }
    }

    /**
     * 金钱
     * 类型：给自己使用
     * 随机加金子 1000内
     */
    public static class Money extends Prop {

        public Money() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = MONEY;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            toUser.modifyMoney((new Random()).nextInt(1000));
            return true;
        }
    }

    /**
     * 宝箱
     * 类型：给自己使用
     * 给玩家随机增加一个道具
     */
    public static class GiftBox extends Prop {

        public GiftBox() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = GIFT_BOX;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            GamePropConfigMap gamePropConfigMap = game.getGamePropConfigMap();
            int index = ThreadLocalRandom.current().nextInt(gamePropConfigMap.getConfigMap().size() - 1);
            int i = 0;
            short propTypeId = 0;
            for (short typeId: gamePropConfigMap.getConfigMap().keySet()) {
                if (index == i ++)  {
                    propTypeId = typeId;
                }
            }

            if (propTypeId != 0) {
                toUser.increaseProp(propTypeId, (short) 1);
            }
            return true;
        }
    }

    /**
     * 灵魂权杖: 抽离灵魂
     */
    /*
    public static class Scepter extends PropConfig.Prop {

        public Scepter() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = SCEPTER;
            autoUse = true;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            user.addBuff(HOLD_SCEPTER);
            return true;
        }
    }
    */

    /**
     * 桥梁
     */
    /*
    public static class Bridge extends PropConfig.Prop {

        public Bridge() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = BRIDGE;
            autoUse = false;
        }

        @Override
        public boolean doUse(PropMessage message, User user, User toUser,  Game game) {
            Block block = game.gameMap.getBlock(user.getPoint().x, user.getPoint().y);
            Map<Integer, List<Block>> blockMap = game.gameMap.getBlocksInDistances(block.blockId, 2,
                    Enums.BlockType.MAIN_ROAD, Enums.BlockType.BRANCH, Enums.BlockType.SHORTCUT);

            for (List<Block> hexagonList: blockMap.values()) {
                for (Block aroundBlock: hexagonList) {
                    BlockGroup blockGroup = game.gameMap.blockGroupMap.get(aroundBlock.blockGroupId);
                    if (blockGroup != null && blockGroup.getTerrainType() == Enums.TerrainType.RIVER) {
                        blockGroup.setTerrainType(Enums.TerrainType.RIVER_WITH_BRIDGE);
                        game.terrainChangedSet.add(blockGroup.getId());
                        return true;
                    }
                }
            }
            return false;
        }
    }
    */
}

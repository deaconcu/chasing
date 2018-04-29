package com.prosper.chasing.game.base;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.navmesh.NaviMesh;
import com.prosper.chasing.game.navmesh.NaviMeshGroup;
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
    public static final short MARK = 1; // 单人位置显示 随机标记一个人
    public static final short INVISIBLE_LEVEL_1 = 2;   // 隐身30秒，不能被标记，可以移动
    public static final short INVISIBLE_LEVEL_2 = 3;   // 隐身5分钟，不能被标记，不能移动
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
    public static final short REBIRTH = 17; // 加血一点

    // 视野
    public static final short DARK_VISION = 18; // 让目标视野变黑

    // buff
    public static final short IMMUNITY_LEVEL_1 = 19; // 对所有道具免疫5分钟，不能移动
    public static final short IMMUNITY_LEVEL_2 = 20;  // 对所有道具免疫30秒，可以移动
    public static final short REBOUND = 21;            // 反弹，持续3分钟

    // 提醒
    public static final short NEAR_ENEMY_REMIND = 22;     // 100米接近提醒，正常为50米

    // 攻击道具
    public static final short PROP_BOMB = 23;  // 摧毁目标道具

    // 其他
    public static final short MONEY = 24;  // 摧毁目标道具
    public static final short GIFT_BOX = 25;  // 摧毁目标道具

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
        private NaviMeshGroup navimeshGroup;

        // 道具允许使用的对象类型
        protected byte[] allowTargetType;

        // 道具类型id
        protected short propTypeId;

        // 是否自动使用,比如killer里边的权杖,捡到就使用，不放到包里,默认为false
        protected boolean autoUse = false;

        // 是否在道具包中
        protected boolean isInPackage = true;

        protected NaviMeshGroup getNavimeshGroup() {
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
        public void use(PropMessage message, User user, Game game) {
            // 检查道具使用对象是否正确
            if (!checkType(message.getType())) {
                log.warn("prop type is not right, prop id: {}, message type: {}", message.getType());
                return;
            }

            // 如果使用成功，扣除道具数量
            if (doUse(message, user, game)) {
                user.reduceProp(message.getPropId(), (byte)1);
            }
        }

        /**
         * @param message
         * @param user
         * @param game
         * @return
         */
        public abstract boolean doUse(PropMessage message, User user, Game game);
    }

    /**
     * 追踪:随机设定一个目标为追逐对象
     */
    public static class Mark extends Prop {

        public Mark() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = MARK;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            List<User> activeUserList = new LinkedList<>();
            for (User singleUser: game.getUserMap().values()) {
                // 用户必须是active的，并且用户必须没有隐身
                if (singleUser.getState() == Constant.UserState.ACTIVE
                        && singleUser.hasBuffer(BuffConfig.INVISIBLE_LEVEL_1)
                        && singleUser.hasBuffer(BuffConfig.INVISIBLE_LEVEL_2)) {
                    activeUserList.add(singleUser);
                }
            }
            if (activeUserList.size() > 0) {
                User targetUser = activeUserList.get(ThreadLocalRandom.current().nextInt(activeUserList.size()));
                user.setTarget(Constant.TargetType.TYPE_USER, targetUser.getId(), null);
            }
            return true;
        }
    }

    /**
     * 隐形药水（大） 隐身30秒，取消所有追踪标记，且不能被标记，可以移动
     */
    public static class InvisibleLevel1 extends Prop {

        public InvisibleLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = INVISIBLE_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            // 如果存在隐身2级，提示有更高级的道具发挥作用，返回
            if (user.hasBuffer(INVISIBLE_LEVEL_2)) {
                return false;
            }
            user.addBuff(BuffConfig.INVISIBLE_LEVEL_1);
            return true;
        }
    }

    /**
     * 隐形药水（小）隐身30秒，取消所有追踪标记，且不能被标记，不能移动
     */
    public static class InvisibleLevel2 extends Prop {

        public InvisibleLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = INVISIBLE_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            // 如果存在隐身1级，去除该buff
            if (user.hasBuffer(BuffConfig.INVISIBLE_LEVEL_1)) {
                user.removeBuff(BuffConfig.INVISIBLE_LEVEL_1);
            }
            user.addBuff(BuffConfig.INVISIBLE_LEVEL_2);
            return true;
        }
    }

    /**
     * 反隐形药水:玩家周围200米距离内，使用了隐形药水的玩家立即显形
     */
    public static class AntiInvisible extends Prop {

        public AntiInvisible() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = ANTI_INVISIBLE;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            for (User gameUser: game.getUserMap().values()) {
                if (gameUser.getId() == user.getId()) {
                    continue;
                }
                if (gameUser.getPosition().point.distance(user.getPosition().point) < 200) {
                    gameUser.removeBuff(BuffConfig.INVISIBLE_LEVEL_1);
                    gameUser.removeBuff(BuffConfig.INVISIBLE_LEVEL_2);
                }
            }
            return true;
        }
    }

    /**
     * 随机位置：随机给一个位置
     */
    public static class ReturnToInitPosition extends Prop {

        public ReturnToInitPosition() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = RETURN_TO_INIT_POSITION;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.setPosition(user.getInitPosition());
            return true;
        }
    }

    /**
     * 传送:速度提高为20，向被标记目标前进到20米范围内
     */
    public static class RandomPosition extends Prop {

        public RandomPosition() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = RANDOM_POSITION;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.setPosition(new Position(Constant.MoveState.IDLE,
                    getNavimeshGroup().getRandomPositionPoint(game.getGameInfo().getMetagameCode()), 0));
            return true;
        }
    }

    /**
     * 瞬移 速度提高为20，向被标记目标前进100米，最多前进到距离目标20米
     */
    public static class FlashLevel1 extends Prop {

        public FlashLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = FLASH_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (user.hasBuffer(FLASH_LEVEL_1) || (user.hasBuffer(FLASH_LEVEL_2))) {
                return false;
            }
            GameObject target = user.getCurrentTargetObject();
            if (target == null) {
                return false;
            }
            // TODO 需要前进到目标对象20米距离
            user.addBuff(BuffConfig.FLASH_LEVEL_1);
            return true;
        }
    }

    /**
     * 跟随：跟随某一个目标移动，两人速度为正常值的一半
     */
    public static class FlashLevel2 extends Prop {

        public FlashLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = FLASH_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (user.hasBuffer(FLASH_LEVEL_1) || (user.hasBuffer(FLASH_LEVEL_2))) {
                return false;
            }
            GameObject target = user.getCurrentTargetObject();
            if (target == null) {
                return false;
            }
            // TODO 需要前进到目标对象20米距离
            user.addBuff(BuffConfig.FLASH_LEVEL_2);
            return true;
        }
    }

    /**
     * 面包 加速道具 20%，持续时间20秒
     */
    public static class Follow extends Prop {

        public Follow() {
            allowTargetType = new byte[]{PropMessage.TYPE_USER};
            propTypeId = FOLLOW;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            // TODO 暂时不做
            User toUser = game.getUserMap().get(message.getToUserId());
            if (toUser != null) {
                user.addBuff(BuffConfig.FOLLOWED, user.getId());
                toUser.addBuff(BuffConfig.FOLLOW, toUser.getId());
                return true;
            }
            return false;
        }
    }

    /**
     * 红酒 加速道具 40%，持续时间20秒
     */
    public static class SpeedUpLevel1 extends Prop {

        public SpeedUpLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = SPEED_UP_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (user.hasBuffer(BuffConfig.SPEED_DOWN_LEVEL_2)) {
                return false;
            }
            user.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_1);
            user.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_2);
            user.addBuff(BuffConfig.SPEED_UP_LEVEL_1);
            return true;
        }
    }

    /**
     * 小积雨云 减速道具 20%，持续时间20秒
     */
    public static class SpeedUpLevel2 extends Prop {

        public SpeedUpLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = SPEED_UP_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.removeBuff(BuffConfig.SPEED_UP_LEVEL_1);
            user.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_1);
            user.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_2);
            user.addBuff(BuffConfig.SPEED_UP_LEVEL_2);
            return true;
        }
    }

    /**
     * 大积雨云 减速道具 40%，持续时间20秒
     */
    public static class SpeedDownLevel1 extends Prop {

        public SpeedDownLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = SPEED_DOWN_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (user.hasBuffer(BuffConfig.SPEED_DOWN_LEVEL_2)) {
                return false;
            }
            user.removeBuff(BuffConfig.SPEED_UP_LEVEL_1);
            user.removeBuff(BuffConfig.SPEED_UP_LEVEL_2);
            user.addBuff(BuffConfig.SPEED_DOWN_LEVEL_1);
            return true;
        }
    }

    /**
     * 禁锢 停止移动，持续时间20秒
     */
    public static class SpeedDownLevel2 extends Prop {

        public SpeedDownLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = SPEED_DOWN_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_1);
            user.removeBuff(BuffConfig.SPEED_UP_LEVEL_1);
            user.removeBuff(BuffConfig.SPEED_UP_LEVEL_2);
            user.addBuff(BuffConfig.SPEED_DOWN_LEVEL_2);
            return true;
        }
    }

    /**
     * 血片 加血一点
     */
    public static class HolaPosition extends Prop {

        public HolaPosition() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = HOLD_POSITION;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.removeBuff(BuffConfig.SPEED_UP_LEVEL_1);
            user.removeBuff(BuffConfig.SPEED_UP_LEVEL_2);
            user.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_1);
            user.removeBuff(BuffConfig.SPEED_DOWN_LEVEL_2);
            user.addBuff(BuffConfig.HOLD_POSITION);
            return true;
        }
    }

    /**
     * 血包 加满血
     */
    public static class BloodPill extends Prop {

        public BloodPill() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = BLOOD_PILL;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.addOneLife();
            return true;
        }
    }

    /**
     * 重生卷轴 死亡后复活并有一点血
     */
    public static class BloodBag extends Prop {

        public BloodBag() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = BLOOD_BAG;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.maxLife();
            return true;
        }
    }

    /**
     * 黑云蔽日 让某人视野变黑，持续时间20秒
     */
    public static class Rebirth extends Prop {

        public Rebirth() {
            allowTargetType = new byte[]{PropMessage.TYPE_NONE};
            propTypeId = REBIRTH;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            return false;
        }
    }

    /**
     * 免疫药水：对所有道具免疫，不能移动
     */
    public static class DarkVision extends Prop {

        public DarkVision() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = DARK_VISION;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.addBuff(BuffConfig.DARK_VISION);
            return true;
        }
    }

    /**
     * 免疫药水：对所有道具免疫，可以移动
     */
    public static class ImmunityLevel1 extends Prop {

        public ImmunityLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = IMMUNITY_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (user.hasBuffer(IMMUNITY_LEVEL_2)) {
                return false;
            }
            user.addBuff(BuffConfig.IMMUNITY_LEVEL_1);
            return true;
        }
    }

    /**
     * 反弹衣，持续1分钟，将单体效果反弹至使用者
     */
    public static class ImmunityLevel2 extends Prop {

        public ImmunityLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = IMMUNITY_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.removeBuff(BuffConfig.IMMUNITY_LEVEL_1);
            user.addBuff(BuffConfig.IMMUNITY_LEVEL_2);
            return true;
        }
    }

    /**
     * 顺风耳：100米接近提醒，正常为50米 持续时间2分钟
     */
    public static class Rebound extends Prop {

        public Rebound() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = REBOUND;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.addBuff(BuffConfig.REBOUND);
            return true;
        }
    }

    /**
     * 炸弹 摧毁某个道具，有效距离100米
     */
    public static class NearEnemyRemind extends Prop {

        public NearEnemyRemind() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = NEAR_ENEMY_REMIND;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.addBuff(BuffConfig.NEAR_ENEMY_REMIND);
            return true;
        }
    }

    /**
     * 道具炸弹：摧毁某一个道具
     */
    public static class PropBomb extends Prop {

        public PropBomb() {
            allowTargetType = new byte[]{PropMessage.TYPE_PROP};
            propTypeId = PROP_BOMB;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            // TODO
            return false;
        }
    }

    /**
     * 金钱：加金子
     */
    public static class Money extends Prop {

        public Money() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = MONEY;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            // TODO
            user.addMoney((new Random()).nextInt(1000));
            return true;
        }
    }

    /**
     * 宝箱：随机出道具
     */
    public static class GiftBox extends Prop {

        public GiftBox() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = GIFT_BOX;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            // TODO
            return false;
        }
    }
}

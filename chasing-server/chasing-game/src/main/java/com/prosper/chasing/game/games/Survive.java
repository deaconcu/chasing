package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

@MetaGameAnno("survive")
public class Survive extends Game {

    private static Logger log = LoggerFactory.getLogger(Survive.class);

    public static byte MAX_THIRSTY = 100;
    public static byte MAX_HUNGRY = 100;

    public static byte PROP_TRANSFER_DISTANCE = 2;
    public static byte PROP_TRANSFER_CATCH_DISTANCE = 5;

    protected static GamePropConfigMap gamePropConfigMap;

    /***********************
     * 自定义User
     ***********************/
    public static class SurviveUser extends User {
        byte teamId;
        byte thirsty = MAX_THIRSTY;
        byte hungry = MAX_HUNGRY;

        private void addHungry(byte value) {
            hungry = (byte)((hungry + value) > MAX_HUNGRY ? MAX_HUNGRY : (hungry + value));
        }

        private void addThirsty(byte value) {
            thirsty = (byte)((thirsty + value) > MAX_THIRSTY ? MAX_THIRSTY : (thirsty + value));
        }

        protected void checkIfEnd() {
            if (thirsty == 0 || hungry == 0) {
                setState(Constant.UserState.GHOST);
                gameOverTime = System.currentTimeMillis();
            }

            boolean allGone = true;
            for (SurviveUser teamUser: ((Survive)getGame()).getTeamUser(teamId)) {
                if (teamUser.thirsty != 0 && teamUser.hungry != 0) {
                    allGone = false;
                }
            }
            if (allGone == true && getState() == Constant.UserState.GHOST) {
                setState(Constant.UserState.GAME_OVER);
            }
        }
    }

    /***********************
     * 新的Prop
     ***********************/
    public static final short PROP_FOOD_LEVEL_1 = 201;
    public static final short PROP_FOOD_LEVEL_2 = 202;
    public static final short PROP_FOOD_LEVEL_3 = 203;
    public static final short PROP_WATER_LEVEL_1 = 204;
    public static final short PROP_WATER_LEVEL_2 = 205;
    public static final short PROP_WATER_LEVEL_3 = 206;

    public static class FoodLevel1 extends PropConfig.Prop {

        public FoodLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = PROP_FOOD_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (!(user instanceof SurviveUser)) {
                log.warn("prop used on wrong user");
                return false;
            }

            SurviveUser surviveUser = (SurviveUser) user;
            surviveUser.addHungry((byte)20);
            return true;
        }
    }

    public static class FoodLevel2 extends PropConfig.Prop {

        public FoodLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = PROP_FOOD_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (!(user instanceof SurviveUser)) {
                log.warn("prop used on wrong user");
                return false;
            }

            SurviveUser surviveUser = (SurviveUser) user;
            surviveUser.addHungry((byte)40);
            return true;
        }
    }

    public static class FoodLevel3 extends PropConfig.Prop {

        public FoodLevel3() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = PROP_FOOD_LEVEL_3;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (!(user instanceof SurviveUser)) {
                log.warn("prop used on wrong user");
                return false;
            }

            SurviveUser surviveUser = (SurviveUser) user;
            surviveUser.addHungry((byte)60);
            return true;
        }
    }

    public static class WaterLevel1 extends PropConfig.Prop {

        public WaterLevel1() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = PROP_WATER_LEVEL_1;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (!(user instanceof SurviveUser)) {
                log.warn("prop used on wrong user");
                return false;
            }

            SurviveUser surviveUser = (SurviveUser) user;
            surviveUser.addThirsty((byte)20);
            return true;
        }
    }

    public static class WaterLevel2 extends PropConfig.Prop {

        public WaterLevel2() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = PROP_WATER_LEVEL_2;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (!(user instanceof SurviveUser)) {
                log.warn("prop used on wrong user");
                return false;
            }

            SurviveUser surviveUser = (SurviveUser) user;
            surviveUser.addThirsty((byte)40);
            return true;
        }
    }

    public static class WaterLevel3 extends PropConfig.Prop {

        public WaterLevel3() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = PROP_WATER_LEVEL_3;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            if (!(user instanceof SurviveUser)) {
                log.warn("prop used on wrong user");
                return false;
            }

            SurviveUser surviveUser = (SurviveUser) user;
            surviveUser.addThirsty((byte)60);
            return true;
        }
    }

    /**
     * 一些设定
     */
    static {
        PropConfig.putProp(new FoodLevel1());
        PropConfig.putProp(new FoodLevel2());
        PropConfig.putProp(new FoodLevel3());
        PropConfig.putProp(new WaterLevel1());
        PropConfig.putProp(new WaterLevel2());
        PropConfig.putProp(new WaterLevel3());

        // prop配置
        /*
        gamePropConfigMap = new GamePropConfigMap(50)
                .add(PropService.MARK, (short)40, (short)15, false)
                .add(PropService.INVISIBLE_LEVEL_1, (short)40, (short)15, false)
                .add(PropService.INVISIBLE_LEVEL_2, (short)40, (short)15, false)
                .add(PropService.ANTI_INVISIBLE, (short)40, (short)15, false)
                .add(PropService.RETURN_TO_INIT_POSITION, (short)40, (short)15, false)
                .add(PropService.TRANSPORT, (short)40, (short)15, false)
                .add(PropService.RANDOM_POSITION, (short)40, (short)15, false)
                .add(PropService.MOVE_FORWARD, (short)40, (short)15, false)
                .add(PropService.FOLLOW, (short)40, (short)15, false)
                .add(PropService.SPEED_UP_LEVEL_1, (short)40, (short)15, false)
                .add(PropService.SPEED_UP_LEVEL_2, (short)40, (short)15, false)
                .add(PropService.SPEED_DOWN_LEVEL_1, (short)40, (short)15, false)
                .add(PropService.SPEED_DOWN_LEVEL_2, (short)40, (short)15, false)
                .add(PropService.HOLD_POSITION, (short)40, (short)15, false)
                .add(PropService.BLOOD_PILL, (short)40, (short)15, false)
                .add(PropService.BLOOD_BAG, (short)40, (short)15, false)
                .add(PropService.REBIRTH, (short)40, (short)15, false)
                .add(PropService.DARK_VISION, (short)40, (short)15, false)
                .add(PropService.IMMUNITY_ALLOW_MOVE, (short)40, (short)15, false)
                .add(PropService.IMMUNITY_NOT_MOVE, (short)40, (short)15, false)
                .add(PropService.REBOUND, (short)40, (short)15, false)
                .add(PropService.NEAR_ENEMY_REMIND, (short)40, (short)15, false)
                .add(PropService.PROP_BOMB, (short)40, (short)15, false)
                .add(PropService.MONEY, (short)40, (short)15, true)
                .add(PropService.GIFT_BOX, (short)40, (short)15, true);
                */
        gamePropConfigMap = new GamePropConfigMap(50)
                .add(PropConfig.SPEED_UP_LEVEL_1, (short)100, (short)6000, false)
                .add(PropConfig.SPEED_UP_LEVEL_2, (short)100, (short)6000, false)
                .add(PropConfig.SPEED_DOWN_LEVEL_1, (short)100, (short)6000, false)
                .add(PropConfig.SPEED_DOWN_LEVEL_2, (short)100, (short)6000, false)
                .add(PropConfig.BLOOD_PILL, (short)100, (short)6000, false)
                .add(PropConfig.BLOOD_BAG, (short)100, (short)6000, false)
                .add(PropConfig.FLASH_LEVEL_1, (short)100, (short)6000, false)
                .add(PropConfig.FLASH_LEVEL_2, (short)100, (short)6000, false);
                //.add(PropConfig.MONEY, (short)100, (short)60, true)
                //.add(PropConfig.GIFT_BOX, (short)100, (short)60, true);
    }

    @Override
    public Class<? extends User> getUserClass() {
        return SurviveUser.class;
    }

    @Override
    public void logic() {
        super.logic();

        // 主要不同的逻辑是合围的时候可以抢道具

    }

    @Override
    public byte[] generateCustomResultMessage(User user) {
        int rank = 1;
        for (User gameUser: getUserMap().values()) {
            if (gameUser.gameOverTime == 0 || gameUser.gameOverTime > user.gameOverTime) {
                rank ++;
            }
        }

        ByteBuilder bb = new ByteBuilder();
        bb.append(rank);
        bb.append(0);
        return bb.getBytes();
    }

    @Override
    protected List<NPC> generateNPC() {
        return null;
    }

    /**
     * 两种情况可以交换道具，
     * 1：两人是队友，离的够进
     * 2：一人落单，对方两人距离都很近，被逼交出道具
     */
    @Override
    protected boolean checkIfPropCanTransfer(User user, User targetUser, byte propId) {
        SurviveUser surviveUser = (SurviveUser) targetUser;
        SurviveUser targetSurviveUser = (SurviveUser) targetUser;

        if (surviveUser.teamId == targetSurviveUser.teamId &&
                surviveUser.getPositionPoint().distance(targetSurviveUser.getPositionPoint()) < PROP_TRANSFER_DISTANCE) {
            // 第一种情况
            return true;
        } else {
            // 第二种情况
            if (surviveUser.getPositionPoint().distance(targetSurviveUser.getPositionPoint()) > PROP_TRANSFER_CATCH_DISTANCE) {
                return false;
            }
            int userCount = 0;
            List<SurviveUser> targetUserList = getTeamUser(targetSurviveUser.teamId);
            for (SurviveUser teamUser : targetUserList) {
                if (surviveUser.getPositionPoint().distance(teamUser.getPositionPoint()) <= PROP_TRANSFER_CATCH_DISTANCE) {
                    userCount ++;
                }
            }
            if (userCount > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取同一个队的所有用户
     */
    List<SurviveUser> getTeamUser(byte teamId) {
        List<SurviveUser> teamUserList = new LinkedList<>();
        for(User user: getUserMap().values()) {
            SurviveUser surviveUser = (SurviveUser) user;
            if (surviveUser.teamId == teamId) {
                teamUserList.add(surviveUser);
            }
        }
        return teamUserList;
    }

    @Override
    public GamePropConfigMap getGamePropConfigMap() {
        return gamePropConfigMap;
    }

    @Override
    protected int getCustomPropPrice(short propTypeId) {
        return 0;
    }

    @Override
    protected short[] getStorePropIds() {
        return new short[0];
    }

}

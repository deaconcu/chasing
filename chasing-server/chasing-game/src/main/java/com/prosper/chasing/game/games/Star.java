package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;

import java.util.List;

/**
 * 捡星星的游戏
 * 分成两队，哪队捡的星星最多胜利
 */
@MetaGameAnno("star")
public class Star extends Game {

    public static byte PROP_TRANSFER_DISTANCE = 2;

    protected static GamePropConfigMap gamePropConfigMap;

    /***********************
     * 自定义User
     ***********************/
    public static class DarknessUser extends User {

        private int genCount;
        private byte lightening;

    }

    /***********************
     * 新的Prop
     ***********************/
    public static final short PROP_GEN = 301;

    public static class Gen extends PropConfig.Prop {

        public Gen() {
            allowTargetType = new byte[]{PropMessage.TYPE_NONE};
            propTypeId = PROP_GEN;
            isInPackage = false;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            return false;
        }
    }

    /**
     * 一些设定
     */
    static {
        PropConfig.putProp(new Gen());

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
        gamePropConfigMap = new GamePropConfigMap(1)
                .add(PropConfig.MONEY, (short)500, (short)300, true)
                .add(PropConfig.GIFT_BOX, (short)500, (short)150, true);
    }

    @Override
    public Class<? extends User> getUserClass() {
        return DarknessUser.class;
    }

    @Override
    public void logic() {
        super.logic();
    }

    /**
     * 必须在定义距离内，而且宝石数量多的哪一方能抢少的那一方的所有宝石
     */
    @Override
    protected boolean checkIfPropCanTransfer(User user, User targetUser, byte propId) {
        if (propId != PROP_GEN) {
            return false;
        }
        if (user.getPositionPoint().distance(user.getPositionPoint()) > PROP_TRANSFER_DISTANCE) {
            return false;
        }
        if (user.getProp(propId) < targetUser.getProp(propId)) {
            return true;
        }
        return false;
    }

    @Override
    public ByteBuilder generateResultMessage(User user) {
        int rank = 1;
        for (User gameUser: getUserMap().values()) {
            if (gameUser.gameOverTime == 0 || gameUser.gameOverTime > user.gameOverTime) {
                rank ++;
            }
        }

        ByteBuilder bb = new ByteBuilder();
        bb.append(Constant.MessageType.RESULT);
        bb.append(rank);
        bb.append(0);

        return bb;
    }

    @Override
    protected List<NPC> generateNPC() {
        return null;
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

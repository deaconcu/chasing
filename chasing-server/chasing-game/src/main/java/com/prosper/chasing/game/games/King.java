package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;

import com.prosper.chasing.game.base.PropConfig;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;

import java.util.LinkedList;
import java.util.List;

@MetaGameAnno("king")
public class King extends Game {

    protected static GamePropConfigMap gamePropConfigMap;

    /***********************
     * 自定义User
     ***********************/
    public static class KingUser extends User {}

    /***********************
     * 新的Prop
     ***********************/
    public static final short PROP_SCEPTER = 101;

    public static class Scepter extends PropConfig.Prop {

        public Scepter() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = PROP_SCEPTER;
            autoUse = true;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            user.addBuff(BUFF_SCEPTER);
            return true;
        }
    }

    /***********************
     * 新的Buff
     ***********************/
    public static final byte BUFF_SCEPTER = 50;


    /**
     * 一些设定
     */
    static {
        PropConfig.putProp(new Scepter());
        BuffConfig.putBuff(new BuffConfig(BUFF_SCEPTER, (short)20));
        /*
        gamePropConfigMap = new GamePropConfigMap(50)
                .add(PropConfig.MARK, (short)40, (short)15, false)
                .add(PropConfig.INVISIBLE_LEVEL_1, (short)40, (short)15, false)
                .add(PropConfig.INVISIBLE_LEVEL_2, (short)40, (short)15, false)
                .add(PropConfig.ANTI_INVISIBLE, (short)40, (short)15, false)
                .add(PropConfig.RETURN_TO_INIT_POSITION, (short)40, (short)15, false)
                .add(PropConfig.TRANSPORT, (short)40, (short)15, false)
                .add(PropConfig.RANDOM_POSITION, (short)40, (short)15, false)
                .add(PropConfig.MOVE_FORWARD, (short)40, (short)15, false)
                .add(PropConfig.FOLLOW, (short)40, (short)15, false)
                .add(PropConfig.SPEED_UP_LEVEL_1, (short)40, (short)15, false)
                .add(PropConfig.SPEED_UP_LEVEL_2, (short)40, (short)15, false)
                .add(PropConfig.SPEED_DOWN_LEVEL_1, (short)40, (short)15, false)
                .add(PropConfig.SPEED_DOWN_LEVEL_2, (short)40, (short)15, false)
                .add(PropConfig.HOLD_POSITION, (short)40, (short)15, false)
                .add(PropConfig.BLOOD_PILL, (short)40, (short)15, false)
                .add(PropConfig.BLOOD_BAG, (short)40, (short)15, false)
                .add(PropConfig.REBIRTH, (short)40, (short)15, false)
                .add(PropConfig.DARK_VISION, (short)40, (short)15, false)
                .add(PropConfig.IMMUNITY_ALLOW_MOVE, (short)40, (short)15, false)
                .add(PropConfig.IMMUNITY_NOT_MOVE, (short)40, (short)15, false)
                .add(PropConfig.REBOUND, (short)40, (short)15, false)
                .add(PropConfig.NEAR_ENEMY_REMIND, (short)40, (short)15, false)
                .add(PropConfig.PROP_BOMB, (short)40, (short)15, false)
                .add(PropConfig.MONEY, (short)40, (short)15, true)
                .add(PropConfig.GIFT_BOX, (short)40, (short)15, true);
                */

        gamePropConfigMap = new GamePropConfigMap(10)
                .add(PropConfig.SPEED_UP_LEVEL_1, (short)100, (short)60, false)
                .add(PropConfig.SPEED_UP_LEVEL_2, (short)100, (short)60, false)
                .add(PropConfig.SPEED_DOWN_LEVEL_1, (short)100, (short)60, false)
                .add(PropConfig.SPEED_DOWN_LEVEL_2, (short)100, (short)60, false)
                .add(PropConfig.BLOOD_PILL, (short)100, (short)60, false)
                .add(PropConfig.BLOOD_BAG, (short)100, (short)60, false)
                .add(PropConfig.FLASH_LEVEL_1, (short)100, (short)60, false)
                .add(PropConfig.FLASH_LEVEL_2, (short)100, (short)60, false)
                .add(PropConfig.MONEY, (short)100, (short)60, true)
                .add(PropConfig.GIFT_BOX, (short)100, (short)60, true);
    }

    @Override
    public Class<? extends User> getUserClass() {
        return KingUser.class;
    }

    @Override
    public void logic() {
        super.logic();
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
    public GamePropConfigMap getGamePropConfigMap() {
        return gamePropConfigMap;
    }

    @Override
    protected int getCustomPropPrice(short propTypeId) {
        if (propTypeId == PropConfig.MARK) return 10;
        else return -1;
    }

    @Override
    protected short[] getStorePropIds() {
        return new short[]{1,2,3,4,5,6,7,8,9,10};
    }

    @Override
    protected List<NPC> generateNPC() {
        List<NPC> npcList = new LinkedList<>();
        // TODO
        /*
        npcList.add(new Merchant(
                this, 1, (short)1, "范蠡", false, new short[]{1,2,3,4,5},
                new Position((byte)0, navimeshGroup.getRandomPositionPoint("king"), 0)));
                */
        return npcList;
    }

}


package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;

import com.prosper.chasing.game.base.PropConfig;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;

import java.util.List;

@MetaGameAnno("king")
public class King extends Game {

    /***********************
     * 自定义User
     ***********************/
    public static class KillerUser extends User {

    }

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
                .add(PropConfig.MONEY, (short)500, (short)20, true)
                .add(PropConfig.GIFT_BOX, (short)500, (short)20, true);
    }

    @Override
    public Class<? extends User> getUserClass() {
        return KillerUser.class;
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
    protected List<NPC> generateNPC() {
        return null;
    }

}

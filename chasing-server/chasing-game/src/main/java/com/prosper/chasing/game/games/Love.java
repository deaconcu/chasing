package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;

import java.util.List;

@MetaGameAnno("love")
public class Love extends Game {

    public static byte LOVE_TYPE_SINGLE = 1; // 单身狗
    public static byte LOVE_TYPE_IN_LOVE = 2; // 有对象

    public static byte NPC_ID_COMPERE = 1;

    /***********************
     * 自定义User
     ***********************/
    public static class LoveUser extends User {
        public int loverUserId;
        public byte loveType;

        public byte gender;
        public int flowerCount;

        public String question;
        public List<Character> loverQuestion;
        public List<Character> haterQuestion;

        public int marryUserId;
    }

    /***********************
     * 自定义NPC
     ***********************/

    /**
     * 主婚人
     */
    public static class Compere extends NPC {

        public Compere(Game game) {
            super(game);
        }

        @Override
        public void action(User user, byte actionId, Object[] actionValues) {
            int userId = 0;
            if (actionValues.length > 1 && actionValues[0] instanceof Integer && actionValues[0] != null) {
                userId = (int) actionValues[0];
            } else {
                return;
            }

            if (actionId == 1) {
            }

        }
    }

    /***********************
     * 新的Prop
     ***********************/
    public static final short PROP_ANSWER = 401;
    public static final short PROP_FLOWER = 402;

    public static class ANSWER extends PropConfig.Prop {

        public ANSWER() {
            allowTargetType = new byte[]{PropMessage.TYPE_SELF};
            propTypeId = PROP_ANSWER;
            isInPackage = true;
        }

        @Override
        public boolean doUse(PropMessage message, User user, Game game) {
            return false;
        }
    }

    public static class Flower extends PropConfig.Prop {

        public Flower() {
            allowTargetType = new byte[]{PropMessage.TYPE_NONE};
            propTypeId = PROP_FLOWER;
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
        PropConfig.putProp(new Flower());

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
        return LoveUser.class;
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
        bb.append(rank);
        bb.append(0);

        return bb;
    }

    @Override
    protected List<NPC> generateNPC() {
        return null;
    }

}

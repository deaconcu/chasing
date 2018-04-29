package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;

import java.util.LinkedList;
import java.util.List;

/**
 * 《黑暗》
 * 黑暗之中，我们在荒原之上
 * 身边有一辆卡车，卡车上有我们生命的依靠-火把
 * 荒野之上有狼，它们怕火，不敢接近我们的车队
 * 但是，火把维持不了多久，我们不得不到处去搜集木材用来制作火把
 * 周围有树，树上有会发光的种子，我们砍树制作火把
 * 如果离开了火光的距离，会立即被捕食
 * 跟随车队撤退到基地即为成功,离基地越近，树越少
 * 路上有材料商人
 */
@MetaGameAnno("darkness")
public class Darkness extends Game {

    public static byte USER_DEAD_DISTANCE = 5;

    public static byte NPC_ID_TRUCK = 1;

    public static byte LIGHTING = 1;
    public static byte NOT_LIGHTING = 1;

    protected static GamePropConfigMap gamePropConfigMap;

    /***********************
     * 自定义User
     ***********************/
    public static class DarknessUser extends User {

        private int woodCount;
        private byte islighting = NOT_LIGHTING;

        /**
         * 检查是否用户已经完成游戏，比如死亡或者胜利之类的，默认生命为0为结束游戏
         */
        protected void checkIfEnd() {
            if (getPositionPoint().distance(getGame().getMoveableNPCMap().get(NPC_ID_TRUCK).getPositionPoint()) < USER_DEAD_DISTANCE) {
                return;
            }

            // 先判断当前目标对象是否亮灯
            GameObject target = getCurrentTargetObject();
            if (getCurrentTargetObject() instanceof DarknessUser) {
                if (((DarknessUser)target).islighting == LIGHTING &&
                        (target.getPositionPoint().distance(getPositionPoint()) < USER_DEAD_DISTANCE)) {
                    return;
                }
            }

            // 再判断周围是否有对象亮灯
            for (User user: getGame().getUserMap().values()) {
                if (((DarknessUser)user).islighting == LIGHTING &&
                        (user.getPositionPoint().distance(getPositionPoint()) < USER_DEAD_DISTANCE)) {
                    return;
                }
            }
            setState(Constant.UserState.GAME_OVER);
            gameOverTime = System.currentTimeMillis();
        }

    }

    /***********************
     * 自定义NPC
     ***********************/

    /**
     * 卡车
     * 属性：木材量
     * 木材量随时间推移而减少，玩家可以借一根木材做成火把去获取新的木材
     *
     * 运动：定义为匀速运动1分钟后休息1分钟,游戏开始后休息1分钟，连续运动15个周期
     */
    public static class Truck extends NPC {

        private static int MOVE_SPEED = 6;

        private static byte ACTION_PUT_WOOD = 1;
        private static byte ACTION_GET_WOOD = 2;

        private byte woodCount;

        public Truck(Game game) {
            super(game);
        }

        @Override
        public void generateNextMoveSpeed() {
            if ((System.currentTimeMillis() - game.startTime / 1000) % 2 == 0) {
                speed = 0;
            } else {
                speed = MOVE_SPEED;
            }
        }

        @Override
        public void action(User user, byte actionId, Object[] actionValues) {
            DarknessUser darknessUser = (DarknessUser) user;
            int value = 0;
            if (actionValues.length > 1 && actionValues[0] instanceof Integer && actionValues[0] != null) {
                value = (int) actionValues[0];
            } else {
                return;
            }

            if (value > woodCount) {
                return;
            }

            if (actionId == 1) {
                if (actionId == ACTION_GET_WOOD && value < woodCount)  {
                    darknessUser.woodCount += value;
                    this.woodCount -= value;
                } else if (actionId == ACTION_PUT_WOOD && value > darknessUser.woodCount) {
                    darknessUser.woodCount += value;
                    this.woodCount += value;
                }
            }
        }
    }

    /**
     * 树：可以从树上获取木材
     */
    public static class Tree extends NPC {

        // 树含有的木材量
        private short woodCount;

        public Tree(Game game) {
            super(game);
            movable = false;
        }

        @Override
        public void action(User user, byte actionId, Object[] actionValues) {
            if (actionId == 1) {
                // TODO self distruction
                user.increaseProp(PROP_WOOD, woodCount);
            }
        }
    }

    /***********************
     * 新的Prop
     ***********************/
    public static final short PROP_WOOD = 301;

    public static class Gen extends PropConfig.Prop {

        public Gen() {
            allowTargetType = new byte[]{PropMessage.TYPE_NONE};
            propTypeId = PROP_WOOD;
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
        List<NPC> npcList = new LinkedList<>();
        Truck truck = new Truck(this);
        truck.setId(NPC_ID_TRUCK);
        truck.setPath(null);
        npcList.add(truck);
        return npcList;
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

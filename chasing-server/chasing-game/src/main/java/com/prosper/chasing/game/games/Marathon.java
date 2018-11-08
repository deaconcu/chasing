package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.base.Point;
import com.prosper.chasing.game.map.Block;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@MetaGameAnno("marathon")
public class Marathon extends GameBase {

    private static final int POSITION_X_LIMIT = 10;
    private static final int POSITION_Z_LIMIT = 10;

    protected static GamePropConfigMap gamePropConfigMap;

    private Point startPosition;
    private Point endPosition;

    static {
        gamePropConfigMap = new GamePropConfigMap(10, 1)
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

    // 出发地
    private Point birthArea = new Point(0, 0, 0);

    public static class MarathonUser extends User {

        // 完成时间
        private int finishTime;

        public MarathonUser() {}

        public MarathonUser(User user) {
            setId(user.getId());
            setPropMap(user.getPropMap());
            setGame(user.getGame());
            setState(user.getState());
        }
    }

    @Override
    public Class<? extends User> getUserClass() {
        return MarathonUser.class;
    }

    @Override
    public void customInit() {
        Block block = gameMap.mainRoad.blockList.get(5);
        startPosition = new Point(block.position.x * 1000, 0, block.position.y * 1000);

        block = gameMap.mainRoad.blockList.get(50);
        endPosition = new Point(block.position.x * 1000, 0, block.position.y * 1000);
    }

    @Override
    public void logic() {
        super.logic();
    }

    @Override
    public ByteBuilder generateResultMessage(User user) {
        byte rank = 2;
        for (User gameUser: getUserMap().values()) {
            if (gameUser.gameOverTime < user.gameOverTime) {
                rank ++;
            }
        }

        ByteBuilder bb = new ByteBuilder();
        bb.append(0);
        bb.append(Constant.MessageType.RESULT);
        bb.append(rank);
        //bb.append(0);

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
    protected List<NPCOld> generateNPC() {
        List<NPCOld> npcOldList = new LinkedList<>();
        // TODO
        /*
        npcOldList.add(new Merchant(
                this, 1, (short)1, "范蠡", false, new short[]{1,2,3,4,5},
                new Position((byte)0, navimeshGroup.getRandomPositionPoint("king"), 0)));
                */
        return npcOldList;
    }

    /**
     * 玩家初始位置为地图主路的开始位置, 朝向随机, 移动状态为idle
     */
    @Override
    protected void initUser(Map<Integer, User> userMap) {
        for (User user: userMap.values()) {
            user.setPosition(startPosition);
            user.setRotateY(ThreadLocalRandom.current().nextInt(360));
            user.setMoveState(Constant.MoveState.IDLE);
        }
    }

    /**
     * 用户位置有变化时需要检查用户是否到达终点
     */
    @Override
    protected void doPositionChanged(User user) {
        // TODO 需要优化，不需要每次都计算平方根
        if (user.getPosition().distance(endPosition) < 2000) {
            user.setState(Constant.UserState.GAME_OVER);

            ByteBuilder resultMessage = generateResultMessage(user);
            user.offerMessage(resultMessage);
        }
    }

    /**
     * 需要创建一个开始地点和结束地点
     */
    @Override
    protected void initDynamicObject() {
        addDynamicObject(new DynamicGameObject(
                Enums.DynamicGameObjectType.FLAG.getValue(),
                0,
                new Point(startPosition.x, 0, startPosition.z),
                45));

        addDynamicObject(new DynamicGameObject(
                Enums.DynamicGameObjectType.FLAG.getValue(),
                0,
                new Point(endPosition.x, 0, endPosition.z),
                45));
    }
}

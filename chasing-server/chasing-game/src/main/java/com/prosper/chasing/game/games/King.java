package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;

import com.prosper.chasing.game.base.PropConfig;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@MetaGameAnno("king")
public class King extends GameBase {

    protected static GamePropConfigMap gamePropConfigMap;

    private static byte groupId = 1;

    /**
     * 一些设定
     */
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
    protected void customInitUser(Map<Integer, User> userMap) {
        byte groupId = 1;
        for (User user: userMap.values()) {
            Point3 point3 = gameMap.getRandomPoint(Enums.RoadPointType.CENTER).getPoint().toPoint3();
            user.setPoint3(point3);
            user.setRotateY(ThreadLocalRandom.current().nextInt(360));
            user.setMoveState(Constant.MoveState.IDLE);

            user.setGroupId(groupId ++);
        }
    }

}

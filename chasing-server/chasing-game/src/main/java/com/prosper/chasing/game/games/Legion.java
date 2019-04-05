package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@MetaGameAnno("legion")
public class Legion extends GameBase {

    protected static GamePropConfigMap gamePropConfigMap;

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
    protected void customInitUser(Map<Integer, User> userMap) {
        byte groupId = 1;
        int count = 1;
        int halfSize = userMap.size() / 2;

        for (User user: userMap.values()) {
            Point3 point3 = gameMap.getRandomRoadPoint(Enums.RoadPointType.CENTER).getPoint().toPoint3();
            user.setPoint3(point3);
            user.setRotateY(ThreadLocalRandom.current().nextInt(360));
            user.setMoveState(Constant.MoveState.IDLE);

            if ((count ++) / halfSize == 1 && groupId == 0) groupId = 1;
            user.setGroupId(groupId);
        }
    }
}

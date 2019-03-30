package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;

import com.prosper.chasing.game.base.PropConfig;
import com.prosper.chasing.game.map.Hexagon;
import com.prosper.chasing.game.map.Segment;
import com.prosper.chasing.game.map.SpecialSection;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;
import com.prosper.chasing.game.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@MetaGameAnno("king")
public class King extends GameBase {

    private static GameConfig gameConfig;

    static {
        gameConfig = new GameConfig(
                "Finish Time", Enums.RankValueType.TIME_DESCEND,
                "Kills", Enums.RankValueType.INT_DESCEND,
                new HashMap<>(),
                new short[]{1,2,3,4,5,6,7,8,9,10},
                new GamePropConfigMap(10, 1)
                        .add(PropConfig.SPEED_UP_LEVEL_1, (short)100, (short)60, false)
                        .add(PropConfig.SPEED_UP_LEVEL_2, (short)100, (short)60, false)
                        .add(PropConfig.SPEED_DOWN_LEVEL_1, (short)100, (short)60, false)
                        .add(PropConfig.SPEED_DOWN_LEVEL_2, (short)100, (short)60, false)
                        .add(PropConfig.BLOOD_PILL, (short)100, (short)60, false)
                        .add(PropConfig.BLOOD_BAG, (short)100, (short)60, false)
                        .add(PropConfig.FLASH_LEVEL_1, (short)100, (short)60, false)
                        .add(PropConfig.FLASH_LEVEL_2, (short)100, (short)60, false)
                        .add(PropConfig.MONEY, (short)100, (short)60, true)
                        .add(PropConfig.GIFT_BOX, (short)100, (short)60, true)
        );
    }

    @Override
    public GameConfig getGameConfig() {
        return gameConfig;
    }

    @Override
    public void doCustomUserLogic(User user) {
        if (user.getProp(PropConfig.SCEPTER) > 0) user.addBuff(BuffConfig.HOLD_SCEPTER, (short)-1, false);
    }

    @Override
    public void customLogic() {
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

    /**
     * 需要创建一个开始地点和结束地点
     */
    @Override
    protected void initGameObject() {

    }

}


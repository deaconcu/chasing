package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;

import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.prosper.chasing.game.util.Enums.PropType.*;

@MetaGameAnno("king")
public class King extends GameBase {

    private static GameConfig gameConfig;

    static {
        gameConfig = new GameConfig(
                "Finish Time", RankValueType.TIME_DESCEND,
                "Kills", RankValueType.INT_DESCEND,
                new HashMap<>(),
                new PropType[]{
                        FLASH_LEVEL_1,
                        FLASH_LEVEL_2,
                        SPEED_UP_LEVEL_1,
                        SPEED_UP_LEVEL_2,
                        SPEED_DOWN_LEVEL_1,
                        SPEED_DOWN_LEVEL_2,
                        BLOOD_PILL,
                        BLOOD_BAG,
                        MONEY,
                        GIFT_BOX
                }
        );
    }

    @Override
    public GameConfig getGameConfig() {
        return gameConfig;
    }

    @Override
    public PropGenerator getPropGenerator() {
        // TODO
        return null;
    }

    @Override
    public void doCustomUserLogic(User user) {
        if (user.getProp(PropType.SCEPTER) > 0) user.addBuff(BuffType.HOLD_SCEPTER, (short)-1, false);
    }

    @Override
    public void customLogic() {
    }

    @Override
    protected void customInitUser(Map<Integer, User> userMap) {
        byte groupId = 1;
        for (User user: userMap.values()) {
            Point3 point3 = gameMap.getRandomRoadPoint(RoadPointType.CENTER).getPoint().toPoint3();
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


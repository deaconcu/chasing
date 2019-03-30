package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.map.Hexagon;
import com.prosper.chasing.game.map.RoadSection;
import com.prosper.chasing.game.map.Segment;
import com.prosper.chasing.game.map.SpecialSection;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;
import com.prosper.chasing.game.base.InteractiveObjects.*;

import com.prosper.chasing.game.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@MetaGameAnno("marathon")
public class Marathon extends GameBase {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static GameConfig gameConfig;

    private Point3 startPosition;
    private Point3 endPosition;

    private Stationary endFlag;

    static {
        gameConfig = new GameConfig(
                "Finish Time", Enums.RankValueType.TIME_ASCEND,
                "Remain Distance", Enums.RankValueType.INT_ASCEND,
                new HashMap<>(),
                new short[]{1,2,3,4,5,6,7,8,9,10},
                new GamePropConfigMap(
                        Integer.MAX_VALUE, 200)
                        .add(PropConfig.SPEED_UP_LEVEL_1, (short)100, (short)60, false)
                        .add(PropConfig.SPEED_UP_LEVEL_2, (short)100, (short)60, false)
                        .add(PropConfig.SPEED_DOWN_LEVEL_1, (short)100, (short)60, false)
                        .add(PropConfig.SPEED_DOWN_LEVEL_2, (short)100, (short)60, false)
                        .add(PropConfig.BLOOD_PILL, (short)100, (short)60, false)
                        .add(PropConfig.BLOOD_BAG, (short)100, (short)60, false)
                        .add(PropConfig.FLASH_LEVEL_1, (short)100, (short)60, false)
                        .add(PropConfig.FLASH_LEVEL_2, (short)100, (short)60, false)
                        .add(PropConfig.MONEY, (short)100, (short)60, false)
                        .add(PropConfig.GIFT_BOX, (short)100, (short)60, false)
        );
    }

    @Override
    public GameConfig getGameConfig() {
        return gameConfig;
    }

    @Override
    public void customInit() {
        RoadSection[] roadSections = gameMap.getNearestRoadSectioon(gameMap.getStart());
        RoadSection selected = roadSections[0];
        if (selected.getStart().getPoint().equals(gameMap.getStart())) {
            startPosition = selected.getBetween()[1].getPoint().toPoint3();
            endPosition = selected.getBetween()[RoadSection.SUB_SECTION_SIZE - 2].getPoint().toPoint3();
        } else {
            startPosition = selected.getBetween()[RoadSection.SUB_SECTION_SIZE - 2].getPoint().toPoint3();
            endPosition = selected.getBetween()[1].getPoint().toPoint3();
        }
        /*
        roadSections = gameMap.getNearestRoadSectioon(gameMap.getEnd());
        selected = roadSections[0];
        if (selected.getStart().getPoint().equals(gameMap.getEnd())) {
            endPosition = selected.getBetween()[2].getPoint().toPoint3();
        } else {
            endPosition = selected.getBetween()[RoadSection.SUB_SECTION_SIZE - 3].getPoint().toPoint3();
        }
        */

        for (User user: getUserMap().values()) {
            rank.setFirstValue(user.getId(), 7199);
            rank.setSecondValue(user.getId(), 99999);
        }
    }

    /**
     * 玩家初始位置为地图主路的开始位置, 朝向随机, 移动状态为idle
     */
    @Override
    protected void customInitUser(Map<Integer, User> userMap) {
        byte groupId = 1;
        for (User user: userMap.values()) {
            int x = startPosition.x + ThreadLocalRandom.current().nextInt(6000) - 3000;
            int z = startPosition.z + ThreadLocalRandom.current().nextInt(6000) - 3000;

            user.setPoint3(new Point3(x, 0 ,z));
            user.setRotateY(ThreadLocalRandom.current().nextInt(360 * 1000));
            user.setMoveState(Constant.MoveState.IDLE);

            user.setStrength(50000);
            user.setGroupId(groupId ++);

            // for test
            /*
            if (user.getId() == 1) {
                user.addBuff(BuffConfig.INVISIBLE_LEVEL_1);
            }
            */
        }
    }

    /**
     * 用户位置有变化时需要检查用户是否到达终点
     */
    @Override
    protected void doPositionChanged(User user) {
        // TODO 需要优化，不需要每次都计算平方根
        if (user.getPoint3().distance(endPosition) < 2000) {
            user.setState(Constant.UserState.FINISHED);
            rank.setFirstValue(user.getId(), (int)(System.currentTimeMillis() - startTime) / 1000);
            rank.setSecondValue(user.getId(), 0);
        }
    }

    /**
     * 创建地图上的游戏对象
     */
    @Override
    protected void initGameObject() {
        super.initGameObject();
        Map<Hexagon, RoadPoint[]> crossRoadPointMap = gameMap.randomBranchCrossList(0.3f);
        for (Map.Entry<Hexagon, RoadPoint[]> entry : crossRoadPointMap.entrySet()) {
            RoadPoint farRoadPoint = null;
            int distance = 0;
            for (RoadPoint roadPoint : entry.getValue()) {
                int currDistance = roadPoint.getPoint().distance(0, 0);
                if (currDistance > distance) {
                    farRoadPoint = roadPoint;
                    distance = currDistance;
                }
            }
            addGameObject(new SignPost(farRoadPoint.getPoint().toPoint3(),
                    - Util.getDegree(gameMap.getRoadDirectionToEnd(entry.getKey().getId()))));
        }

        addGameObject(new Stationary(Enums.StationaryType.FLAG, new Point3(startPosition.x, 0, startPosition.z), 45));
        addGameObject(new Stationary(Enums.StationaryType.FLAG, new Point3(endPosition.x, 0, endPosition.z), 45));
    }

    @Override
    protected void doPropLogic() {
        // 生成新的道具
        int propRemained = getGameConfig().getPropConfig().getPropRemained((int)getGameTime() / 1000);
        int count = propList.size() - propRemained;
        if (count <= 0) return;

        while (count > 0) {
            EnvProp envProp = new EnvProp(this);

            envProp.typeId = propList.removeFirst();
            envProp.setId(nextPropSeqId ++);

            Point2 point2 = gameMap.getRandomPoint(Enums.RoadPointType.CENTER).getPoint();
            envProp.setPoint3(new Point3(point2.x, 100, point2.y));
            envProp.createTime = System.currentTimeMillis();
            envProp.vanishTime = envProp.createTime +
                    getGameConfig().getPropConfig().getPropConfig(envProp.typeId).duration * 1000;
            propMap.put(envProp.getId(), envProp);
            getObjectChangedSet().add(envProp);

            count --;

            log.info("created prop: {}:{}-{}:{}:{}", gameInfo.getId(), envProp.getId(),
                    envProp.getPoint3().x, envProp.getPoint3().y, envProp.getPoint3().z);
        }
    }

    @Override
    protected ByteBuilder createIntroductionMessage() {
        String[] lines = new String[] {
                "你被放置在无边的森林中",
                "和大家一样，请保持冷静",
                "这里是森林的终点",
                "率先到达这里的人，我将给予他金币作为奖赏",
                "所以",
                "请赶快开始吧",
        };

        int index = 0;

        ByteBuilder bb = new ByteBuilder();
        bb.append(0);
        bb.append(Constant.MessageType.INTRODUCTION);
        bb.append((byte)3);

        bb.append(Enums.TargetType.SELF.getValue());

        bb.append((byte)2);
        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        bb.append(Enums.TargetType.STATIONARY.getValue());
        bb.append(endFlag.getId());

        bb.append((byte)2);
        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        bb.append(Enums.TargetType.SELF.getValue());

        bb.append((byte)2);
        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        return bb;
    }
}

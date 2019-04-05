package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.map.Hexagon;
import com.prosper.chasing.game.map.RoadSection;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums.*;
import com.prosper.chasing.game.base.InteractiveObjects.*;
import static com.prosper.chasing.game.util.Enums.PropType.*;

import com.prosper.chasing.game.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@MetaGameAnno("marathon")
public class Marathon extends GameBase {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static GameConfig gameConfig;

    private AllAtOnce allAtOnce;
    private Point3 startPosition;
    private Point3 endPosition;

    private Point3 specialSectionPosition;

    private Stationary startFlag;
    private Stationary endFlag;

    public Marathon() {
        super();
        allAtOnce = new AllAtOnce(this);
        allAtOnce.set(FLASH_LEVEL_1, 10);
        allAtOnce.set(FLASH_LEVEL_2, 10);
        allAtOnce.set(SPEED_UP_LEVEL_1, 10);
        allAtOnce.set(SPEED_UP_LEVEL_2, 10);
        allAtOnce.set(SPEED_DOWN_LEVEL_1, 10);
        allAtOnce.set(SPEED_DOWN_LEVEL_2, 10);
        allAtOnce.set(BLOOD_PILL, 10);
        allAtOnce.set(BLOOD_BAG, 10);
        allAtOnce.set(MONEY, 10);
        allAtOnce.set(GIFT_BOX, 10);
    }

    static {
        gameConfig = new GameConfig(
                "Finish Time", RankValueType.TIME_ASCEND,
                "Remain Distance", RankValueType.INT_ASCEND,
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
        return allAtOnce;
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
        endPosition = gameMap.getEnd().toPoint3();

        specialSectionPosition = gameMap.specialSectionMap.get((short)0).getRoadPoints()[0].getPoint().toPoint3();
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
            createGameObject(new SignPost(farRoadPoint.getPoint().toPoint3(),
                    - Util.getDegree(gameMap.getRoadDirectionToEnd(entry.getKey().getId()))));
        }

        startFlag = new Stationary(StationaryType.FLAG, new Point3(startPosition.x, 0, startPosition.z), 45);
        endFlag = new Stationary(StationaryType.FLAG, new Point3(endPosition.x, 0, endPosition.z), 45);

        createGameObject(startFlag);
        createGameObject(endFlag);
    }

    @Override
    protected Prologue createPrologue() {
        Prologue prologue = new Prologue(new Prologue.PrologueItem[] {
                new Prologue.PrologueItem(TargetType.SELF, new String[] {
                        "你现在是在一个无边的森林中",
                        "身边有很多陌生人"
                }),
                new Prologue.PrologueItem(TargetType.STATIONARY, endFlag.getId(), new String[] {
                        "这里是森林的终点",
                        "率先到达这里的人，会得到金币作为奖赏"
                }),
                new Prologue.PrologueItem(TargetType.PROP, new String[] {
                        "森林中有各种宝物来帮助你到达终点"
                }),
                new Prologue.PrologueItem(TargetType.INTERACTIVE, new String[] {
                        "也有很多地方好像是不能通过的"
                }),
                new Prologue.PrologueItem(specialSectionPosition, new String[] {
                        "还有很多地方不便通行"
                }),
                new Prologue.PrologueItem(TargetType.SELF, new String[] {
                        "考验你的时候开始了",
                        "所以，请开始吧"
                }),
        });
        return prologue;
    }
}

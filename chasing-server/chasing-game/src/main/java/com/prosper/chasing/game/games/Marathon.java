package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.map.Hexagon;
import com.prosper.chasing.game.map.SpecialSection;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;
import com.prosper.chasing.game.base.InteractiveObjects.*;

import com.prosper.chasing.game.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@MetaGameAnno("marathon")
public class Marathon extends GameBase {

    private Logger log = LoggerFactory.getLogger(getClass());

    private static final int POSITION_X_LIMIT = 10;
    private static final int POSITION_Z_LIMIT = 10;

    protected static GamePropConfigMap gamePropConfigMap;

    private Point3 startPosition;
    private Point3 endPosition;

    private Stationary endFlag;

    static {
        gamePropConfigMap = new GamePropConfigMap(Integer.MAX_VALUE, 200)
                .add(PropConfig.SPEED_UP_LEVEL_1, (short)100, (short)60, false)
                .add(PropConfig.SPEED_UP_LEVEL_2, (short)100, (short)60, false)
                .add(PropConfig.SPEED_DOWN_LEVEL_1, (short)100, (short)60, false)
                .add(PropConfig.SPEED_DOWN_LEVEL_2, (short)100, (short)60, false)
                .add(PropConfig.BLOOD_PILL, (short)100, (short)60, false)
                .add(PropConfig.BLOOD_BAG, (short)100, (short)60, false)
                .add(PropConfig.FLASH_LEVEL_1, (short)100, (short)60, false)
                .add(PropConfig.FLASH_LEVEL_2, (short)100, (short)60, false)
                .add(PropConfig.MONEY, (short)100, (short)60, false)
                .add(PropConfig.GIFT_BOX, (short)100, (short)60, false);
    }

    // 出发地
    private Point3 birthArea = new Point3(0, 0, 0);

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
        startPosition = gameMap.getStart().toPoint3();
        endPosition = gameMap.getEnd().toPoint3();
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
    protected List<NPC> generateNPC() {
        List<NPC> npcOldList = new LinkedList<>();
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
    protected void customInitUser(Map<Integer, User> userMap) {
        byte groupId = 1;
        for (User user: userMap.values()) {
            int x = startPosition.x + ThreadLocalRandom.current().nextInt(2000) - 1000;
            int z = startPosition.z + ThreadLocalRandom.current().nextInt(2000) - 1000;

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
            user.setState(Constant.UserState.GAME_OVER);

            ByteBuilder resultMessage = generateResultMessage(user);
            user.offerMessage(resultMessage);
        }
    }

    /**
     * 需要创建一个开始地点和结束地点
     */
    @Override
    protected void initStationary() {
        for (SpecialSection specialSection: gameMap.specialSectionMap.values()) {
            if (!specialSection.isSingle()) continue;
            RoadPoint roadPoint = specialSection.getRoadPoints()[0];
            if (specialSection.getTerrainType() == Enums.TerrainType.RIVER)
                addGameObject(new River(roadPoint.getPoint().toPoint3(), roadPoint.getDegree()));
            else if (specialSection.getTerrainType() == Enums.TerrainType.STONE)
                addGameObject(new Stone(roadPoint.getPoint().toPoint3(), roadPoint.getDegree()));
            else if (specialSection.getTerrainType() == Enums.TerrainType.FIRE_FENCE)
                addGameObject(new FireFence(roadPoint.getPoint().toPoint3(), roadPoint.getDegree()));
            else if (specialSection.getTerrainType() == Enums.TerrainType.GATE)
                addGameObject(new Gate(roadPoint.getPoint().toPoint3(), roadPoint.getDegree()));
            else continue;
        }

        Map<Hexagon, RoadPoint[]> crossRoadPointMap = gameMap.randomBranchCrossList(0.3f);
        for (Map.Entry<Hexagon, RoadPoint[]> entry : crossRoadPointMap.entrySet()) {
            RoadPoint farRoadPoint = null;
            int distance = 0;
            for (RoadPoint roadPoint: entry.getValue()) {
                if (roadPoint.getPoint().distance(0, 0) > distance) farRoadPoint = roadPoint;
            }

            Enums.HexagonDirection direction = gameMap.getRoadDirectionToEnd(entry.getKey().getId());
            int degree = Util.getDegree(direction);

            addGameObject(new SignPost(farRoadPoint.getPoint().toPoint3(), degree));
            /*
            if (degree > 45 && degree < 225) {
            } else {
                addGameObject(new Stationary(
                        Enums.StationaryType.SIGNPOST_2, farRoadPoint.getPoint().toPoint3(), degree));
            }
            */
        }

        /*
        Point2 position;
        int rotateY = 0;
        Point3 point3;


        position = gameMap.mainRoad.hexagonList.get(38).position;
        rotateY = 0;
        if (!gameMap.isRoadAlongX(position.x, position.y)) {
            rotateY = 90 * 1000;
        }
        point3 = new Point3(position.x *  1000, 0, position.y * 1000);
        addGameObject(new Stationary(Enums.StationaryType.RIVER, point3, rotateY));

        position = gameMap.mainRoad.hexagonList.get(58).position;
        rotateY = 0;
        if (!gameMap.isRoadAlongX(position.x, position.y)) {
            rotateY = 90 * 1000;
        }
        point3 = new Point3(position.x *  1000, 0, position.y * 1000);
        addGameObject(new Stationary(Enums.StationaryType.STONES, point3, rotateY));

        position = gameMap.mainRoad.hexagonList.get(78).position;
        rotateY = 0;
        if (!gameMap.isRoadAlongX(position.x, position.y)) {
            rotateY = 90 * 1000;
        }

        point3 = new Point3(position.x *  1000, 0, position.y * 1000);
        addGameObject(new Stationary(Enums.StationaryType.FIRE_FENCE, point3, rotateY));

        position = gameMap.mainRoad.hexagonList.get(118).position;
        rotateY = 0;
        if (!gameMap.isRoadAlongX(position.x, position.y)) {
            rotateY = 90 * 1000;
        }

        point3 = new Point3(position.x *  1000, 0, position.y * 1000);
        addGameObject(new Stationary(Enums.StationaryType.GATE, point3, rotateY));


        position = gameMap.mainRoad.hexagonList.get(15).position;
        rotateY =  0;
        if (!gameMap.isRoadAlongX(position.x, position.y)) {
            rotateY = 90 * 1000;
            position.y += 3;
        } else {
            position.x += 3;
        }

        point3 = new Point3((startPosition.x + 3000), 0, startPosition.z);
        addGameObject(new Stationary(Enums.StationaryType.STORE, point3, rotateY));
        */

        addGameObject(new Stationary(
                Enums.StationaryType.FLAG,
                new Point3(startPosition.x, 0, startPosition.z),
                45));

        endFlag = new Stationary(
                Enums.StationaryType.FLAG,
                new Point3(endPosition.x, 0, endPosition.z),
                45);

        addGameObject(endFlag);
    }

    @Override
    protected void doPropLogic() {
        // 生成新的道具
        int propRemained = getGamePropConfigMap().getPropRemained((int)getGameTime() / 1000);
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
                    getGamePropConfigMap().getPropConfig(envProp.typeId).duration * 1000;
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

        bb.append(Constant.TargetType.TYPE_SELF);

        bb.append((byte)2);
        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        bb.append(Constant.TargetType.TYPE_STATIONARY);
        bb.append(endFlag.getId());

        bb.append((byte)2);
        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        bb.append(Constant.TargetType.TYPE_SELF);

        bb.append((byte)2);
        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        bb.append(lines[index].getBytes().length);
        bb.append(lines[index ++].getBytes());

        return bb;
    }
}

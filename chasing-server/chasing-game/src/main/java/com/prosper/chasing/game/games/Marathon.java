package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.base.Point;
import com.prosper.chasing.game.map.Block;
import com.prosper.chasing.game.map.BlockGroup;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import com.prosper.chasing.game.util.Enums;
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

    private Point startPosition;
    private Point endPosition;

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
        Block block = gameMap.getStartBlock();
        startPosition = new Point(block.position.x * 1000, 0, block.position.y * 1000);

        block = gameMap.getEndBlock();
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

            user.setPoint(new Point(x, 0 ,z));
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
        if (user.getPoint().distance(endPosition) < 2000) {
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
        for (BlockGroup blockGroup: gameMap.blockGroupMap.values()) {
            if (!blockGroup.isSingle()) continue;
            Point mapPoint = gameMap.getPoint(blockGroup.getStartBlockId());
            Point point = new Point(mapPoint.x *  1000, 0, mapPoint.z * 1000);
            int rotateY = 0;
            if (!gameMap.isRoadAlongX(mapPoint.x, mapPoint.z)) {
                rotateY = 90 * 1000;
            }
            if (blockGroup.getTerrainType() == Enums.TerrainType.RIVER) {
                addGameObject(new Stationary(Enums.StationaryType.RIVER, point, rotateY));
            } else if (blockGroup.getTerrainType() == Enums.TerrainType.STONE) {
                addGameObject(new Stationary(Enums.StationaryType.STONES, point, rotateY));
            } else if (blockGroup.getTerrainType() == Enums.TerrainType.FIRE_FENCE) {
                addGameObject(new Stationary(Enums.StationaryType.FIRE_FENCE, point, rotateY));
            } else if (blockGroup.getTerrainType() == Enums.TerrainType.GATE) {
                addGameObject(new Stationary(Enums.StationaryType.GATE, point, rotateY));
            }
        }

        List<Integer> crossBlockIdList = gameMap.getRandomRoadCrossPosition(30);
        for (int crossBlockId: crossBlockIdList) {
            Enums.Direction direction = gameMap.getPointsDirection(crossBlockId, gameMap.endBlockId);
            Point point = gameMap.getRoadSidePoint(crossBlockId);

            if (direction == Enums.Direction.RIGHT) {
                addGameObject(new Stationary(Enums.StationaryType.SIGNPOST_1, point, 0));
            } else if (direction ==  Enums.Direction.DOWN) {
                addGameObject(new Stationary(Enums.StationaryType.SIGNPOST_1, point, 90));
            } else if (direction ==  Enums.Direction.UP) {
                addGameObject(new Stationary(Enums.StationaryType.SIGNPOST_2, point, 0));
            } else {
                addGameObject(new Stationary(Enums.StationaryType.SIGNPOST_2, point, 90));
            }
        }

        /*
        Point2D position;
        int rotateY = 0;
        Point point;


        position = gameMap.mainRoad.hexagonList.get(38).position;
        rotateY = 0;
        if (!gameMap.isRoadAlongX(position.x, position.y)) {
            rotateY = 90 * 1000;
        }
        point = new Point(position.x *  1000, 0, position.y * 1000);
        addGameObject(new Stationary(Enums.StationaryType.RIVER, point, rotateY));

        position = gameMap.mainRoad.hexagonList.get(58).position;
        rotateY = 0;
        if (!gameMap.isRoadAlongX(position.x, position.y)) {
            rotateY = 90 * 1000;
        }
        point = new Point(position.x *  1000, 0, position.y * 1000);
        addGameObject(new Stationary(Enums.StationaryType.STONES, point, rotateY));

        position = gameMap.mainRoad.hexagonList.get(78).position;
        rotateY = 0;
        if (!gameMap.isRoadAlongX(position.x, position.y)) {
            rotateY = 90 * 1000;
        }

        point = new Point(position.x *  1000, 0, position.y * 1000);
        addGameObject(new Stationary(Enums.StationaryType.FIRE_FENCE, point, rotateY));

        position = gameMap.mainRoad.hexagonList.get(118).position;
        rotateY = 0;
        if (!gameMap.isRoadAlongX(position.x, position.y)) {
            rotateY = 90 * 1000;
        }

        point = new Point(position.x *  1000, 0, position.y * 1000);
        addGameObject(new Stationary(Enums.StationaryType.GATE, point, rotateY));


        position = gameMap.mainRoad.hexagonList.get(15).position;
        rotateY =  0;
        if (!gameMap.isRoadAlongX(position.x, position.y)) {
            rotateY = 90 * 1000;
            position.y += 3;
        } else {
            position.x += 3;
        }

        point = new Point((startPosition.x + 3000), 0, startPosition.z);
        addGameObject(new Stationary(Enums.StationaryType.STORE, point, rotateY));
        */

        addGameObject(new Stationary(
                Enums.StationaryType.FLAG,
                new Point(startPosition.x, 0, startPosition.z),
                45));

        endFlag = new Stationary(
                Enums.StationaryType.FLAG,
                new Point(endPosition.x, 0, endPosition.z),
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

            int randomBlockId = gameMap.getRandomRoadBlockId();
            int x = gameMap.getX(randomBlockId);
            int y = gameMap.getY(randomBlockId);
            envProp.setPoint(new Point(x * 1000, 100, y * 1000));
            envProp.createTime = System.currentTimeMillis();
            envProp.vanishTime = envProp.createTime +
                    getGamePropConfigMap().getPropConfig(envProp.typeId).duration * 1000;
            propMap.put(envProp.getId(), envProp);
            getEnvPropChangedList().add(envProp);

            count --;

            log.info("created prop: {}:{}-{}:{}:{}", gameInfo.getId(), envProp.getId(),
                    envProp.getPoint().x, envProp.getPoint().y, envProp.getPoint().z);
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

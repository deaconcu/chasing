package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.*;
import com.prosper.chasing.game.navmesh.Point;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@MetaGameAnno("marathon")
public class Marathon extends Game {

    private static final int POSITION_X_LIMIT = 10;
    private static final int POSITION_Z_LIMIT = 10;

    // 圈数
    private static final int loopCount = 5;
    // 路径点
    private static final Point[] flagList = new Point[]{
            new Point(10, 0, 10),
            new Point(-10, 0, 10),
            new Point(-10, 0, -10),
            new Point(10, 0, -10)
    };
    // 出发地
    private Point birthArea = new Point(0, 0, 0);

    public static class MarathonUser extends User {
        // 当前圈数
        private int loop;

        // 下一个需要到达的路径点
        private int pointIndex;

        // 完成时间
        private int finishTime;

        public MarathonUser(User user) {
            setId(user.getId());
            setPropMap(user.getPropMap());
            setGame(user.getGame());
            setState(user.getState());
        }
    }

    public void loadUserOld(List<User> userList) {
        for (User user: userList) {
            MarathonUser marathonUser = new MarathonUser(user);
            Position position = new Position(
                    (byte)1, new Point(birthArea.x, birthArea.y, birthArea.z), 0);
            marathonUser.setPosition(position);
            marathonUser.setInitPosition(position);
            getUserMap().put(marathonUser.getId(), marathonUser);
        }
    }

    /**
     * 马拉松图地形生成的原则:
     * 1. 起点在一头（0，0），终点在另外一头（1000，1000），边界是一个正方形，这样玩家可以有交集，不至于玩的人都看不见
     * 2. 是一个网状图
     */
    @Override
    public void generateTerrainBlock() {
        Point2D start = new Point2D(0, 0);
        Point2D end = new Point2D(1000, 1000);

        // 随机产生5组线条，从起点到终点，越靠近边界，方向越确定指向边界反方向

    }

    @Override
    public void logic() {
        super.logic();

        // 判断是否到路径点
        for (int flagIndex = 0; flagIndex < flagList.length; flagIndex++) {
            for (User user: getUserMap().values()) {
                MarathonUser mUser = (MarathonUser) user;
                if (flagIndex == mUser.pointIndex) {
                    //boolean isNear = isNear(flagList[flagIndex], user.getPosition().point, FETCH_DISTANCE);
                    boolean isNear = false;
                    if (isNear) {
                        if ((mUser.pointIndex == flagList.length - 1) && (mUser.loop == loopCount - 1)) {
                            mUser.loop++;
                            mUser.pointIndex = -1;
                            mUser.finishTime = (int) System.currentTimeMillis() / 1000;
                        } else if ((mUser.pointIndex == flagList.length - 1) && (mUser.loop < loopCount - 1)) {
                            mUser.loop++;
                            mUser.pointIndex = 0;
                        } else {
                            mUser.pointIndex ++;
                        }
                    }
                }
            }
        }

        generateProp();
    }

    @Override
    protected List<NPC> generateNPC() {
        return null;
    }

    public List<Result> getResultList() {
        List<Result> resultList = new LinkedList<>();
        for(User user: getUserMap().values()) {
            MarathonUser mUser = (MarathonUser) user;
            int cost = (int) (mUser.finishTime - startTime / 1000);
            resultList.add(new Result(mUser, cost, 0));
        }
        Collections.sort(resultList);
        return resultList;
    }

    @Override
    public GamePropConfigMap getGamePropConfigMap() {
        return null;
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

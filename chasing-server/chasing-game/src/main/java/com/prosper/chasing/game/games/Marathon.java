package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.MetaGameAnno;
import com.prosper.chasing.game.base.Position;
import com.prosper.chasing.game.base.User;

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
    private static final PositionPoint[] flagList = new PositionPoint[]{
            new PositionPoint(10, 0, 10),
            new PositionPoint(-10, 0, 10),
            new PositionPoint(-10, 0, -10),
            new PositionPoint(10, 0, -10)
    };
    // 出发地
    private PositionPoint birthArea = new PositionPoint(0, 0, 0);

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

    @Override
    public void loadUser(List<User> userList) {
        for (User user: userList) {
            MarathonUser marathonUser = new MarathonUser(user);
            Position position = new Position(
                    (byte)1, new PositionPoint(birthArea.x, birthArea.y, birthArea.z), 0);
            marathonUser.setPosition(position);
            marathonUser.setInitPosition(position);
            getUserMap().put(marathonUser.getId(), marathonUser);
        }
    }

    @Override
    public void logic() {
        removeInvalidProp();
        fetchProp();

        // 判断是否到路径点
        for (int flagIndex = 0; flagIndex < flagList.length; flagIndex++) {
            for (User user: getUserMap().values()) {
                MarathonUser mUser = (MarathonUser) user;
                if (flagIndex == mUser.pointIndex) {
                    boolean isNear = isNear(flagList[flagIndex], user.getPosition().positionPoint, FETCH_DISTANCE);
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
}

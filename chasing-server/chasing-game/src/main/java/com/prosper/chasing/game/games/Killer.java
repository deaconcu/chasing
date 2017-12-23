package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.MetaGameAnno;
import com.prosper.chasing.game.base.Position;
import com.prosper.chasing.game.base.User;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@MetaGameAnno("killer")
public class Killer extends Game {

    // 一开始收集道具的时间
    private static final int waitTime = 300;

    public static class KillerUser extends User {
        // 坚持的时间
        private int endTime;

        // 生命值
        private int life;

        public KillerUser(User user) {
            setId(user.getId());
            setPropMap(user.getPropMap());
            setGame(user.getGame());
            setState(user.getState());
        }
    }

    @Override
    public void loadUser(List<User> userList) {
        for (User user: userList) {
            KillerUser KillerUser = new KillerUser(user);
            Position position = new Position(
                    (byte)1, new PositionPoint(0, 0, 0), 0);
            KillerUser.setPosition(position);
            KillerUser.setInitPosition(position);
            getUserMap().put(KillerUser.getId(), KillerUser);
        }
    }

    @Override
    public void logic() {
        removeInvalidProp();
        fetchProp();

        // 判断是否生命已结束
        for (User user: getUserMap().values()) {
            KillerUser mUser = (KillerUser) user;
            if (mUser.life == 0) {
                mUser.endTime = (int) System.currentTimeMillis() / 1000;
            }
        }

        generateProp();
    }

    @Override
    public List<Result> getResultList() {
        List<Result> resultList = new LinkedList<>();
        for(User user: getUserMap().values()) {
            KillerUser mUser = (KillerUser) user;
            int cost = (int) (mUser.endTime - startTime / 1000);
            resultList.add(new Result(mUser, cost, 0));
        }
        Collections.sort(resultList);
        return resultList;
    }
}

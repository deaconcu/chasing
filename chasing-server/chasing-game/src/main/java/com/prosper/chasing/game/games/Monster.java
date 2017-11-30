package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.MetaGameAnno;
import com.prosper.chasing.game.base.Position;
import com.prosper.chasing.game.base.User;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@MetaGameAnno("monster")
public class Monster extends Game {

    // 找变成妖怪的道具时间，超过这个时间就会随机找一个人成为妖怪
    private static final int frozenTime = 300;
    private static final int fragmentTotal = 10;

    // 已找到的碎片数
    private int fragmentCount = 0;

    private MonsterUser choosenOne;

    public static class MonsterUser extends User {
        // 坚持的时间
        private int endTime;

        private int type = 0; // 0 为平民， 1为妖怪

        private int life = 1;

        private int getFragmentCount = 0;

        public MonsterUser(User user) {
            setId(user.getId());
            setPropMap(user.getPropMap());
            setGame(user.getGame());
            setState(user.getState());
        }
    }

    @Override
    public void loadUser(List<User> userList) {
        for (User user: userList) {
            MonsterUser monsterUser = new MonsterUser(user);
            Position position = new Position(
                    (byte)1, new PositionPoint(0, 0, 0), 0);
            monsterUser.setPosition(position);
            monsterUser.setInitPosition(position);
            getUserMap().put(monsterUser.getId(), monsterUser);
        }
    }

    @Override
    public void logic() {
        removeInvalidProp();
        fetchProp();

        // 超过设置时间，随机指定一个人为monster
        if ((System.currentTimeMillis() / 1000 - startTime) > frozenTime && choosenOne == null) {
            MonsterUser user = (MonsterUser)getUserMap().values().toArray()[getRandom().nextInt(getUserMap().size())];
            user.type = 1;
            choosenOne = user;
        }

        // 判断是否生命已结束
        if (choosenOne != null) {
            for (User user: getUserMap().values()) {
                MonsterUser monsterUser = (MonsterUser)user;
                if (monsterUser.type == 1) {
                    continue;
                }
                int distance = getDistance(
                        choosenOne.getPosition().positionPoint, monsterUser.getPosition().positionPoint);
                if (distance < FETCH_DISTANCE) {
                    monsterUser.life = 0;
                    monsterUser.endTime = (int) System.currentTimeMillis() / 1000;
                }
            }
        }

        if (fragmentCount >= fragmentTotal) {
            choosenOne.life = 0;
        }

        generatProp();
    }

    @Override
    public List<Result> getResultList() {
        List<Result> resultList = new LinkedList<>();
        for(User user: getUserMap().values()) {
            MonsterUser mUser = (MonsterUser) user;
            if (mUser.type == 1) {
                continue;
            }
            resultList.add(new Result(mUser, mUser.getFragmentCount));
        }
        Collections.sort(resultList);
        if (choosenOne.life == 1) {
            resultList.add(0, new Result(choosenOne, choosenOne.getFragmentCount));
        } else {
            resultList.add(new Result(choosenOne, choosenOne.getFragmentCount));
        }
        return resultList;
    }
}

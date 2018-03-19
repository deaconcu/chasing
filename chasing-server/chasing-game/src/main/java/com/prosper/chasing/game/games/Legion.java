package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.MetaGameAnno;
import com.prosper.chasing.game.base.Position;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.navmesh.Point;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@MetaGameAnno("legion")
public class Legion extends Game {

    // 每轮时间
    private static final int roundTime = 300;

    private int currentRoundStartTime = (int) System.currentTimeMillis() / 1000;

    private int currentLegionTeam = 1;

    public static class LegionUser extends User {
        // 生命值
        private int life;

        // 队伍id
        private int team;

        private int kill;

        public LegionUser(User user) {
            setId(user.getId());
            setPropMap(user.getPropMap());
            setGame(user.getGame());
            setState(user.getState());
        }
    }

    public void loadUserOld(List<User> userList) {
        Collections.shuffle(userList);
        for (int i = 0; i < userList.size(); i++) {
            LegionUser legionUser = new LegionUser(userList.get(i));
            Position position = new Position(
                    (byte)1, new Point(0, 0, 0), 0);
            legionUser.setPosition(position);
            legionUser.setInitPosition(position);
            if (i < (userList.size() / 2)) {
                legionUser.team = 1;
            } else {
                legionUser.team = 2;
            }
            getUserMap().put(legionUser.getId(), legionUser);
        }
    }

    @Override
    public void logic() {
        super.logic();
        /*
        if ((System.currentTimeMillis() / 1000 - currentRoundStartTime) > roundTime) {
            if (currentLegionTeam == 1) {
                currentLegionTeam = 2;
            } else {
                currentLegionTeam = 1;
            }
            currentRoundStartTime = (int) System.currentTimeMillis() / 1000;
        }
        */

    }

    public List<Result> getResultList() {
        List<Result> team1ResultList = new LinkedList<>();
        int team1Remain = 0;
        List<Result> team2ResultList = new LinkedList<>();
        int team2Remain = 1;
        for(User user: getUserMap().values()) {
            LegionUser mUser = (LegionUser) user;
            if (mUser.team == 1) {
                team1ResultList.add(new Result(mUser, mUser.kill, 0));
                if (mUser.life > 0) {
                    team1Remain ++;
                }
            } else {
                team2ResultList.add(new Result(mUser, mUser.kill, 0));
                if (mUser.life > 0) {
                    team2Remain ++;
                }
            }
        }

        Collections.sort(team1ResultList);
        Collections.sort(team2ResultList);
        List<Result> resultList = new LinkedList<>();
        if (team1Remain > team2Remain) {
            resultList.addAll(team1ResultList);
            resultList.addAll(team2ResultList);
        } else {
            resultList.addAll(team2ResultList);
            resultList.addAll(team1ResultList);
        }
        return resultList;
    }
}

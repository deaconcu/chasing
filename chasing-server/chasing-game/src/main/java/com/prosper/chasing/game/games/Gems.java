package com.prosper.chasing.game.games;

import com.prosper.chasing.game.base.Position;

import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.MetaGameAnno;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.service.PropService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@MetaGameAnno("gems")
public class Gems extends Game {

    private static final int POSITION_X_LIMIT = 10;
    private static final int POSITION_Z_LIMIT = 10;

    public static class GemsUser extends User {
        public GemsUser(User user) {
            setId(user.getId());
            setName(user.getName());
            setPropMap(user.getPropMap());
            setGame(user.getGame());
            setState(user.getState());
        }
    }

    @Override
    public Class<? extends User> getUserClass() {
        return GemsUser.class;
    }

    @Override
    public void loadUser(List<User> userList) {
        for (User user: userList) {
            GemsUser gemsUser = new GemsUser(user);
            int positionX = getRandom().nextInt(POSITION_X_LIMIT * 2 + 1) - POSITION_X_LIMIT;
            int positionZ = getRandom().nextInt(POSITION_Z_LIMIT * 2 + 1) - POSITION_Z_LIMIT;
            Position position = new Position((byte)1, new PositionPoint(positionX, 0, positionZ), 0);
            gemsUser.setPosition(position);
            gemsUser.setInitPosition(position);
            getUserMap().put(gemsUser.getId(), gemsUser);
        }
    }

    @Override
    public void logic() {
        removeInvalidProp();
        fetchProp();
        generateProp();
    }

    @Override
    public List<Result> getResultList() {
        List<Result> resultList = new LinkedList<>();
        for(User user: getUserMap().values()) {
            resultList.add(new Result(user, user.getProp(PropService.GEN), 0));
        }
        Collections.sort(resultList);
        return resultList;
    }
}

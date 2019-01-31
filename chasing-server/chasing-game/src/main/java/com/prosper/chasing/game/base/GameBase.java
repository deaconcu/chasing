package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.ByteBuilder;

import java.util.List;
import java.util.Map;

/**
 * Created by deacon on 2018/9/25.
 */
public class GameBase extends Game {

    @Override
    protected void customInit() {

    }

    @Override
    protected ByteBuilder createIntroductionMessage() {
        return null;
    }

    @Override
    protected void initStationary() {

    }

    @Override
    protected void customLogic() {

    }

    @Override
    protected void doPositionChanged(User user) {

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

    @Override
    protected List<NPC> generateNPC() {
        return null;
    }

    @Override
    protected void customInitUser(Map<Integer, User> userMap) {

    }
}

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
    public Class<? extends User> getUserClass() {
        return User.class;
    }

    @Override
    public GameConfig getGameConfig() {
        return null;
    }

    @Override
    protected ByteBuilder createIntroductionMessage() {
        return null;
    }

    @Override
    protected void initGameObject() {

    }

    @Override
    protected void doCustomUserLogic(User user) {

    }

    @Override
    protected void customLogic() {

    }

    @Override
    protected void doPositionChanged(User user) {

    }

    @Override
    protected void customInitUser(Map<Integer, User> userMap) {

    }
}

package com.prosper.chasing.game.prop;

import java.util.Map;

import com.prosper.chasing.game.base.User;

public class BloodPill extends PropService {

    @Override
    public void use(User user, User toUser, Map<Integer, User> userMap) {
        user.getBuffMap().put((byte)1, new User.Buff());
    }
    
}

package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2018/12/31.
 */
public class Action {

    public static class PropAction extends Action {

        public PropType propType;
        public int userId;
        public TargetType targetType;
        public int targetId;

        public PropAction(PropType propType, int userId, TargetType targetType, int targetId) {
            this.propType = propType;
            this.userId = userId;
            this.targetType = targetType;
            this.targetId = targetId;
        }
    }
}

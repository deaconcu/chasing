package com.prosper.chasing.game.base;

import com.prosper.chasing.game.util.Enums;

/**
 * Created by deacon on 2018/12/31.
 */
public class Action {

    public static class PropAction extends Action {

        public short propTypeId;
        public int userId;
        public Enums.TargetType targetType;
        public int targetId;

        public PropAction(short propTypeId, int userId, Enums.TargetType targetType, int targetId) {
            this.propTypeId = propTypeId;
            this.userId = userId;
            this.targetType = targetType;
            this.targetId = targetId;
        }
    }
}

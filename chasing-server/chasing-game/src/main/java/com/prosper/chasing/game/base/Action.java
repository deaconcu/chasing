package com.prosper.chasing.game.base;

/**
 * Created by deacon on 2018/12/31.
 */
public class Action {

    public static class PropAction extends Action {

        public short propTypeId;
        public int userId;
        public byte targetType;
        public int targetId;

        public PropAction(short propTypeId, int userId, byte targetType, int targetId) {
            this.propTypeId = propTypeId;
            this.userId = userId;
            this.targetType = targetType;
            this.targetId = targetId;
        }
    }
}

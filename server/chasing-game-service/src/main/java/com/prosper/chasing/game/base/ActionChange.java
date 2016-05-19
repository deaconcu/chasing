package com.prosper.chasing.game.base;

import java.util.List;
import java.util.Map;

public class ActionChange {

    /**
     * 动作发起人的user id
     */
    private int userId;

    /**
     * 发起的动作
     */
    private Action action;
    
    /**
     * 造成的变化
     */
    private Map<Integer, List<FieldChange>> changeMap;
    
    public static int getActionType(Action action) {
        if (action instanceof PropAction) {
            return 1;
        } else if (action instanceof SkillAction) {
            return 2;
        } else {
            throw new RuntimeException("unknown action");
        }
    }
    
    public static int booleanToInt(Boolean b) {
        if (b) {
            return 1;
        } else {
            return 0;
        }
    }
    
    
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Map<Integer, List<FieldChange>> getChangeMap() {
        return changeMap;
    }

    public void setChangeMap(Map<Integer, List<FieldChange>> changeMap) {
        this.changeMap = changeMap;
    }

    public static class Action {
        public int opCode;
    }
    
    public static class PropAction extends Action {
        public int propId;
    }
    
    public static class SkillAction extends Action {
        public int skillId;
    }
    
    public static class FieldChange {
    }
    
    public static class PositionChange extends FieldChange {
        public int name;
        public int value;
        public PositionChange(int name, int value) {
            this.name = name;
            this.value = value;
        }
    }
    
    public static class StateChange extends FieldChange {
        public int stateId;
        public int action;
        public StateChange(int action, int stateId) {
            this.action = action;
            this.stateId = stateId;
        }
    }
}

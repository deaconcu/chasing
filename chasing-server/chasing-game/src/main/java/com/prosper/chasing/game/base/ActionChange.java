package com.prosper.chasing.game.base;

import java.util.HashMap;
import java.util.LinkedList;
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
    
    public ActionChange() {
        changeMap = new HashMap<>();
    }
    
    public void putChange(Integer userId, FieldChange fieldChange) {
        List<FieldChange> changeList = changeMap.get(userId);
        if (changeList == null) {
            changeList = new LinkedList<FieldChange>();
            changeMap.put(userId, changeList);
        }
        changeList.add(fieldChange);
    }
    
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
    
    /**
     * 位置变化
     */
    public static class PositionChange extends FieldChange {
        public int name;
        public int value;
        public PositionChange() {
        }
        public PositionChange(int name, int value) {
            this.name = name;
            this.value = value;
        }
    }
    
    /**
     * Buff变化
     */
    public static class BuffChange extends FieldChange {
        public int buffId;
        public int action;
        public BuffChange(int action, int buffId) {
            this.action = action;
            this.buffId = buffId;
        }
    }
    
    /**
     * 状态变化
     */
    public static class StateChange extends FieldChange {
        public int sourceState;
        public int targetState;
        public StateChange(int sourceState, int targetState) {
            this.sourceState = sourceState;
            this.targetState = targetState;
        }
    }
}

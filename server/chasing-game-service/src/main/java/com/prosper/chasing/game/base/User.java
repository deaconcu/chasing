package com.prosper.chasing.game.base;

import java.util.Map;

public class User {
    
    /**
     * 用户id
     */
    private int id;
    
    /**
     * 用户位置
     */
    private Position position;
    
    /**
     * 所拥有的道具
     */
    private Map<Integer, Prop> propMap;
    
    /**
     * 使用过的道具
     */
    private Map<Integer, Prop> usedPropMap;
    
    /**
     * 状态
     */
    private Map<Integer, Integer> stateMap; 
    
    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }
    
    /**
     * 检查道具是否满足要求的数量
     */
    public boolean checkProp(int propId, int count) {
        Prop prop = propMap.get(propId);
        if (prop == null) {
            return false;
        }
        if (prop.getCount() < count) {
            return false;
        }
        return true;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<Integer, Prop> getPropMap() {
        return propMap;
    }

    public void setPropMap(Map<Integer, Prop> propMap) {
        this.propMap = propMap;
    }

    public Map<Integer, Prop> getUsedPropMap() {
        return usedPropMap;
    }

    public void setUsedPropMap(Map<Integer, Prop> usedPropMap) {
        this.usedPropMap = usedPropMap;
    }

    public Map<Integer, Integer> getStateMap() {
        return stateMap;
    }

    public void setStateMap(Map<Integer, Integer> stateMap) {
        this.stateMap = stateMap;
    }

}

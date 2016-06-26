package com.prosper.chasing.game.base;

import java.util.Map;

public class User {
    
    public static class UserState {
        public static int ACTIVE = 1;   // 活动状态
        public static int QUITING = 2;  // 正在退出
        public static int QUIT = 3;     // 已退出
    }
    
    /**
     * 用户id
     */
    private int id;
    
    /**
     * game id
     */
    private int gameId;
    
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
     * Buff Map
     */
    private Map<Integer, Integer> buffMap;
    
    /**
     * 用户状态 @see #User.UserState
     */
    private int state;
    
    /**
     * 使用状态是否
     */
    private int dataChanged;
    
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

    public Map<Integer, Integer> getBuffMap() {
        return buffMap;
    }

    public void setBuffMap(Map<Integer, Integer> buffMap) {
        this.buffMap = buffMap;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

}

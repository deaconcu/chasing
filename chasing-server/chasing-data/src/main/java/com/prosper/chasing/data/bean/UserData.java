package com.prosper.chasing.data.bean;

public class UserData {
    
    private int id;
    private int distance;
    private byte roleType;
    private int hill;
    private int river;
    private String createTime;
    private String updateTime;
    private Short state;
    private int gameId;
    private int steps;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getDistance() {
        return distance;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }
    public int getHill() {
        return hill;
    }
    public void setHill(int hill) {
        this.hill = hill;
    }
    public int getRiver() {
        return river;
    }
    public void setRiver(int river) {
        this.river = river;
    }
    public String getCreateTime() {
        return createTime;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public String getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Short getState() {
        return state;
    }

    public void setState(Short state) {
        this.state = state;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public byte getRoleType() {
        return roleType;
    }

    public void setRoleType(byte roleType) {
        this.roleType = roleType;
    }
}

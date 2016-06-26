package com.prosper.chasing.data.bean;

public class UserData {
    
    private int id;
    private int distance;
    private int road;
    private int hill;
    private int river;
    private String createTime;
    private String updateTime;
    
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
    public int getRoad() {
        return road;
    }
    public void setRoad(int road) {
        this.road = road;
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
    

}

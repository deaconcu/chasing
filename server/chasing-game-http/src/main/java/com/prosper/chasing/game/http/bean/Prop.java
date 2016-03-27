package com.prosper.chasing.game.http.bean;

public class Prop {
    
    private int id;
    private String name;
    private String createTime;
    private String updateTime;
    private short state;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public short getState() {
        return state;
    }
    public void setState(short state) {
        this.state = state;
    }

}

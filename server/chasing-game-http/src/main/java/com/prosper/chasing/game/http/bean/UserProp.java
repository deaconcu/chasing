package com.prosper.chasing.game.http.bean;

public class UserProp {
    
    private int id;
    private int UserId;
    private int propId;
    private int count;
    private short action;
    private String createTime;
    private String updateTime;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getUserId() {
        return UserId;
    }
    public void setUserId(int userId) {
        UserId = userId;
    }
    public int getPropId() {
        return propId;
    }
    public void setPropId(int propId) {
        this.propId = propId;
    }
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
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
    public short getAction() {
        return action;
    }
    public void setAction(short action) {
        this.action = action;
    }

}

package com.prosper.chasing.data.bean;

public class UserProp {
    
    private int id;
    private int userId;
    private String propCode;
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
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
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
    public String getPropCode() {
        return propCode;
    }
    public void setPropCode(String propCode) {
        this.propCode = propCode;
    }

}

package com.prosper.chasing.game.http.bean;

public class Friend {

    private int id;
    private int userId;
    private int friendUserId;
    private int state;
    private String createTime;
    
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
    public int getFriendUserId() {
        return friendUserId;
    }
    public void setFriendUserId(int friendUserId) {
        this.friendUserId = friendUserId;
    }
    public String getCreateTime() {
        return createTime;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
}

package com.prosper.chasing.game.base;

/*
 * 游戏的元信息
 */
public class GameInfo {

    private Integer id;
    private Integer metagameId;
    private int duration;
    private int state;
    private int creatorId;
    private String server;
    private String startTime;
    private String createTime;
    private String updateTime;
    
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
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
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public int getCreatorId() {
        return creatorId;
    }
    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }
    public Integer getMetagameId() {
        return metagameId;
    }
    public void setMetagameId(Integer metagameId) {
        this.metagameId = metagameId;
    }
    
    
}

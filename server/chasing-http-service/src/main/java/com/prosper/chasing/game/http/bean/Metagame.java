package com.prosper.chasing.game.http.bean;

import java.util.List;

public class Metagame {

    private Integer id;
    private String code;
    private String name;
    private Integer duration;
    private Integer state;
    private List<MetagameType> metagameTypeList;
    private String types;
    private String createTime;
    private String updateTime;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
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
    public List<MetagameType> getMetagameTypeList() {
        return metagameTypeList;
    }
    public void setMetagameTypeList(List<MetagameType> metagameTypeList) {
        this.metagameTypeList = metagameTypeList;
    }
    public String getTypes() {
        return types;
    }
    public void setTypes(String types) {
        this.types = types;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    
    
}

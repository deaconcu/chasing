package com.prosper.chasing.data.bean;

public class Prop {
    
    private int id;
    private String code;
    private String name;
    private int count;
    private int price;
    private short unit;
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
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
    }
    public short getUnit() {
        return unit;
    }
    public void setUnit(short unit) {
        this.unit = unit;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

}

package com.prosper.chasing.tool.bean;

public class ActionData {

    private float x;
    
    private float y;

    private float z;
    
    private long createTime;
    
    public ActionData() {
    }
    
    public ActionData(float x, float y, float z, long createTime) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.createTime = createTime;
    }
    
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    
    public String toString() {
        return "x:" + x + " y:" + y + " z:" + z + " ct:" + createTime;
    }
}

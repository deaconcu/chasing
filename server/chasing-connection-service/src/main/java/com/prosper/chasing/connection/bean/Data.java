package com.prosper.chasing.connection.bean;

public class Data {
    
    private Long userId;
    
    private byte[] bytes;
    
    public Data() {
        
    }
    
    public Data(byte[] bytes) {
        this();
        deserialize(bytes);
    }
    
    public Data(String s) {
        this.userId = 10001L;
    }
    
    public void deserialize(byte[] bytes) {
        // TODO
    }
    
    public byte[] serialize() {
        // TODO
        return null;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

}

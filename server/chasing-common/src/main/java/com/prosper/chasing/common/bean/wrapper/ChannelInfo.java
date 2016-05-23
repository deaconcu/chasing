package com.prosper.chasing.common.bean.wrapper;

import java.util.HashMap;
import java.util.Map;

public class ChannelInfo {
    
    private Integer key;
    
    private Map<String, Object> customValues;
    
    public ChannelInfo() {
        customValues = new HashMap<>();
    }
    
    public void putCustomValue(String key, Object value) {
        customValues.put(key, value);
    }

    public Map<String, Object> getCustomValues() {
        return customValues;
    }

    public void setCustomValues(Map<String, Object> customValues) {
        this.customValues = customValues;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }
    
}

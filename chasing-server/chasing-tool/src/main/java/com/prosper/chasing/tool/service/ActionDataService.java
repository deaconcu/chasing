package com.prosper.chasing.tool.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.prosper.chasing.tool.bean.ActionData;

@Component
public class ActionDataService {
    
    ConcurrentHashMap<Long, ActionData> actionDataMap = new ConcurrentHashMap<>();
    TreeSet<Long> actionDataTimeSet = new TreeSet<>();
    
    @PostConstruct
    public void init() {
        int i = 1;
        while (i <= 10000) {
            addActionData(new ActionData(i, i, i, i ++));
        }
    }

    public List<Long> getActionDataTimes(int count){
        List<Long> xList = new ArrayList<>();
        Long time = actionDataTimeSet.last();
        while (count -- > 0 && time != null) {
            xList.add(time);
            time = actionDataTimeSet.lower(time);
        }
        Collections.reverse(xList);
        return xList;
    }
    
    public List<Float> getActionDataX(int count){
        List<Float> xList = new ArrayList<>();
        Long time = actionDataTimeSet.last();
        while (count -- > 0 && time != null) {
            xList.add(actionDataMap.get(time).getX());
            time = actionDataTimeSet.lower(time);
        }
        Collections.reverse(xList);
        return xList;
    }
    
    public List<Float> getActionDataY(int count){
        List<Float> yList = new ArrayList<>();
        Long time = actionDataTimeSet.last();
        while (count -- > 0 && time != null) {
            yList.add(actionDataMap.get(time).getY());
            time = actionDataTimeSet.lower(time);
        }
        Collections.reverse(yList);
        return yList;
    }
    
    public List<Float> getActionDataZ(int count){
        List<Float> zList = new ArrayList<>();
        Long time = actionDataTimeSet.last();
        while (count -- > 0 && time != null) {
            zList.add(actionDataMap.get(time).getZ());
            time = actionDataTimeSet.lower(time);
        }
        Collections.reverse(zList);
        return zList;
    }
    
    public List<Float> getActionDataA(int count){
        List<Float> zList = new ArrayList<>();
        Long time = actionDataTimeSet.last();
        while (count -- > 0 && time != null) {
            float x = actionDataMap.get(time).getX();
            float y = actionDataMap.get(time).getY();
            float z = actionDataMap.get(time).getZ();
            
            float a = (float)Math.sqrt(x * x + y * y + z * z) - 1;
            
            zList.add(a);
            time = actionDataTimeSet.lower(time);
        }
        Collections.reverse(zList);
        return zList;
    }
    
    public void addActionData(ActionData actionData){
        if (actionDataMap.size() > 1000000) {
            actionDataMap.clear();
        }
        if (actionData != null) {
            actionDataMap.put(actionData.getCreateTime(), actionData);
            actionDataTimeSet.add(actionData.getCreateTime());
        }
    }
    
}

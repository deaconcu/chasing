package com.prosper.chasing.connection.game;

import java.util.HashMap;
import java.util.Map;

import com.prosper.chasing.connection.bean.Data;

public class MessageManager {
    
    private int count = 10;
    
    private static MessageManager mm;
    
    private Map<Integer, MessageHandler> handlerMap = new HashMap<>();
    
    public static void init(int count, int capacity) {
        mm = new MessageManager(count, capacity);
    }
    
    public static MessageManager instance() {
        return mm;
    }
    
    public MessageManager(int count, int capacity) {
        this.count = count;
        
        for (int i = 0; i <= count; i++) {
            MessageHandler mh = new MessageHandler(capacity);
            handlerMap.put(i, mh);
            mh.start();
        }
    }
    
    public void add(Data data) {
        Integer n = (int) (data.getUserId() % count);
        MessageHandler mh = handlerMap.get(n);
        mh.offer(data);
    }
    
    public void pull() {
        
    }
    
}
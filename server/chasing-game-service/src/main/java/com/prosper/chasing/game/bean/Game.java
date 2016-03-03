package com.prosper.chasing.game.bean;

import java.util.HashMap;
import java.util.Map;

public class Game {

    private String id;
    
    private Map<Long, User> userMap = new HashMap<>();
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addUser(User user) {
        userMap.put(user.getId(), user);
    }
    
    public User getUser(long userId) {
        return userMap.get(userId);
    }

    public void onChange(User user) {
        // add user info to message queue
    }
    
}

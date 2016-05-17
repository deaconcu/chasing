package com.prosper.chasing.game.prop;

import java.util.Map;

import com.prosper.chasing.game.base.User;

public class BaseProp {

    public static int BLOOD_PILL = 1;
    
    public static BaseProp getProp(int id) {
        if (id == BLOOD_PILL) {
            return new BloodPill();
        } else {
            return null;
        }
    }
    
    public void use(Map<Integer, User> userMap) {
        
    }
    
}

package com.prosper.chasing.game.prop;

import java.util.Map;

import com.prosper.chasing.game.base.ActionChange;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.base.ActionChange.PropAction;

public class BaseProp {

    public static int BLOOD_PILL = 1;
    
    public static BaseProp getProp(int id) {
        if (id == BLOOD_PILL) {
            return new BloodPill();
        } else {
            return null;
        }
    }
    
    /**
     * 使用道具
     * @param user 使用者 
     * @param toUser 被使用者
     * @param userMap 用户列表
     * @param syncMessage 同步消息
     */
    public void use(User user, User toUser, Map<Integer, User> userMap, ActionChange syncMessage) {
    }

    /**
     * 检查道具是否可用
     * @param action 
     * @return 
     */
    public boolean testUse(User user, User toUser, Map<Integer, User> userMap, PropAction action) {
        return false;
    }
    
}

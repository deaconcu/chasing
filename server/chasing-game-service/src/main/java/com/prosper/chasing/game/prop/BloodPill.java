package com.prosper.chasing.game.prop;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.prosper.chasing.game.base.ActionChange;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.base.ActionChange.*;

public class BloodPill extends BaseProp {

    @Override
    public void use(User user, User toUser, Map<Integer, User> userMap, ActionChange syncMessage) {
        user.getBuffMap().put(1, 1);
        List<FieldChange> fieldChangeList = new LinkedList<FieldChange>();
        fieldChangeList.add(new BuffChange(1, 1));
        syncMessage.getChangeMap().put(toUser.getId(), fieldChangeList);
    }
    
    @Override
    public boolean testUse(User user, User toUser, Map<Integer, User> userMap, PropAction action) {
        return false;
    }
    
}

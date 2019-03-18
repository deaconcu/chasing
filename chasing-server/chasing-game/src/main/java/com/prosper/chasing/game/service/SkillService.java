package com.prosper.chasing.game.service;

import com.prosper.chasing.game.base.Buff.*;
import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.message.PropMessage;
import com.prosper.chasing.game.message.SkillMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by deacon on 2017/12/31.
 */
public class SkillService {

    private Logger log = LoggerFactory.getLogger(getClass());

    public static final byte CHASING = 1;

    public static Map<Short, byte[]> typeMap = new HashMap<>();

    static {
        typeMap.put((short) 1, new byte[]{SkillMessage.TYPE_NPC});
    }

    private boolean checkType (short skillId, byte messageType) {
        byte[] type = typeMap.get(skillId);
        for (byte i : type) {
            if (i == messageType) {
                return true;
            }
        }
        return false;
    }

    public void use(short skillId, SkillMessage message, User user, User toUser, Map<Integer, User> userMap) {
        if (!checkType(skillId, message.getType())) {
            log.warn("prop type is not right, prop objectId: {}, message type: {}", skillId, message.getType());
            return;
        }

        /*
        if (skillId == CHASING) {
            if (message.getType() == SkillMessage.TYPE_STATIONARY) {
                user.setBuff(new ChasingBuff(
                        BuffService.CHASING, (short)3000, ChasingBuff.NPCOld, message.getToNPCSeqId()));
            }
        }
        */
    }
}

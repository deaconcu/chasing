package com.prosper.chasing.game.message;

import com.prosper.chasing.game.util.Constant;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by deacon on 2017/3/13.
 */
public class EchoMessage extends UserMessage {

    public byte retryType;
    public List<Integer> missingMessageSeqIdList;

    public EchoMessage(UserMessage message) {
        super(message);
        retryType = message.getContent().get(0);

        if (retryType == Constant.MessageRetryType.SINGLE) {
            missingMessageSeqIdList = new LinkedList<>();
            int count = message.getContent().getInt();
            for (int i = 0; i < count; i ++) {
                missingMessageSeqIdList.add(message.getContent().getInt());
            }
        }
    }

}

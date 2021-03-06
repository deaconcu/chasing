package com.prosper.chasing.game.thrift;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.wrapper.ThriftRPCService;
import com.prosper.chasing.common.interfaces.game.GameException;
import com.prosper.chasing.common.interfaces.game.GameService;
import com.prosper.chasing.game.base.GameManage;
import com.prosper.chasing.game.message.UserMessage;

@Component
@Lazy
@ThriftRPCService(processorClass = GameService.Processor.class)
public class GameServiceImpl implements GameService.Iface {

    private static Logger log = LoggerFactory.getLogger(GameServiceImpl.class);
    
    @Autowired
    private GameManage gameManage;

    @Override
    public void executeData(int gameId, int userId, ByteBuffer data)
            throws GameException, TException {
        log.info("receive data:" + data);
        UserMessage userMessage = new UserMessage(gameId, userId, data);
        gameManage.recieveData(userMessage);
    }

}

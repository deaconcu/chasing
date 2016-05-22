package com.prosper.chasing.game.thrift;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.boot.RPCService;
import com.prosper.chasing.common.interfaces.game.GameException;
import com.prosper.chasing.common.interfaces.game.GameService;
import com.prosper.chasing.game.base.GameManage;

@Component
@RPCService(processorClass = GameService.Processor.class)
public class GameServiceImpl implements GameService.Iface {
    
    @Autowired
    private GameManage gameManage;

    @Override
    public void executeData(int gameId, int userId, ByteBuffer message)
            throws GameException, TException {
        gameManage.recieveData(gameId, userId, message);
    }

}

package com.prosper.chasing.game.thrift;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.boot.RPCService;
import com.prosper.chasing.common.interfaces.game.GameException;
import com.prosper.chasing.common.interfaces.game.GameService;
import com.prosper.chasing.common.interfaces.game.Message;
import com.prosper.chasing.game.Service.GameProcessService;

@Component
@RPCService(processorClass = GameService.Processor.class)
public class GameServiceImpl implements GameService.Iface {
    
    @Autowired
    private GameProcessService gameProcessService;
    
    @Override
    public boolean createGame(ByteBuffer bs) throws GameException, TException {
        return gameProcessService.createGame(bs);
    }

    @Override
    public boolean closeGame(String gameId) throws GameException, TException {
        return gameProcessService.closeGame(gameId);
    }

    @Override
    public void sendData(Message message) throws GameException, TException {
        gameProcessService.sendData(message);
    }

}

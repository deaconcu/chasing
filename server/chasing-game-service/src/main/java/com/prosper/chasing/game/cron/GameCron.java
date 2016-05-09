package com.prosper.chasing.game.cron;

import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Autowired;

import com.prosper.chasing.common.client.ThriftClient;
import com.prosper.chasing.common.client.ZkClient;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.util.Constant;
import com.prosper.chasing.common.util.Constant.GameState;
import com.prosper.chasing.game.Service.GameProcessService;
import com.prosper.chasing.game.util.Config;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

public class GameCron {
    
    @Autowired
    ThriftClient thriftClient;
    @Autowired
    GameProcessService gameProcessService;
    
    public void createGame() throws TException {
        GameDataService.Client gameDataServiceClient = thriftClient.getGameDataServiceClient("GameDataServiceImpl");
        List<GameTr> gameTrList = gameDataServiceClient.getGames(GameState.CREATE, 1, 1);
        
        
//        gameProcessService.createGame(gameTrList.get(0));
        
    }

}

package com.prosper.chasing.game.cron;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.client.ThriftClient;
import com.prosper.chasing.common.client.ZkClient;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.util.CommonConstant;
import com.prosper.chasing.common.util.CommonConstant.GameState;
import com.prosper.chasing.game.GameManage;
import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.util.Config;
import com.prosper.chasing.game.util.Constant;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

@Component
public class GameCron {
    
    @Autowired
    ThriftClient thriftClient;
    @Autowired
    Game gameProcessService;
    @Autowired
    Config config;
    @Autowired
    ExecutorService executorService;
    @Autowired
    GameManage gameManage;
    
    @Scheduled(cron="${cron.create.game}")
    public void createGame() throws TException {
        GameDataService.Client gameDataServiceClient = thriftClient.getGameDataServiceClient(
                "GameDataServiceImpl", config.gameDataServerZKName);
        List<GameTr> gameTrList = gameDataServiceClient.ClaimGame(config.serverIp, config.serverPort, 100);
        
        CountDownLatch countDownLatch = new CountDownLatch(gameTrList.size());
        for (final GameTr gameTr: gameTrList) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    GameManage.createGame(gameTr);
                }
            });
        }
        
        
    }

}

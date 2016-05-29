package com.prosper.chasing.game.cron;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.client.ThriftClient;
import com.prosper.chasing.common.bean.client.ThriftClient.GameDataServiceClient;
import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.util.CommonConstant;
import com.prosper.chasing.common.util.CommonConstant.GameState;
import com.prosper.chasing.game.base.Game;
import com.prosper.chasing.game.base.GameManage;
import com.prosper.chasing.game.util.Config;
import com.prosper.chasing.game.util.Constant;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

@Component
public class GameCron {

    private Logger log = LoggerFactory.getLogger(getClass());

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
    public void createGame() {
        try {
            final GameDataServiceClient gameDataServiceClient = thriftClient.gameDataServiceClient();
            List<GameTr> gameTrList = gameDataServiceClient.ClaimGame(config.serverIp, config.serverPort, 100);

            final CountDownLatch countDownLatch = new CountDownLatch(gameTrList.size());
            for (final GameTr gameTr: gameTrList) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            gameManage.createGame(gameTr);
                            gameTr.setState((byte)GameState.PROCESSING);
                            gameDataServiceClient.updateGame(gameTr);
                        } catch (Exception e) {
                            log.error("create game failed", e);
                        }
                        countDownLatch.countDown();
                    }
                });
            }    
        } catch (Exception e) {
            log.error("create game failed", e);
        }
    }

}

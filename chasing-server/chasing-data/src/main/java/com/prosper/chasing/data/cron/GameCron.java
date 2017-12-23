package com.prosper.chasing.data.cron;

import com.prosper.chasing.data.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class GameCron {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    GameService gameService;

    @PostConstruct
    public void createGame() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        int count = gameService.createGameBySystem();
                        if (count == 0) {
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        log.error("create game failed", e);
                    }
                }
            }
        }).start();
    }

}

package com.prosper.chasing.data.cron;

import com.prosper.chasing.data.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GameCron {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    GameService gameService;

    @Scheduled(cron="${cron.create.game}")
    public void createGame() {
        try {
            gameService.createGameBySystem();
        } catch (Exception e) {
            log.error("create game failed", e);
        }
    }

}

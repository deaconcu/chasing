package com.prosper.chasing.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.prosper.chasing.common.interfaces.game.Message;
import com.prosper.chasing.common.message.PositionMessage;
import com.prosper.chasing.game.bean.Game;
import com.prosper.chasing.game.bean.User;

public class GameManage {

    private Map<String, Game> gameMap = new HashMap<>();
    
    public void createGame(String gameId, List<User> userList) {
        Game game = gameMap.get(gameId);
        if (game != null) {
            throw new RuntimeException("game is exist");
        }
        game = new Game();
        for (User user: userList) {
            game.addUser(user);
        }
        gameMap.put(game.getId(), game);
    }

    public void executeData(Message message) {
        if (message instanceof PositionMessage) {
            PositionMessage positionMessage = (PositionMessage) message;
            String gameId = positionMessage.getGameId();
            long userId = positionMessage.getUserId();
            
            Game game = gameMap.get(gameId);
            User user = game.getUser(userId);
            
            user.addY(positionMessage.getDistance());
        } else {
            return;
        }
    }
    
}

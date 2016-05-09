package com.prosper.chasing.data.thrift;

import java.util.List;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.boot.RPCService;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.interfaces.data.UserTr;
import com.prosper.chasing.data.bean.Game;
import com.prosper.chasing.data.bean.User;
import com.prosper.chasing.data.service.GameService;
import com.prosper.chasing.data.util.ViewTransformer;

@Component
@RPCService(processorClass = GameDataService.Processor.class)
public class GameDataServiceImpl implements GameDataService.Iface {
    
    private GameService gameService;

    @Override
    public List<GameTr> getGames(int state, int page, int pageLength)
            throws TException {
        List<Game> gameList = gameService.getGame(state, page, pageLength);
        return ViewTransformer.transferList(gameList);
    }

    @Override
    public void updateGame(GameTr gameTr) throws TException {
        Game game = ViewTransformer.transferObject(gameTr);
        gameService.updateGame(game);
    }

    @Override
    public List<UserTr> getGameUsers(int gameId) throws TException {
        List<User> userList = gameService.getGameUser(gameId);
        return ViewTransformer.transferList(userList);
    }

}

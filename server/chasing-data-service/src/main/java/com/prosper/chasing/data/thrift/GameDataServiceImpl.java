package com.prosper.chasing.data.thrift;

import java.util.List;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.wrapper.RPCService;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.interfaces.data.UserTr;
import com.prosper.chasing.common.util.ViewTransformer;
import com.prosper.chasing.data.bean.Game;
import com.prosper.chasing.data.bean.User;
import com.prosper.chasing.data.service.GameService;

@Component
@RPCService(processorClass = GameDataService.Processor.class)
public class GameDataServiceImpl implements GameDataService.Iface {
    
    @Autowired
    private GameService gameService;

    @Override
    public List<GameTr> ClaimGame(String ip, int port, int count)
            throws TException {
        return ViewTransformer.transferList(gameService.claimGame(ip, port, count), GameTr.class);
    }

    @Override
    public void updateGame(GameTr gameTr) throws TException {
        Game game = ViewTransformer.transferObject(gameTr, Game.class);
        gameService.updateGame(game);
    }

    @Override
    public List<UserTr> getGameUsers(int gameId) throws TException {
        List<User> userList = gameService.getGameUser(gameId);
        return ViewTransformer.transferList(userList, UserTr.class);
    }

   

}

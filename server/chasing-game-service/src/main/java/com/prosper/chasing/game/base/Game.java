package com.prosper.chasing.game.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.prosper.chasing.common.boot.RPCService;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.game.GameException;
import com.prosper.chasing.common.interfaces.game.GameService;
import com.prosper.chasing.common.interfaces.game.Message;
import com.prosper.chasing.game.GameManage;
import com.prosper.chasing.game.util.Constant.GameLoadingState;
import com.prosper.chasing.common.util.JsonUtil;

@Component
public abstract class Game {
    
    private int state;
    private GameInfo gameInfo;
    private Map<Integer, User> userList;
    
    private GameManage gameManage;
    private JsonUtil jsonUtil = new JsonUtil();
    
    public Game() {
        this.state = GameLoadingState.START;
    }
    
    /**
     * 抽象方法，处理进入的消息
     */
    public abstract void executeMessage();
    
    public abstract void onChange(User user);

    public boolean closeGame(String gameId) throws GameException, TException {
        // TODO Auto-generated method stub
        return false;
    }

    public void sendData(Message message) throws GameException, TException {
        gameManage.executeData(message);
    }

    public void setGameManage(GameManage gameManage) {
        this.gameManage = gameManage;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public Map<Integer, User> getUserList() {
        return userList;
    }

    public void setUserList(Map<Integer, User> userList) {
        this.userList = userList;
    }

    public int getState() {
        return state;
    }

}

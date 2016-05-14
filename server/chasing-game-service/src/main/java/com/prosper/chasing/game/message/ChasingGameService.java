package com.prosper.chasing.game.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.thrift.TException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.prosper.chasing.common.interfaces.game.GameException;
import com.prosper.chasing.common.interfaces.game.GameService;
import com.prosper.chasing.common.interfaces.game.Message;
import com.prosper.chasing.game.GameManage;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.common.util.JsonUtil;

public class ChasingGameService implements GameService.Iface {
    
    private GameManage gameManage;
    private JsonUtil jsonUtil = new JsonUtil();

    @Override
    public boolean createGame(ByteBuffer bs) throws GameException, TException {
        String dataString = new String(bs.array());
        JsonNode node;
        try {
            node = jsonUtil.getObjectMapper().readTree(dataString);
        } catch (Exception e) {
            throw new GameException();
        }
        String gameId = node.get("gameId").asText();
        
        List<User> userList = new LinkedList<>();
        JsonNode users = node.get("users");
        for (JsonNode userNode: users) {
            User user = new User();
            user.setId(userNode.get("id").asLong());
            userList.add(user);
        }
        gameManage.createGame(gameId, userList);
        return true;
    }

    @Override
    public boolean closeGame(String gameId) throws GameException, TException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void sendData(Message message) throws GameException, TException {
        gameManage.executeData(message);
    }

    public void setGameManage(GameManage gameManage) {
        this.gameManage = gameManage;
    }

}

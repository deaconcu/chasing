package com.prosper.chasing.connection;

import java.util.Map;

import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

import com.prosper.chasing.common.bean.client.ThriftClient;
import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.bean.wrapper.ChannelInfo;
import com.prosper.chasing.common.bean.wrapper.WebSocketService;
import com.prosper.chasing.common.interfaces.game.GameService;

@Component
public class GameWebSocketService implements WebSocketService {

    @Autowired
    private ThriftClient thriftClient;
    @Autowired
    private ZkClient zkClient;
    @Autowired
    private Jedis jedis;
    @Autowired
    private Config config;

    @Override
    public ChannelInfo executeHttpRequest(FullHttpRequest req) {
        // TODO check if session id exist and get game id and user id by session id
        HttpHeaders httpHeaders = req.headers();
        int userId = 0;
        int gameId = 0;
        try {
            String sessionId = httpHeaders.get("sessionId");
            String info = jedis.get(sessionId);

            String[] infos = info.split(",");
            userId = Integer.parseInt(infos[0]);
            gameId = Integer.parseInt(infos[1]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (userId == 0 || gameId == 0) {
            throw new RuntimeException("user id or game id is not init, user id:" + userId + " game id:" + gameId);
        }
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.putCustomValue("userId", userId);
        channelInfo.putCustomValue("gameId", gameId);
        channelInfo.setKey(userId);
        
        byte[] addrBytes = (config.serverIp + ":" + config.rpcPort).getBytes();
        zkClient.createNode(config.userZKName + "/" + Integer.toString(userId), addrBytes, CreateMode.EPHEMERAL, true);
        return channelInfo;
    }

    @Override
    public void executeData(ByteBuf in, Map<String, Object> customValueMap) {
        try {
            int userId = (Integer)customValueMap.get("userId");
            int gameId = (Integer)customValueMap.get("gameId");

            // get ip from zk by game id
            byte[] serverBytes = zkClient.get(config.gameZKName + "/" + gameId, true);
            if (serverBytes == null) {
                return;
            }
            String server = new String(serverBytes);
            String[] hostPort = server.split(":");
            String host = hostPort[0];
            int port = Integer.parseInt(hostPort[1]);

            thriftClient.gameServiceClient(host, port).executeData(gameId, userId, in.nioBuffer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

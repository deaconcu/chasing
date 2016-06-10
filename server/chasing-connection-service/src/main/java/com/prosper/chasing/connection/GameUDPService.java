package com.prosper.chasing.connection;

import java.nio.charset.Charset;
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
import com.prosper.chasing.common.bean.wrapper.UDPService;
import com.prosper.chasing.common.bean.wrapper.WebSocketService;
import com.prosper.chasing.common.interfaces.game.GameService;

@Component
public class GameUDPService implements UDPService {

    @Autowired
    private ThriftClient thriftClient;
    @Autowired
    private ZkClient zkClient;
    @Autowired
    private Jedis jedis;
    @Autowired
    private Config config;

    @Override
    public int executeData(ByteBuf in) {
        try {
            byte[] sessionIdBuffer = new byte[16]; 
            in.readBytes(sessionIdBuffer);
            String sessionId = new String(sessionIdBuffer);
            String info = jedis.get(sessionId);

            String[] infos = info.split(",");
            int userId = Integer.parseInt(infos[0]);
            int gameId = Integer.parseInt(infos[1]);

            // get ip from zk by game id
            byte[] serverBytes = zkClient.get(config.gameZKName + "/" + gameId, true);
            if (serverBytes == null) {
                return userId;
            }
            String server = new String(serverBytes);
            String[] hostPort = server.split(":");
            String host = hostPort[0];
            int port = Integer.parseInt(hostPort[1]);

            thriftClient.gameServiceClient(host, port).executeData(gameId, userId, in.nioBuffer());
            return userId;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}

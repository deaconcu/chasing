package com.prosper.chasing.connection;

import java.nio.ByteBuffer;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import io.netty.buffer.ByteBuf;

import com.prosper.chasing.common.bean.client.ThriftClient;
import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.bean.wrapper.UDPService;

@Component
public class GameUDPService implements UDPService {
    
    private Logger log = LoggerFactory.getLogger(getClass());

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

            int length = in.readableBytes();
            ByteBuffer buffer = ByteBuffer.allocate(length);
            in.readBytes(buffer);
            buffer.flip();
            
            byte[] addrBytes = (config.serverIp + ":" + config.rpcPort).getBytes();

            // 在zookeeper写入当前用户所在的连接服务器
            // TODO 需要考虑节点已存在的情况
            zkClient.createNode(
                    config.userZKName + "/" + Integer.toString(userId),
                    addrBytes, CreateMode.EPHEMERAL, true);
            
            thriftClient.gameServiceClient(host, port).executeData(gameId, userId, buffer);
            return userId;
        } catch (Exception e) {
            log.error("execute message failed", e);
            return 0;
        }
    }

}

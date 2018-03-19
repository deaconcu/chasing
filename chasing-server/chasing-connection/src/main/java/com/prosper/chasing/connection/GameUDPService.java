package com.prosper.chasing.connection;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.prosper.chasing.common.util.CommonConstant.*;
import org.apache.ibatis.annotations.Param;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.springframework.util.StopWatch;
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

    private Map<Integer, Map<String, Object>> userMap = new ConcurrentHashMap<>();

    /*
     * 处理用户数据
     * 当前用户数据格式：| userId(4) | sessionId(16) | time(8) | xxxbyte:gameInfo |
     */
    @Override
    public int executeData(ByteBuf in) {
        try {
            log.info("received user message");
            // 读userId
            int userId = in.readInt();

            // 读sessionId
            byte[] sessionIdBuffer = new byte[16];
            in.readBytes(sessionIdBuffer);
            String postSessionId = new String(sessionIdBuffer);

            in.readLong(); // skip time

            Map<String, Object> userInfo = userMap.get(userId);
            if (userInfo == null) {
                userInfo = new HashMap<>();
                userMap.put(userId, userInfo);
            }
            if (userInfo.get("sessionId") == null || !userInfo.get("sessionId").equals(postSessionId)) {
                // 获取userId对应的sessionId
                log.info("get session from redis");
                String sessionCacheName = CacheName.session + userId;
                String sessionId = jedis.get(sessionCacheName);

                if (postSessionId.equals(sessionId)) {
                    userInfo.put("sessionId", sessionId);
                } else {
                    return 0;
                }
                jedis.set("user-" + userId, config.serverIp + ":" + config.rpcPort);
            }

            in.markReaderIndex();
            in.readInt(); // skip seqId
            int messageType = in.readByte();
            if (messageType == 1) {
                // 获取用户参加的游戏id，没有游戏id返回
                log.info("aaaaa");
                int gameId = thriftClient.gameDataServiceClient().getUserGame(userId);
                log.info("bbbbb");
                if (gameId <= 0) {
                    return 0;
                }
                userInfo.put("gameId", gameId);

                // 通过游戏id获取对应的游戏服务器ip和port
                log.info("cccccc");
                byte[] serverBytes = zkClient.get(config.gameZKName + "/" + gameId, true);
                log.info("dddddd");
                if (serverBytes == null) {
                    return userId;
                }
                String server = new String(serverBytes);
                String[] hostPort = server.split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);
                userInfo.put("host", host);
                userInfo.put("port", port);

                // 读取剩下的数据到buffer

            } else {

            }
            in.resetReaderIndex();
            int length = in.readableBytes();
            log.info("eeeee");
            ByteBuffer buffer = ByteBuffer.allocate(length);
            log.info("fffff");
            in.readBytes(buffer);
            log.info("hhhhhh");
            buffer.flip();
            log.info("gggggg");

            /*
            // 在zookeeper写入当前用户所在的连接服务器
            // TODO 需要考虑节点已存在的情况
            byte[] addrBytes = (config.serverIp + ":" + config.rpcPort).getBytes();
            zkClient.createNode(
                    config.userZKName + "/" + Integer.toString(userId),
                    addrBytes, CreateMode.EPHEMERAL, true);

            */
            // 处理用户行为数据
            //synchronized (this) {
                log.info("execute user message: {}", Arrays.toString(buffer.array()));
                thriftClient.gameServiceClient((String) userInfo.get("host"), (Integer) userInfo.get("port")).
                        executeData((Integer) userInfo.get("gameId"), userId, buffer);
            //}

            return userId;
        } catch (Exception e) {
            log.error("execute message failed", e);
            return 0;
        }
    }
}

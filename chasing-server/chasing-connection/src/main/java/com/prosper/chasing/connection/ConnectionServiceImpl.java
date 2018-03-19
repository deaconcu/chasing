package com.prosper.chasing.connection;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.wrapper.NettyUDPServer;
import com.prosper.chasing.common.bean.wrapper.NettyWebSocketServer;
import com.prosper.chasing.common.bean.wrapper.ThriftRPCService;
import com.prosper.chasing.common.interfaces.connection.ConnectionService;

@Component
@ThriftRPCService(processorClass = ConnectionService.Processor.class)
public class ConnectionServiceImpl implements ConnectionService.Iface {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    NettyUDPServer server;

    /*
     * gameServer处理完之后给用户发送同步数据的接口
     */
    @Override
    public void executeData(int userId, ByteBuffer message) throws TException {
        try {
            // 把message弄成bytes
            int length = message.remaining();
            byte[] data = new byte[length];
            message.get(data);

            // 通过udpserver和userId发送bytes
            server.sendData(userId, data);
            //log.info("reply user message: {}:{}", userId, Arrays.toString(data));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

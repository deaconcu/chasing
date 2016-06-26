package com.prosper.chasing.connection;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.wrapper.NettyUDPServer;
import com.prosper.chasing.common.bean.wrapper.NettyWebSocketServer;
import com.prosper.chasing.common.bean.wrapper.ThriftRPCService;
import com.prosper.chasing.common.interfaces.connection.ConnectionService;

@Component
@ThriftRPCService(processorClass = ConnectionService.Processor.class)
public class ConnectionServiceImpl implements ConnectionService.Iface {
    
    @Autowired
    NettyUDPServer server;

    @Override
    public void executeData(int userId, ByteBuffer message) throws TException {
        int length = message.remaining();
        byte[] data = new byte[length];
        message.get(data);
        server.sendData(userId, data);
    }

}

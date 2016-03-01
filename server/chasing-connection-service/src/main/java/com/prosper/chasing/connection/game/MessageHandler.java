package com.prosper.chasing.connection.game;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.prosper.chasing.common.client.ZkClient;
import com.prosper.chasing.common.interfaces.GameMessageService;
import com.prosper.chasing.connection.bean.Data;

public class MessageHandler extends Thread {

    private BlockingQueue<Data> queue;

    public MessageHandler(int capacity) {
        this.queue = new LinkedBlockingQueue<Data>(capacity);
    }

    public boolean offer(Data data) {
        return queue.offer(data);
    }

    @Override
    public void run() {
        while(true) {
            try {
                ZkClient zkClient = ZkClient.instance();
                Data data = queue.take();

                long userId = data.getUserId();
                // get game id from zk by userid
                byte[] gameIdBytes = zkClient.get("/user-game/" + userId);
                if (gameIdBytes == null) continue;
                long gameId = Long.parseLong(new String(gameIdBytes));

                // get ip from zk by game id
                byte[] serverBytes = zkClient.get("/game-server/" + gameId);
                if (serverBytes == null) continue;
                String server = new String(serverBytes);
                String[] hostPort = server.split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);
                
                try {
                    TTransport transport;
                    transport = new TSocket(host, port);
                    transport.open();

                    TProtocol protocol = new  TBinaryProtocol(transport);
                    GameMessageService.Client client = new GameMessageService.Client(protocol);

                    client.send(ByteBuffer.wrap(data.getBytes()));
                    transport.close();
                } catch (TException x) {
                    x.printStackTrace();
                } 

                System.out.println("a");
                // rpc call from ip
            } catch (Exception e) {
                e.printStackTrace();
            }
        }            
    }

}

package com.prosper.chasing.game;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.prosper.chasing.common.interfaces.GameMessageService;

public class GameMessageServer {

    private void start(){
        try {
            TServerSocket serverTransport = new TServerSocket(8811);
            GameMessageService.Processor<GameMessageService.Iface> processor = 
                    new GameMessageService.Processor<GameMessageService.Iface>(new GameMessageHandler());
            
            TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
            System.out.println("Starting server on port 8811 ...");
            server.serve();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GameMessageServer srv = new GameMessageServer();
        srv.start();
    }

}

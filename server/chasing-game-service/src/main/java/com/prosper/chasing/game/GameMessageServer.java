package com.prosper.chasing.game;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.prosper.chasing.common.interfaces.game.GameService;
import com.prosper.chasing.game.Service.ChasingGameService;

public class GameMessageServer {

    private void start(){
        try {
            GameManage gameManage = new GameManage();
            ChasingGameService gameService = new ChasingGameService();
            gameService.setGameManage(gameManage);
            
            TServerSocket serverTransport = new TServerSocket(8811);
            GameService.Processor<GameService.Iface> processor = 
                    new GameService.Processor<GameService.Iface>(gameService);
            
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

package com.prosper.chasing.game;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;

import com.prosper.chasing.common.interfaces.game.GameException;
import com.prosper.chasing.common.interfaces.game.GameMessageService;

public class GameMessageHandler implements GameMessageService.Iface {

    @Override
    public void send(ByteBuffer bs) throws GameException, TException {
        System.out.println(bs);
    }

}

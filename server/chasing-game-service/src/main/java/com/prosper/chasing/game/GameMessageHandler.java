package com.prosper.chasing.game;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;

import com.prosper.chasing.common.interfaces.GameException;
import com.prosper.chasing.common.interfaces.GameMessageService;

public class GameMessageHandler implements GameMessageService.Iface {

    @Override
    public void send(ByteBuffer bs) throws GameException, TException {
        System.out.println(bs);
    }

}

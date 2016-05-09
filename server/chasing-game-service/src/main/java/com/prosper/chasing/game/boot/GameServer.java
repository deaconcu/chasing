package com.prosper.chasing.game.boot;

import com.prosper.chasing.common.boot.DefaultRPCApplication;

public class GameServer extends DefaultRPCApplication {

    @Override
    public String getIP() {
        return "127.0.0.1";
    }

    @Override
    public int getPort() {
        return 8812;
    }

    @Override
    public String getPackage() {
        return "com.prosper.chasing.game";
    }

    @Override
    public String getName() {
        return "gameServer";
    }

}

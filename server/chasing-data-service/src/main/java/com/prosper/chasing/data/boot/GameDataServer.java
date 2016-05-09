package com.prosper.chasing.data.boot;

import com.prosper.chasing.common.boot.DefaultRPCApplication;

public class GameDataServer extends DefaultRPCApplication {

    @Override
    public String getIP() {
        return "127.0.0.1";
    }

    @Override
    public int getPort() {
        return 8811;
    }

    @Override
    public String getPackage() {
        return "com.prosper.chasing.game";
    }

    @Override
    public String getName() {
        return "gameDataServer";
    }

}

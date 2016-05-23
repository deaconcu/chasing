package com.prosper.chasing.connection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestResponse {

    @Autowired
    GameWebSocketService gameWebSocketService;
    
    @PostConstruct
    public void run() {
        while(true) {
            byte[] bytes = new byte[]{0,0,0,1};
            ByteBuf in = Unpooled.copiedBuffer(bytes);
            
            Map<String, Object> customValueMap = new HashMap<>();
            customValueMap.put("gameId", 1);
            customValueMap.put("userId", 46);
            
            gameWebSocketService.executeData(in, customValueMap);
        }
    }

}

package com.prosper.chasing.connection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpointConfig;

public class MyClientConfigurator extends ClientEndpointConfig.Configurator {

    @Override
    public void beforeRequest(Map<String, List<String>> headers) {
        headers.put("sessionId", Arrays.asList("test-session-key"));
    }
}

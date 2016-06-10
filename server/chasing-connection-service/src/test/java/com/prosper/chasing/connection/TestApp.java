package com.prosper.chasing.connection;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class TestApp {

    public static void main(String[] args) {
        try {
            // open websocket
            final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://127.0.0.1:8201/websocket"));

            // add listener
            clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleMessage(ByteBuffer buffer) {
                    System.out.println("get data:" + buffer);
                }
            });

            ByteBuffer buffer = ByteBuffer.allocate(12);
            buffer.putInt(1);
            buffer.putInt(1);
            buffer.putInt(2);
            buffer.flip();
            clientEndPoint.sendMessage(buffer);
            
//            buffer = ByteBuffer.allocate(12);
//            buffer.putInt(1);
//            buffer.putInt(1);
//            buffer.putInt(2);
//            buffer.flip();
//            clientEndPoint.sendMessage(buffer);

            // wait 5 seconds for messages from websocket
            Thread.sleep(50000);

        } catch (InterruptedException ex) {
            System.err.println("InterruptedException exception: " + ex.getMessage());
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }
    }
}

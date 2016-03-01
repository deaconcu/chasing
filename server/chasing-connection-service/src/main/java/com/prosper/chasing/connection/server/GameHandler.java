package com.prosper.chasing.connection.server;


import com.prosper.chasing.common.client.ZkClient;
import com.prosper.chasing.connection.bean.Data;
import com.prosper.chasing.connection.game.MessageManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class GameHandler extends ChannelInboundHandlerAdapter {

    private MessageManager messageManager;

    public static GameHandler gameHandler;

    public static GameHandler instance() {
        return gameHandler;
    }
    
    public static void init(MessageManager messageQuManager) {
        gameHandler = new GameHandler(messageQuManager);
    }
    
    public GameHandler(MessageManager messageQuManager) {
        this.setMessageManager(messageQuManager);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        byte[] content = in.array();

        Data data = new Data(content);

        try {
            getMessageManager().add(data);
            while (in.isReadable()) { // (1)
                System.out.print((char) in.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

}

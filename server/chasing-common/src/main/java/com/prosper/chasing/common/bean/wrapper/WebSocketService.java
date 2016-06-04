package com.prosper.chasing.common.bean.wrapper;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;

public interface WebSocketService {
    
    /**
     * 处理http握手,主要用来检查权限
     * @return 需要存放到channel里边的一些自定义值
     */
    public ChannelInfo executeHttpRequest(FullHttpRequest req);

    /**
     * 处理tcp数据包
     */
    public void executeData(ByteBuf in, Map<String, Object> customValueMap);

    /**
     * 关闭连接
     */
    public void Close(Map<String, Object> customValueMap);
    
}

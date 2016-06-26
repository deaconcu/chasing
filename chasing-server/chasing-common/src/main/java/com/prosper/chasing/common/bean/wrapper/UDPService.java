package com.prosper.chasing.common.bean.wrapper;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;

public interface UDPService {
    
    /**
     * 处理UDP数据包
     */
    public int executeData(ByteBuf in);

}

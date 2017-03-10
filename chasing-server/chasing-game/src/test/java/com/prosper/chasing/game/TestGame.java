package com.prosper.chasing.game;

import com.prosper.chasing.common.bean.client.ThriftClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.ByteBuffer;

/**
 * Created by deacon on 2017/3/10.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestBeans.class})
public class TestGame {

    private Logger log = LoggerFactory.getLogger(TestGame.class);

    @Autowired
    private ThriftClient thriftClient;

    @Test
    public void testGame() throws Exception {
        log.info("test done");
        //ByteBuffer byteBuffer = ByteBuffer.wrap("this is a test".getBytes());
        //thriftClient.gameServiceClient("127.0.0.1", 8081).executeData(1, 1001, byteBuffer);
    }

}

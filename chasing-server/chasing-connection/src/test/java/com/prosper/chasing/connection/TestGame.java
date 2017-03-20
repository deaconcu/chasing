package com.prosper.chasing.connection;

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
    public void testRecieve() throws Exception {
        log.info("test start");
        Thread.sleep(2000);

        ByteBuffer byteBuffer = ByteBuffer.allocate(4).putInt(1);
        byteBuffer.flip();
        thriftClient.gameServiceClient("127.0.0.1", 8203).executeData(1, 2, byteBuffer);

        Thread.sleep(10000);
    }

}

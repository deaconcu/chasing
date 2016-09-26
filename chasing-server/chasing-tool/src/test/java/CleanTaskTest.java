import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.prosper.chasing.tool.bean.ActionData;
import com.prosper.chasing.tool.service.ActionDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestBean.class})
public class CleanTaskTest {

    Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    ActionDataService actionDataService;

    @Test
    public void actionDataTest() throws ClientProtocolException, IOException, InterruptedException {
        int i = 1;
        while (i <= 1000) {
            actionDataService.addActionData(new ActionData(i, i, i, i ++));
        }
        
        List<Float> xList = actionDataService.getActionDataX(100);
        System.out.println(xList);
    }

}

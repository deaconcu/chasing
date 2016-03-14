package com.prosper.game.http;

import java.util.LinkedList;
import java.util.List;

import org.apache.http.entity.StringEntity;

import com.prosper.game.http.client.HttpClientProxy;
import com.prosper.game.http.util.JsonUtil;

public class HttpClientTest {

    public static void main(String... args) {
        HttpClientProxy proxy = new HttpClientProxy();
        String response = proxy.post("http://10.10.69.151:8077/messagepool/messagecore/send", 
                new StringEntity("{\"topic\":\"youku.fingerprint.analogue.action\", \"token\":\"565d0cd2e4b0ea3df8b0b883\", \"msg\":{\"test\":1}}", "utf-8"), 3000, 3000);
        System.out.println(response);
    }
}

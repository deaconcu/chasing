package com.prosper.chasing.common.bean.client;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.util.CommonUtil;

@Component
public class HttpClientProxy {
	
	private Logger log = LoggerFactory.getLogger(HttpClientProxy.class);
	
	private static final int MAX_SOCKET_TIMEOUT = 10000;
	private static final int MAX_CONNECT_TIMEOUT = 10000;

	private CloseableHttpClient httpClient;

	public HttpClientProxy() {
		httpClient = HttpClients.createDefault();
	}

	/**
	 * get content from url
	 * @param url
	 * @param socketTimeout
	 * @param connectTimeout
	 * @return
	 */
	public String get(String url, int socketTimeout, int connectTimeout) {
		if (socketTimeout <= 0) {
			socketTimeout = MAX_SOCKET_TIMEOUT;
		}
		if (connectTimeout <= 0) {
			socketTimeout = MAX_CONNECT_TIMEOUT;
		}
		
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(socketTimeout)
				.setConnectionRequestTimeout(connectTimeout)
				.setConnectTimeout(connectTimeout)
				.build();

		HttpGet httpget = new HttpGet(url);
		httpget.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		String responseString = null;
		try {
			response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			}
		} catch (IOException e) {
			throw new RuntimeException(
					"http client failed to get response from url: " + url, e);
		} finally {
			try {
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				log.warn("response is not closed, " + "exception:\n" + CommonUtil.getStackTrace(e));
			}
		}
		return responseString;
	}
	
	/**
     * post content to url
     * @param url
     * @param socketTimeout(ms)
     * @param connectTimeout(ms)
     * @return
     */
    public String post(String url, HttpEntity requestEntity, int socketTimeout, int connectTimeout) {
        if (socketTimeout <= 0) {
            socketTimeout = MAX_SOCKET_TIMEOUT;
        }
        if (connectTimeout <= 0) {
            socketTimeout = MAX_CONNECT_TIMEOUT;
        }
        
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(connectTimeout)
                .setConnectTimeout(connectTimeout)
                .build();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        httpPost.setEntity(requestEntity);;
        CloseableHttpResponse response = null;
        String responseString = null;
        try {
            response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            String postString = "";
            try {
                postString = EntityUtils.toString(requestEntity);
            } catch (IOException ie) {
                postString = "parse error";
            }
            throw new RuntimeException(
                    "http client failed to post data to url: " + url + ", post data:" + postString, e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.warn("response is not closed, " + "exception:\n" + CommonUtil.getStackTrace(e));
            }
        }
        return responseString;
    }

}

package com.prosper.chasing.data.service;

import com.prosper.chasing.data.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

/**
 * Created by deacon on 2017/2/17.
 */
public class UserQueueService {

    @Autowired
    private Jedis jedis;

    public void addUser(int type, int userId) {
        String userListKey = Constant.CacheName.systemUserList + "0";
        jedis.lrem(userListKey, 0L, Integer.toString(userId));
        jedis.rpush(userListKey, Integer.toString(userId));

        String userStateKey = Constant.CacheName.userState + Integer.toString(userId);
        jedis.set(userStateKey, "1");
    }

    public void removeUser(int type, int userId) {
        String userListKey = Constant.CacheName.systemUserList + "0";
        jedis.lrem(userListKey, 0L, Integer.toString(userId));

        String userStateKey = Constant.CacheName.userState + Integer.toString(userId);
        jedis.del(userStateKey);
    }
}

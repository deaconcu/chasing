package com.prosper.chasing.data.service;

import com.prosper.chasing.common.exception.ResourceNotExistException;
import com.prosper.chasing.data.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

/**
 * Created by deacon on 2017/2/17.
 */
@Service
public class UserQueueService {

    @Autowired
    private Jedis jedis;

    public void addUser(int userId, int userLevel, String gameCode) {
        if (gameCode == null || gameCode.equals("")) {
            throw new ResourceNotExistException("game code not exist");
        }

        // TODO 有一个问题是先加入map的用户可能被后安排进游戏
        jedis.hset(Constant.CacheName.userQueue,
                Integer.toString(userId), Integer.toString(userLevel) + "-" + gameCode);

        // 设置用户当前是否加入游戏
        jedis.hset(Constant.CacheName.userState, Integer.toString(userId), "1");
    }

    public void removeUser(int userId) {
        jedis.hdel(Constant.CacheName.userQueue, Integer.toString(userId));
        jedis.hdel(Constant.CacheName.userState, Integer.toString(userId));
    }

}

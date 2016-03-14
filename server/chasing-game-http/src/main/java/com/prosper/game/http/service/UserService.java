package com.prosper.game.http.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prosper.game.http.bean.User;
import com.prosper.game.http.exception.InvalidArgumentException;
import com.prosper.game.http.mapper.UserMapper;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    
    public long createUser(User user, String ipAddress) {
        if (user ==  null || user.getName() == null || "".equals(user.getName())) {
            throw new InvalidArgumentException("user is empty");
        }
        user.setEmail("");
        user.setPhone("");
        user.setRegisterTime(System.currentTimeMillis());
        user.setRegisterIp(ipAddress);
        user.setLastLoginTime(0L);
        user.setLastLoginIp(ipAddress);
        
        return userMapper.insert(user);
    }
    
}

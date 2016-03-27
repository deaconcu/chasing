package com.prosper.chasing.game.http.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.prosper.chasing.game.http.bean.User;
import com.prosper.chasing.game.http.bean.UserData;
import com.prosper.chasing.game.http.exception.InvalidArgumentException;
import com.prosper.chasing.game.http.mapper.UserDataMapper;
import com.prosper.chasing.game.http.mapper.UserMapper;
import com.prosper.chasing.game.http.util.CommonUtil;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserDataMapper userDataMapper;
    
    public void createUser(User user, String ipAddress) {
        if (user ==  null || user.getName() == null || "".equals(user.getName())) {
            throw new InvalidArgumentException("user is empty");
        }
        user.setEmail("");
        user.setPhone("");
        user.setRegisterTime(CommonUtil.getTime(new Date()));
        user.setRegisterIp(ipAddress);
        user.setLastLoginTime(CommonUtil.getTime(new Date()));
        user.setLastLoginIp(ipAddress);
        
        int length = 16;
        StringBuilder builder = new StringBuilder(length);  
        for (int i = 0; i < length; i++) {  
            builder.append((char) (ThreadLocalRandom.current().nextInt(33, 127)));  
        }  
        String password = builder.toString();
        byte[] passwordBytes;
        String passwordMd5;
        try {
            passwordBytes = user.getPassword().getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] passwordMd5Bytes = md.digest(passwordBytes);
            passwordMd5 = new String(passwordMd5Bytes);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        
        user.setPassword(password);
        user.setPasswordMd5(passwordMd5);
        userMapper.insert(user);
        
        UserData userData = new UserData();
        userData.setDistance(0);
        userData.setHill(0);
        userData.setRiver(0);
        userData.setRoad(0);
        
        userData.setCreateTime(CommonUtil.getTime(new Date()));
        userData.setUpdateTime(CommonUtil.getTime(new Date()));
        userDataMapper.insert(userData);
    }

    public boolean checkUser(User user) {
        if (user.getPassword() == null || "".equals(user.getPassword())) {
            return false;
        }
        
        User existUser = null;
        if (user.getId() != null) {
            existUser = userMapper.selectOne(user.getId());
        } else if (user.getPhone() != null && !"".equals(user.getPhone())) {
            existUser = userMapper.selectOneByPhone(user.getPhone());
        } else if (user.getEmail() != null && !"".equals(user.getEmail())) {
            existUser = userMapper.selectOneByPhone(user.getPhone());
        }
        
        if (existUser == null) {
            return false;
        }
        
        byte[] passwordBytes;
        String passwordMd5;
        try {
            passwordBytes = user.getPassword().getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] passwordMd5Bytes = md.digest(passwordBytes);
            passwordMd5 = new String(passwordMd5Bytes);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        
        if (passwordMd5.equals(existUser.getPasswordMd5())) {
            return true;
        }
        return false;
    }
    
}

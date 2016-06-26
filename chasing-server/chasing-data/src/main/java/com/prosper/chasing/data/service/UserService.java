package com.prosper.chasing.data.service;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

import com.prosper.chasing.common.util.CommonUtil;
import com.prosper.chasing.data.bean.Friend;
import com.prosper.chasing.data.bean.User;
import com.prosper.chasing.data.bean.UserData;
import com.prosper.chasing.data.exception.InvalidArgumentException;
import com.prosper.chasing.data.exception.ResourceNotExistException;
import com.prosper.chasing.data.mapper.FriendMapper;
import com.prosper.chasing.data.mapper.UserDataMapper;
import com.prosper.chasing.data.mapper.UserMapper;
import com.prosper.chasing.data.util.Constant.CacheName;
import com.prosper.chasing.data.util.Constant.FriendState;
import com.prosper.chasing.data.util.Constant.FriendType;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserDataMapper userDataMapper;
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private Jedis jedis;
    
    /**
     * 创建用户，只需要提交用户名和ip就可以
     * @param user 用户
     * @param ip ip地址
     */
    public void createUser(User user, String ip) {
        if (user ==  null || user.getName() == null || "".equals(user.getName())) {
            throw new InvalidArgumentException("user is empty");
        }
        user.setEmail("");
        user.setPhone("");
        user.setRegisterTime(CommonUtil.getTime(new Date()));
        user.setRegisterIp(ip);
        user.setLastLoginTime(CommonUtil.getTime(new Date()));
        user.setLastLoginIp(ip);
        
        String password = getRamdomPassword();
        user.setPassword(password);
        user.setPasswordMd5(getMd5(password));
        userMapper.insert(user);
        
        UserData userData = new UserData();
        userData.setId(user.getId());
        userData.setDistance(0);
        userData.setHill(0);
        userData.setRiver(0);
        userData.setRoad(0);
        
        userData.setCreateTime(CommonUtil.getTime(new Date()));
        userData.setUpdateTime(CommonUtil.getTime(new Date()));
        userDataMapper.insert(userData);
    }

    /**
     * 验证用户是否能登录，
     * 可以使用这些组合：id + password，email + password, phone + password
     * @param postUser
     * @return user 包含了该用户全部信息 
     * @return null 没有找到该用户
     */
    public User checkLogin(User postUser) {
        if (postUser.getPassword() == null || "".equals(postUser.getPassword())) {
            return null;
        }
        
        User user = null;
        if (postUser.getId() != null) {
            user = userMapper.selectOne(postUser.getId());
        } else if (postUser.getPhone() != null && !"".equals(postUser.getPhone())) {
            user = userMapper.selectOneByPhone(postUser.getPhone());
        } else if (postUser.getEmail() != null && !"".equals(postUser.getEmail())) {
            user = userMapper.selectOneByPhone(postUser.getPhone());
        }
        
        if (user == null) {
            return null;
        }
        
        String postPasswordMd5 = getMd5(postUser.getPassword());
        if (!postPasswordMd5.equals(user.getPasswordMd5())) {
            return null;
        }
        return user;
    }
    
    /**
     * 登录，需要返回sessionId
     */
    public String login(User postUser) {
        User user = checkLogin(postUser);
        if (user == null) {
            throw new ResourceNotExistException("user is not exist");
        }
        String sessionCacheName = CacheName.session + user.getId();
        String sessionId = jedis.get(sessionCacheName);
        
        if (sessionId == null) {
            sessionId = getSessionId();
            jedis.set(sessionCacheName, sessionId);
        }
        return sessionId;
    }
    
    /**
     * 登出
     */
    public void logout(String userId) {
        String sessionCacheName = CacheName.session + userId;
        String sessionId = jedis.get(sessionCacheName);
        
        if(sessionId != null) {
            jedis.del(sessionCacheName);
        }
    }
    
    /**
     * 提交朋友申请
     * @param userId 申请者id
     */
    public void applyFriend(int userId, Friend postFriend) {
        if (userId == postFriend.getFriendUserId()) {
            throw new InvalidArgumentException("can't add friend to yourself");
        }
        User user = userMapper.selectOne(userId);
        if (user == null) {
            throw new InvalidArgumentException("user is not exist");
        }
        int friendUserId = postFriend.getFriendUserId();
        User friendUser = userMapper.selectOne(friendUserId);
        if (friendUser == null) {
            throw new InvalidArgumentException("friend is not exist");
        }
            
        Friend friend = friendMapper.selectOneByUserFriend(userId, friendUserId);
        if (friend == null) {
            friend = new Friend();
            friend.setUserId(userId);
            friend.setFriendUserId(friendUserId);
            friend.setState(FriendState.APPLYING);
            friend.setCreateTime(CommonUtil.getTime(new Date()));
            friendMapper.insert(friend);
        } else {
            if (friend.getState() == FriendState.APPLYING) {
                throw new InvalidArgumentException("friend request is already sended");
            } else if (friend.getState() == FriendState.APPROVED) {
                throw new InvalidArgumentException("already friends");
            }
        }
    }
    
    /**
     * 通过朋友申请
     * @param userId 操作者id
     */
    public void approveFriend(int userId, Friend postFriend) {
        if (userId == postFriend.getFriendUserId()) {
            throw new InvalidArgumentException("can't add friend to yourself");
        }
        User user = userMapper.selectOne(userId);
        if (user == null) {
            throw new InvalidArgumentException("user is not exist");
        }
        int friendUserId = postFriend.getFriendUserId();
        User friendUser = userMapper.selectOne(friendUserId);
        if (friendUser == null) {
            throw new InvalidArgumentException("friend is not exist");
        }
            
        Friend friend = friendMapper.selectOneByUserFriend(friendUserId, userId);
        if (friend == null) {
            throw new InvalidArgumentException("friend request is not exist");
        } else {
            if (friend.getState() == FriendState.APPLYING) {
                friend.setState(FriendState.APPROVED);
                friend.setCreateTime(CommonUtil.getTime(new Date()));
                friendMapper.update(friend);
                Friend pairfriend = friendMapper.selectOneByUserFriend(userId, friendUserId);
                if (pairfriend == null) {
                    friend = new Friend();
                    friend.setUserId(userId);
                    friend.setFriendUserId(friendUserId);
                    friend.setState(FriendState.APPROVED);
                    friend.setCreateTime(CommonUtil.getTime(new Date()));
                    friendMapper.insert(friend);
                } else {
                    pairfriend.setState(FriendState.APPROVED);
                    pairfriend.setCreateTime(CommonUtil.getTime(new Date()));
                    friendMapper.update(pairfriend);
                }
            } else if (friend.getState() == FriendState.APPROVED) {
                throw new InvalidArgumentException("already friends");
            }
        }
    }
    
    /**
     * 删除朋友
     */
    public void deleteFriend(int userId, int friendUserId) {
        if (userId == friendUserId) {
            throw new InvalidArgumentException("can't delete yourself as a friend");
        }
        User user = userMapper.selectOne(userId);
        if (user == null) {
            throw new InvalidArgumentException("user is not exist");
        }
        User friendUser = userMapper.selectOne(friendUserId);
        if (friendUser == null) {
            throw new InvalidArgumentException("friend is not exist");
        }
        Friend friend = friendMapper.selectOneByUserFriend(userId, friendUserId);
        if (friend != null) {
            friendMapper.delete(friend.getId());
        }
        friend = friendMapper.selectOneByUserFriend(friendUserId, userId);
        if (friend != null) {
            friendMapper.delete(friend.getId());
        }
    }
    
    /**
     * 获取朋友列表
     */
    public List<Friend> getFriends(int userId, int type, int page, int pageLength) {
        if (type == FriendType.APPLYING_TO_ME) {
            return friendMapper.selectListByFriendStatePage(userId, FriendState.APPLYING, pageLength, (page - 1) * pageLength);
        } else if (type == FriendType.APPLYING_FROM_ME) {
            return friendMapper.selectListByUserStatePage(userId, FriendState.APPLYING, pageLength, (page - 1) * pageLength);
        } else if (type == FriendType.APPROVED) {
            return friendMapper.selectListByUserStatePage(userId, FriendState.APPROVED, pageLength, (page - 1) * pageLength);
        } else {
            throw new InvalidArgumentException("state is not valid");
        }
    }
    
    /**
     * 生成一个随机的sessionId;
     */
    private String getSessionId() {        
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }
    
    /**
     * 获得指定字符串的md5串
     */
    private String getMd5(String s) {
        byte[] bytes;
        StringBuilder md5 = new StringBuilder();
        try {
            bytes = s.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] passwordMd5Bytes = md.digest(bytes);
            for (int i = 0; i < passwordMd5Bytes.length; i++) {
                if ((0xff & passwordMd5Bytes[i]) < 0x10) {
                    md5.append("0" + Integer.toHexString((0xFF & passwordMd5Bytes[i])));
                } else {
                    md5.append(Integer.toHexString(0xFF & passwordMd5Bytes[i]));
                }
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return md5.toString();
    }
    
    private String getRamdomPassword() {
        int length = 16;
        StringBuilder passwordBuilder = new StringBuilder(length);  
        for (int i = 0; i < length; i++) {  
            passwordBuilder.append((char) (ThreadLocalRandom.current().nextInt(33, 127)));  
        }
        return passwordBuilder.toString();
    }

    

    

    

    
}

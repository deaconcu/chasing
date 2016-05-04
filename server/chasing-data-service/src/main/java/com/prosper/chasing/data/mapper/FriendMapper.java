package com.prosper.chasing.data.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.prosper.chasing.data.bean.Friend;

public interface FriendMapper extends MapperI<Friend>{

    public Friend selectOneByUserFriend(
            @Param("userId") int userId, 
            @Param("friendUserId") int friendUserId);

    public List<Friend> selectListByUserStatePage(
            @Param("userId") int userId, 
            @Param("state") int state,
            @Param("limit") int limit, 
            @Param("offset") int offset);

    public List<Friend> selectListByFriendStatePage(
            @Param("friendUserId") int friendUserId, 
            @Param("state") int state,
            @Param("limit") int limit, 
            @Param("offset") int offset);
}

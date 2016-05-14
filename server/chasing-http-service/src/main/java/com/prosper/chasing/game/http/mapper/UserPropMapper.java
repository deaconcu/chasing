package com.prosper.chasing.game.http.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.prosper.chasing.game.http.bean.Prop;
import com.prosper.chasing.game.http.bean.UserProp;

public interface UserPropMapper extends MapperI<UserProp>{

    List<UserProp> selectListByUser(@Param("userId")int userId);

    UserProp selectOneByUserProp(@Param("userId")int userId, @Param("propCode")String propCode);

}
package com.prosper.chasing.data.mapper;

import com.prosper.chasing.data.bean.UserData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDataMapper extends MapperI<UserData>{

    void updateUserIntoGame(
            @Param("userIdList") List<Integer> userIdList,
            @Param("gameId") Integer gameId,
            @Param("state") Short state);
}

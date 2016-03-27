package com.prosper.chasing.game.http.mapper;

import com.prosper.chasing.game.http.bean.User;

public interface UserMapper extends MapperI<User>{

    User selectOneByPhone(String phone);

}

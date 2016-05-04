package com.prosper.chasing.data.mapper;

import com.prosper.chasing.data.bean.User;

public interface UserMapper extends MapperI<User>{

    User selectOneByPhone(String phone);

}

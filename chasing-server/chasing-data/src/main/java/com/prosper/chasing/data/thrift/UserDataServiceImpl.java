package com.prosper.chasing.data.thrift;

import com.prosper.chasing.common.util.ViewTransformer;
import com.prosper.chasing.data.bean.Game;
import com.prosper.chasing.data.bean.UserData;
import com.prosper.chasing.data.mapper.UserDataMapper;
import com.prosper.chasing.data.service.UserService;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.wrapper.ThriftRPCService;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.UserDataService;
import com.prosper.chasing.common.interfaces.data.UserTr;
import com.prosper.chasing.data.service.PropService;

@Component
@ThriftRPCService(processorClass = UserDataService.Processor.class)
public class UserDataServiceImpl implements UserDataService.Iface {
    
    private UserDataMapper userDataMapper;

    @Override
    public UserTr getUser(int userId) throws TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateUser(UserTr userTr) throws TException {
        UserData userData = ViewTransformer.transferObject(userTr, UserData.class);
        userDataMapper.update(userData);
    }
}

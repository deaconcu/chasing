package com.prosper.chasing.data.thrift;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.wrapper.RPCService;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.UserDataService;
import com.prosper.chasing.common.interfaces.data.UserTr;
import com.prosper.chasing.data.service.PropService;

@Component
@RPCService(processorClass = UserDataService.Processor.class)
public class UserDataServiceImpl implements UserDataService.Iface {
    
    private PropService propService;

    @Override
    public UserTr getUser(int userId) throws TException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateUser(UserTr user) throws TException {
        // TODO Auto-generated method stub
        
    }



}

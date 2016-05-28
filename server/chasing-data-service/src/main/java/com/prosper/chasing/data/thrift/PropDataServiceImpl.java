package com.prosper.chasing.data.thrift;

import java.util.List;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.wrapper.ThriftRPCService;
import com.prosper.chasing.common.interfaces.data.GameDataService;
import com.prosper.chasing.common.interfaces.data.PropDataService;
import com.prosper.chasing.common.interfaces.data.UserPropTr;
import com.prosper.chasing.common.util.ViewTransformer;
import com.prosper.chasing.data.bean.UserProp;
import com.prosper.chasing.data.service.PropService;

@Component
@ThriftRPCService(processorClass = PropDataService.Processor.class)
public class PropDataServiceImpl implements PropDataService.Iface {
    
    @Autowired
    private PropService propService;

    @Override
    public List<UserPropTr> getUserProp(int userId) throws TException {
        List<UserProp> userPropList = propService.getUserProp(userId);
        return ViewTransformer.transferList(userPropList, UserPropTr.class);
    }

    @Override
    public void updateUserProp(int userId, List<UserPropTr> usedUserPropList)
            throws TException {
        // TODO Auto-generated method stub
    }


}

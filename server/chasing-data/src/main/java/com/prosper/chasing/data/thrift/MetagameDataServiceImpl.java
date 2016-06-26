package com.prosper.chasing.data.thrift;

import java.util.List;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.wrapper.ThriftRPCService;
import com.prosper.chasing.common.interfaces.data.MetagameDataService;
import com.prosper.chasing.common.interfaces.data.MetagameTr;
import com.prosper.chasing.common.util.ViewTransformer;
import com.prosper.chasing.data.service.GameService;

@Component
@ThriftRPCService(processorClass = MetagameDataService.Processor.class)
public class MetagameDataServiceImpl implements MetagameDataService.Iface {
    
    @Autowired
    private GameService gameService;

    @Override
    public List<MetagameTr> getMetagame(List<Integer> metagameIdList)
            throws TException {
        return ViewTransformer.transferList(gameService.getMetagames(metagameIdList), MetagameTr.class);
    }

}

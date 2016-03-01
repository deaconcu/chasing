package com.prosper.chasing.common.client;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;

import com.prosper.chasing.common.interfaces.GameMessageService;

public class ThriftClient {
    public static TTransport open(String [] args) {
        try {
            TTransport transport;
            transport = new TSocket("localhost", 9090);
            transport.open();

            TProtocol protocol = new  TBinaryProtocol(transport);
            GameMessageService.Client client = new GameMessageService.Client(protocol);

            client.getInputProtocol().getTransport();
            transport.close();
        } catch (TException x) {
            x.printStackTrace();
        }  
        return null;
    }
    
}

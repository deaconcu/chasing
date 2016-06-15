using UnityEngine;
using UnityEngine.Networking;
using System.Collections.Generic;

using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System;

public class NetworkService
{

    private static NetworkService instance;

    private string serverIp = "127.0.0.1";
    private int serverPort = 8201;

    private UdpClient udpClient;

    public NetworkService()
    {
        this.udpClient = new UdpClient(11000);
    }

    public static NetworkService getInstance()
    {
        return instance;
    }

    public void send(byte[] message)
    {
        Debug.Log("write log");
        udpClient.Send(message, message.Length, serverIp, serverPort);
    }

    public LinkedList<byte[]> receive()
    {
        if (udpClient.Available == 0)
        {
            return null;
        }

        IPEndPoint RemoteIpEndPoint = new IPEndPoint(IPAddress.Parse(serverIp), serverPort);
        LinkedList<byte[]> dataList = new LinkedList<byte[]>();
        while (udpClient.Available > 0)
        {
            dataList.AddLast(udpClient.Receive(ref RemoteIpEndPoint));
        }

        foreach(byte[] data in dataList)
        {
            Debug.Log(BitConverter.ToString(data));
        }
        return dataList;
    }

}

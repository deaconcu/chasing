using UnityEngine;
using System.Collections.Generic;

public class GameBase : MonoBehaviour {

    private NetworkService networkService;
    private GameService gameService;

	void Start () {
        networkService = new NetworkService();
        gameService = new GameService(Global.userId);
	}
	
	void Update () {
        LinkedList<byte[]> data = networkService.receive();
        gameService.executeData(data);

        if (Input.GetKeyDown("w"))
        {
            byte[] sendData = gameService.sendRunAction();
            networkService.send(sendData);
        }
	}
}

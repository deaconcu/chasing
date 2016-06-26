using UnityEngine;
using System.Collections.Generic;
using System.Text;

public class GameService
{

    Dictionary<int, GameUser> userMap = new Dictionary<int, GameUser>();
    Dictionary<string, GameObject> playerMap = new Dictionary<string, GameObject>();

    int userId;
    GameObject self = null;

    public GameService(int userId)
    {
        this.userId = userId;
    }

    public void executeData(LinkedList<byte[]> dataList)
    {
        if (dataList == null || dataList.Count == 0)
        {
            return;
        }

        foreach (byte[] data in dataList)
        {
            if (data[0] == 1)
            {
                executeSyncMessage(data);
            }
            else if (data[1] == 2)
            {
                executeActionMessage(data);
            }
            else
            {
                // TODO log
            }
        }
    }

    private void executeSyncMessage(byte[] data)
    {
        parseSyncMessage(data);
        syncPlayer();
    }

    private SyncMessage parseSyncMessage(byte[] data)
    {
        // todo
        return null;
    }

    private void executeActionMessage(byte[] data)
    {
        SyncMessage syncMessage = parseSyncMessage(data);
        if (syncMessage == null) return;
    }

    private ActionMessage parseActionMessage(byte[] data)
    {
        // todo
        return null;
    }

    private void syncPlayer()
    {
        GameObject[] players = GameObject.FindGameObjectsWithTag("player");

        playerMap.Clear();
        foreach (GameObject player in players)
        {
            playerMap.Add(player.name, player);
        }

        foreach (GameUser gameUser in userMap.Values)
        {
            int userId = gameUser.id;
            GameObject player = null;
            if (this.userId == userId)
            {
                playerMap.TryGetValue("self", out player);
                updatePlayer(player, gameUser);
                continue;
            }

            playerMap.TryGetValue(userId.ToString(), out player);
            if (player == null)
            {
                createPlayer(gameUser);
            }
            else
            {
                updatePlayer(player, gameUser);
                playerMap.Remove(userId.ToString());
            }
        }

        foreach (GameObject player in playerMap.Values)
        {
            deletePlayer(player);
        }
    }

    private void createPlayer(GameUser gameUser)
    {
        GameObject player = new GameObject();
        player.tag = "player";
        if (gameUser.id == userId)
        {
            this.self = player;
        }

        Mesh bungalov = Resources.Load<Mesh>("bungalov_02");
        player.AddComponent<MeshFilter>().mesh = bungalov;
        player.AddComponent<MeshCollider>();
        player.AddComponent<MeshRenderer>();

        Transform transform = player.GetComponent<Transform>();
        transform.localPosition = new Vector3(gameUser.x, gameUser.y, gameUser.z);
    }

    private void updatePlayer(GameObject player, GameUser gameUser)
    {
        Transform transform = player.GetComponent<Transform>();
        transform.localPosition = new Vector3(gameUser.x, gameUser.y, gameUser.z);
    }

    private void deletePlayer(GameObject player)
    {
        Object.Destroy(player);
    }

    public byte[] sendRunAction()
    {
        byte[] sessionByte = Encoding.ASCII.GetBytes("test-session-key");
        byte[] content = new byte[] {0, 0, 0, 2, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1 };

        byte[] message = new byte[sessionByte.Length + content.Length];
        System.Buffer.BlockCopy(sessionByte, 0, message, 0, sessionByte.Length);
        System.Buffer.BlockCopy(content, 0, message, sessionByte.Length, content.Length);
        return message;
    }
}

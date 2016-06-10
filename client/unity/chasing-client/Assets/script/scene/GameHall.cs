using UnityEngine;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;

public class GameHall : MonoBehaviour
{

    private List<Game> gameData = null;
    private List<Game> joinedGameData = null;
    private int dataState = 0;
    private GameObject loadingImg;

    // Use this for initialization
    void Start()
    {
        
        loadingImg = GameObject.Find("loadingImg");
        User user = DbService.getUser();
        Global.userId = user.id;
        Debug.Log(JsonConvert.SerializeObject(user));
        if (user == null)
        {
            SceneManager.LoadScene("auth");
        }
        GameObject returnButton = GameObject.Find("returnButton");
        returnButton.GetComponent<Button>().onClick.AddListener(toMain);
    }

    public void toMain()
    {
        SceneManager.LoadScene("main");
    }

    public void toGame(Game game)
    {
        if (game.metagameId == 1)
        {
            SceneManager.LoadScene("chasing");
        }
    }

    // Update is called once per frame
    void Update()
    {
        if (dataState == 0)
        {
            GameObject scroll = GameObject.Find("Content");
            var children = new List<GameObject>();
            foreach (Transform child in scroll.transform) children.Add(child.gameObject);
            children.ForEach(child => Destroy(child));

            loadingImg.SetActive(true);
            StartCoroutine(getGameList(Global.userId));
            dataState = 1;
        }
        else if (dataState == 1)
        {
            Transform transform = loadingImg.GetComponent<Transform>();
            transform.Rotate(Vector3.forward * Time.deltaTime * 720);
        }
        else if (dataState == 2)
        {
            GameObject scroll = GameObject.Find("Content");

            int firstY = -20;
            int gapY = 50;
            int n = 0;
            scroll.GetComponent<RectTransform>().sizeDelta = new Vector2(300, 100 * gameData.Count * 3);
            for (int i = 0; i < gameData.Count; i++)
            {
                Game game = gameData[i];
                int posY = firstY - gapY * (n++);
                string gameInfo = "id:" + game.id.ToString();
                ObjectFactoryService.createText(scroll, "name", "arial", 18, Color.white, gameInfo, 20, posY, 300, 30, TextAnchor.MiddleLeft);

                if (game.state == 1)
                {
                    GameObject gameObject = ObjectFactoryService.createButton(scroll, "name", "arial", 14, "开始游戏", 240, posY, 80, 30);
                    gameObject.GetComponent<Button>().onClick.AddListener(delegate { StartCoroutine(startGame(game)); });
                } else if (game.state == 2 || game.state == 3 || game.state == 4)
                {
                    string stateInfo = "正在进行";
                    ObjectFactoryService.createText(scroll, "name", "arial", 18, Color.white, stateInfo, 120, posY, 300, 30, TextAnchor.MiddleLeft);
                    GameObject gameObject = ObjectFactoryService.createButton(scroll, "name", "arial", 14, "进入游戏", 240, posY, 80, 30);
                    gameObject.GetComponent<Button>().onClick.AddListener(delegate { StartCoroutine(enterGame(game)); });
                } else if (game.state == 5)
                {
                    string stateInfo = "已结束";
                    ObjectFactoryService.createText(scroll, "name", "arial", 18, Color.white, stateInfo, 240, posY, 300, 30, TextAnchor.MiddleLeft);
                }
            }
            dataState = 3;
            if (loadingImg.activeSelf)
            {
                loadingImg.SetActive(false);
            }
        }
        else
        {

        }
    }

    private IEnumerator startGame(Game game)
    {
        Debug.Log("game id:" + game.id);
        if (game.state != 1)
        {
            Debug.Log("game state not equal 1");
            yield break;
        }
        String url = "http://" + Global.serverIp + ":" + Global.serverPort + "/games?_method=PUT";
        Dictionary<string, string> headers = new Dictionary<string, string>();
        headers.Add("Content-Type", "application/json");
        headers.Add("sessionId", Global.sessionId);
        headers.Add("userId", Global.userId.ToString());

        Dictionary<string, System.Object> postDataList = new Dictionary<string, System.Object>();
        postDataList.Add("id", game.id);
        postDataList.Add("duration", game.duration);
        postDataList.Add("state", 2);

        string postDataString = JsonConvert.SerializeObject(postDataList);
        Debug.Log(postDataString);
        byte[] postBytes = Encoding.UTF8.GetBytes(postDataString);

        WWW www = new WWW(url, postBytes, headers);
        while (!www.isDone)
        {
            Debug.Log("waiting for response ...");
            yield return null;
        }
        String response = www.text;
        Debug.Log(response);
        dataState = 0;
    }

    /*
     * 进入游戏
     */
    public IEnumerator enterGame(Game game)
    {
        String url = "http://" + Global.serverIp + ":" + Global.serverPort + "/games?userId=" + Global.userId.ToString();

        Dictionary<string, string> headers = new Dictionary<string, string>();
        headers.Add("Content-Type", "application/json");
        headers.Add("sessionId", Global.sessionId);
        headers.Add("userId", Global.userId.ToString());

        Debug.Log(JsonConvert.SerializeObject(headers));

        WWW www = new WWW(url, null, headers);
        while (!www.isDone)
        {
            Debug.Log("waiting for response ...");
            yield return null;
        }

        String response = www.text;
        Debug.Log(response);
        var gameResponseDef = new { code = 0, data = new List<Game>() };
        var gameResponseJson = JsonConvert.DeserializeAnonymousType(response, gameResponseDef);

        List<Game> gameList;
        int code = gameResponseJson.code;
        if (code != 200)
        {
            yield break;
        }

        gameList = gameResponseJson.data;
        Debug.Log(JsonConvert.SerializeObject(gameList));

        bool joined = false;
        foreach (Game joinedGame in gameList)
        {
            if (joinedGame.id == game.id) joined = true;
        }

        if (!joined)
        {
            Debug.Log("you are not in the game");
            yield break;
        }

        toGame(game);
    }

    /*
     * 获取游戏列表，显示所有没有结束的游戏
     */
    public IEnumerator getGameList(int userId)
    {
        String url = "http://" + Global.serverIp + ":" + Global.serverPort + "/games";

        Dictionary<string, string> headers = new Dictionary<string, string>();
        headers.Add("Content-Type", "application/json");
        headers.Add("sessionId", Global.sessionId);
        headers.Add("userId", userId.ToString());

        Debug.Log(JsonConvert.SerializeObject(headers));

        WWW www = new WWW(url, null, headers);
        while (!www.isDone)
        {
            Debug.Log("waiting for response ...");
            yield return null;
        }

        String response = www.text;
        Debug.Log(response);
        var gameResponseDef = new { code = 0, data = new List<Game>() };
        var gameResponseJson = JsonConvert.DeserializeAnonymousType(response, gameResponseDef);

        List<Game> gameList;
        int code = gameResponseJson.code;
        if (code != 200)
        {
            yield break;
        }

        gameList = gameResponseJson.data;
        Debug.Log(JsonConvert.SerializeObject(gameList));

        this.gameData = gameList;
        dataState = 2;
    }
}

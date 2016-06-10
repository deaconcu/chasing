using UnityEngine;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using System;
using System.Collections;
using System.Collections.Generic;
using Newtonsoft.Json;

public class Main : MonoBehaviour
{
    private UserData userData = null;
    private Boolean userDataLoaded = false;
    private GameObject loadingImg;

    // Use this for initialization
    void Start()
    {
        loadingImg = GameObject.Find("loadingImg");
        User user = DbService.getUser();
        Debug.Log(JsonConvert.SerializeObject(user));
        if (user == null)
        {
            SceneManager.LoadScene("auth");
        }

        StartCoroutine(getUserData(user.id));

        GameObject storeBtnObj = GameObject.Find("storeButton");
        Button storeBtn = storeBtnObj.GetComponent<Button>();
        storeBtn.onClick.AddListener(StoreBtnOnClick);

        GameObject gameBtnObj = GameObject.Find("gameButton");
        Button gameBtn = gameBtnObj.GetComponent<Button>();
        gameBtn.onClick.AddListener(gameBtnOnClick);
    }

    public void StoreBtnOnClick()
    {
        SceneManager.LoadScene("store");
    }

    public void gameBtnOnClick()
    {
        SceneManager.LoadScene("gameHall");
    }

    // Update is called once per frame
    void Update()
    {
        if (userData == null)
        {
            Transform transform = loadingImg.GetComponent<Transform>();
            transform.Rotate(Vector3.forward * Time.deltaTime * 720);
        } else
        {
            if (!userDataLoaded)
            {
                GameObject distance = GameObject.Find("distance");
                distance.GetComponent<Text>().text = userData.distance.ToString();

                GameObject roadSpeed = GameObject.Find("roadSpeed");
                roadSpeed.GetComponent<Text>().text = userData.road.ToString();

                GameObject hillSpeed = GameObject.Find("hillSpeed");
                hillSpeed.GetComponent<Text>().text = userData.hill.ToString();

                GameObject riverSpeed = GameObject.Find("riverSpeed");
                riverSpeed.GetComponent<Text>().text = userData.river.ToString();
            }
            if (loadingImg.activeSelf)
            {
                loadingImg.SetActive(false);
            }
        }
    }

    public IEnumerator getUserData(int id)
    {
        String url = "http://" + Global.serverIp + ":" + Global.serverPort + "/userData?id=" + id;

        Dictionary<string, string> headers = new Dictionary<string, string>();
        headers.Add("Content-Type", "application/json");
        headers.Add("sessionId", Global.sessionId);
        headers.Add("userId", id.ToString());

        Debug.Log(JsonConvert.SerializeObject(headers));

        WWW www = new WWW(url, null, headers);
        while (!www.isDone)
        {
            yield return null;
        }

        String response = www.text;
        var responseDef = new { code = 0, data = new UserData() };
        var responseJson = JsonConvert.DeserializeAnonymousType(response, responseDef);

        int code = responseJson.code;
        if (code == 200)
        {
            this.userData = responseJson.data;
        }
    }
}

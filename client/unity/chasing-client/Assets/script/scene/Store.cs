using UnityEngine;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;

public class Store : MonoBehaviour
{
    private List<Prop> storeData = null;
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
        returnButton.GetComponent<Button>().onClick.AddListener(returnToMain);
    }

    public void returnToMain()
    {
        SceneManager.LoadScene("main");
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
            StartCoroutine(getPropInfo(Global.userId));
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
            int gapY = 100;
            int n = 0;
            scroll.GetComponent<RectTransform>().sizeDelta = new Vector2(300, 100 * storeData.Count * 3);
            for (int i = 0; i < storeData.Count; i++)
            {
                Prop prop = storeData[i];
                int posY = firstY - gapY * (n++);
                string propStoreInfo = prop.name + "，数量：" + prop.count.ToString() + "，价格：" + prop.price.ToString();
                ObjectFactoryService.createText(scroll, "name", "arial", 18, Color.white, propStoreInfo, 20, posY, 300, 30, TextAnchor.MiddleLeft);
                string propUserInfo = "已持有：" + prop.userCount.ToString();
                ObjectFactoryService.createText(scroll, "name", "arial", 16, Color.white, propUserInfo, 20, posY - 50, 100, 30, TextAnchor.MiddleLeft);

                GameObject gameObject = ObjectFactoryService.createButton(scroll, "name", "arial", 14, "购买", 120, posY - 50, 100, 30);
                Debug.Log(prop.code);
                gameObject.GetComponent<Button>().onClick.AddListener(delegate { StartCoroutine(purchase(prop.code, prop.count)); });
            }
            Debug.Log(storeData.Count * 3);
            dataState = 3;
            if (loadingImg.activeSelf)
            {
                loadingImg.SetActive(false);
            }
        } else
        {

        }
    }

    private IEnumerator purchase(string propCode, int count)
    {
        Debug.Log(propCode);
        String url = "http://" + Global.serverIp + ":" + Global.serverPort + "/userProps?_method=put";
        Dictionary<string, string> headers = new Dictionary<string, string>();
        headers.Add("Content-Type", "application/json");
        headers.Add("sessionId", Global.sessionId);
        headers.Add("userId", Global.userId.ToString());

        Dictionary<string, System.Object> postDataList = new Dictionary<string, System.Object>();
        postDataList.Add("userId", Global.userId);
        postDataList.Add("propCode", propCode);
        postDataList.Add("count", count);
        postDataList.Add("action", 1);

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

    public IEnumerator getPropInfo(int userId)
    {
        String url = "http://" + Global.serverIp + ":" + Global.serverPort + "/props?state=1&page=1&pageLength=10000";

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
        var propResponseDef = new { code = 0, data = new List<Prop>() };
        var propResponseJson = JsonConvert.DeserializeAnonymousType(response, propResponseDef);

        List<Prop> propList;
        int code = propResponseJson.code;
        if (code != 200)
        {
            yield break;
        }

        propList = propResponseJson.data;
        Debug.Log(JsonConvert.SerializeObject(propList));

        url = "http://" + Global.serverIp + ":" + Global.serverPort + "/userProps?userId=" + userId.ToString();

        headers = new Dictionary<string, string>();
        headers.Add("Content-Type", "application/json");
        headers.Add("sessionId", Global.sessionId);
        headers.Add("userId", userId.ToString());

        Debug.Log(JsonConvert.SerializeObject(headers));

        www = new WWW(url, null, headers);
        while (!www.isDone)
        {
            Debug.Log("waiting for response ...");
            yield return null;
        }

        response = www.text;
        Debug.Log(response);
        var userPropResponseDef = new { code = 0, data = new List<UserProp>() };
        var userPropResponseJson = JsonConvert.DeserializeAnonymousType(response, userPropResponseDef);

        List<UserProp> userPropList;
        code = userPropResponseJson.code;
        if (code != 200)
        {
            yield break;
        }
        userPropList = userPropResponseJson.data;

        Debug.Log(propList.Count);
        Debug.Log(JsonConvert.SerializeObject(userPropList));
        foreach (Prop prop in propList)
        {
            foreach (UserProp userProp in userPropList)
            {
                Debug.Log("prop code:" + prop.code);
                Debug.Log("user prop code:" + userProp.propCode);
                if (prop.code.Equals(userProp.propCode))
                {
                    prop.userCount = userProp.count;
                    Debug.Log("count:" + prop.userCount);
                }
            }
        }

        Debug.Log(JsonConvert.SerializeObject(propList));
        this.storeData = propList;
        dataState = 2;
    }
}

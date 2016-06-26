using UnityEngine;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using System;
using System.Collections;
using System.Reflection;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;

public class Auth : MonoBehaviour
{

    GameObject btnObj;

    void Start()
    {
        Type type = Type.GetType("Mono.Runtime");
        if (type != null)
        {
            MethodInfo displayName = type.GetMethod("GetDisplayName", BindingFlags.NonPublic | BindingFlags.Static);
            if (displayName != null)
                Debug.Log(displayName.Invoke(null, null));
        }
        DbService.createDbIfNotExist();
        User user = DbService.getUser();
        if (user != null)
        {
            StartCoroutine(postLogin(user.id, user.password));
            return;
        }
        GameObject btnObj = GameObject.Find("EnterButton");
        Button btn = btnObj.GetComponent<Button>();
        btn.onClick.AddListener(OnClick);
    }

    void Update()
    {
    }

    public void OnClick()
    {
        Debug.Log("begin ...");
        GameObject inputName = GameObject.Find("InputName");
        InputField inputField = inputName.GetComponent<InputField>();
        String name = inputField.text;
        StartCoroutine(postRegister(name));
    }

    public IEnumerator postRegister(String name)
    {
        Debug.Log("enter post");
        String registerUrl = "http://" + Global.serverIp + ":" + Global.serverPort + "/users";
        String postData = "{\"name\":\"" + name + "\"}";

        Dictionary<string, string> headers = new Dictionary<string, string>();
        headers.Add("Content-Type", "application/json");

        Encoding utf8 = Encoding.UTF8;
        WWW www = new WWW(registerUrl, utf8.GetBytes(postData), headers);
        while (!www.isDone)
        {
            Debug.Log("enter is done");
            yield return null;
        }
        Debug.Log("out is done");
        Debug.Log(www.text);

        String response = www.text;
        Debug.Log(response);
        var responseDef = new { code = 0, data = new { id = 0, password = "" } };
        var responseJson = JsonConvert.DeserializeAnonymousType(response, responseDef);
        int code = responseJson.code;
        if (code == 200)
        {
            int id = responseJson.data.id;
            string password = responseJson.data.password;
            Debug.Log(responseJson.data.id + ":" + responseJson.data.password);

            DbService.insertUser(id, password);
            SceneManager.LoadScene("main");
        }
        else
        {
            Debug.Log(responseJson.data.id + ":" + responseJson.data.password);
        }
    }

    public IEnumerator postLogin(int id, String password)
    {
        Debug.Log("enter post login");
        String loginUrl = "http://" + Global.serverIp + ":" + Global.serverPort + "/logins";
        String postData = "{\"id\":\"" + id + "\", \"password\":\"" + password + "\"}";

        Debug.Log("login url:" + loginUrl);
        Debug.Log("post data:" + postData);

        Dictionary<string, string> headers = new Dictionary<string, string>();
        headers.Add("Content-Type", "application/json");

        Encoding utf8 = Encoding.UTF8;
        WWW www = new WWW(loginUrl, utf8.GetBytes(postData), headers);
        while (!www.isDone)
        {
            Debug.Log("http login is running");
            yield return null;
        }
        Debug.Log(www.text);

        String response = www.text;
        Debug.Log(response);
        var responseDef = new { code = 0, data = new { id = 0, sessionId = "" } };
        var responseJson = JsonConvert.DeserializeAnonymousType(response, responseDef);
        int code = responseJson.code;
        string sessionId = responseJson.data.sessionId;
        Global.sessionId = sessionId;

        Debug.Log("global session id:" + Global.sessionId);
        Debug.Log(responseJson.data.id + ":" + responseJson.data.sessionId);

        if (code == 200)
        {
            SceneManager.LoadScene("main");
        }
    }


}

using UnityEngine;
using UnityEngine.UI;
using UnityEngine.SceneManagement;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using Newtonsoft.Json;

public class ActionTest : MonoBehaviour {

	// Use this for initialization
	void Start () {
        
	}
	
	// Update is called once per frame
	void Update () {
        float x = Input.acceleration.x;
        float y = Input.acceleration.y;
        float z = Input.acceleration.z;

        Debug.Log("accelartion:" + x + ", " + y + ", " + z);

        postData(x, y, z);
    }

    void postData(float x, float y, float z)
    {
        String url = "http://192.168.1.2:8205/actionData";

        Dictionary<string, string> headers = new Dictionary<string, string>();
        headers.Add("Content-Type", "application/json");

        Dictionary<string, System.Object> postDataList = new Dictionary<string, System.Object>();
        postDataList.Add("x", x);
        postDataList.Add("y", y);
        postDataList.Add("z", z);
        postDataList.Add("createTime", (Int64)(DateTime.UtcNow.Subtract(new DateTime(1970, 1, 1))).TotalMilliseconds);

        string postDataString = JsonConvert.SerializeObject(postDataList);
        Debug.Log(url);
        Debug.Log(postDataString);
        Debug.Log(headers);
        byte[] postBytes = Encoding.UTF8.GetBytes(postDataString);

        WWW www = new WWW(url, postBytes, headers);

        /*
        while (!www.isDone)
        {
            Debug.Log("waiting for response ..., url:" + www.url);
            if (!string.IsNullOrEmpty(www.error))
                Debug.Log(www.error);
            yield return null;
        }
        if (!string.IsNullOrEmpty(www.error))
            Debug.Log(www.error);
        String response = www.text;
        Debug.Log(response);
        */
    }
}

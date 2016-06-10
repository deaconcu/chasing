using UnityEngine;
using System.Collections.Generic;

public class GameUser {

    public int id { get; set; }

    public float x { get; set; }
    public float y { get; set; }
    public float z { get; set; }

    private Dictionary<int, int> stateMap;

}

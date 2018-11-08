package com.prosper.chasing.game.map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by deacon on 2018/5/3.
 */
@Component
public class MapCreator {

    private Map<String, GameMap> gameMaps = new HashMap<>();

    @Autowired
    private Jedis jedis;

    @PostConstruct
    public void createMap() {
        MarathonGameMapCreator marathonGameMapCreator = new MarathonGameMapCreator();
        GameMap gameMap = marathonGameMapCreator.generate(20, 20, 49);

        gameMaps.put("marathon", gameMap);
        jedis.set("marathon".getBytes(), gameMap.getMapBytes());
    }

    public GameMap getMap(String name) {
        return gameMaps.get(name);
    }

}

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

    private Map<String, MapSkeleton> gameMaps = new HashMap<>();

    @Autowired
    private Jedis jedis;

    @PostConstruct
    public void createMap() {
        MapSkeleton mapSkeleton = new MapSkeleton(30, 100)
                .merge(new MapSkeleton(30, 100))
                .merge(new MapSkeleton(30, 100))
                .merge(new MapSkeleton(30, 100))
                .merge(new MapSkeleton(30, 100));

        mapSkeleton.optimize();
        mapSkeleton.generateTerrain();
        mapSkeleton.toBytes();

        //MarathonGameMapCreator marathonGameMapCreator = new MarathonGameMapCreator();
        //GameMap gameMap = marathonGameMapCreator.generateV2(20, 20, 49);

        gameMaps.put("marathon", mapSkeleton);
        byte[] mapBytes = mapSkeleton.toBytes();
        jedis.set("marathon".getBytes(), mapBytes);
    }

    public MapSkeleton getMap(String name) {
        return gameMaps.get(name);
    }

}

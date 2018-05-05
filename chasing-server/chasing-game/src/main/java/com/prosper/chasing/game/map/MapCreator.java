package com.prosper.chasing.game.map;

import org.springframework.stereotype.Component;

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

    @PostConstruct
    public void createMap() {
        MarathonGameMapCreator marathonGameMapCreator = new MarathonGameMapCreator();
        GameMap gameMap = marathonGameMapCreator.generate(30, 30);

        gameMaps.put("marathon", gameMap);
    }

    public GameMap getMap(String name) {
        return gameMaps.get(name);
    }

}

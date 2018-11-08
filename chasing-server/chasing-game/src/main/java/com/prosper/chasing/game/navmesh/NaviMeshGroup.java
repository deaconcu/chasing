package com.prosper.chasing.game.navmesh;

import com.prosper.chasing.game.base.Point;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by deacon on 2018/3/23.
 */
@Component
public class NaviMeshGroup {

    private Map<String, NaviMesh> navimeshMap = new HashMap<>();

    @PostConstruct
    public void load() throws URISyntaxException, IOException {
        String[] fileNames = new File(getClass().getClassLoader().getResource("navimesh").toURI()).list();
        for (String fileName: fileNames) {
            NaviMesh naviMesh = new NaviMesh();
            naviMesh.load("navimesh/" + fileName);
            navimeshMap.put(fileName.split("\\.")[0], naviMesh);
        }
    }

    public static void main(String[] s) throws IOException, URISyntaxException {
        NaviMeshGroup naviMeshGroup = new NaviMeshGroup();
        naviMeshGroup.load();
    }

    public Point getRandomPositionPoint(String metagameCode) {
        if (!navimeshMap.containsKey(metagameCode)) {
            return null;
        }
        return navimeshMap.get(metagameCode).getRandomPositionPoint();
    }

    public Deque<Point> getPath(String metagameCode, Point start, Point end) {
        if (!navimeshMap.containsKey(metagameCode)) {
            return null;
        }
        return navimeshMap.get(metagameCode).getPath(start, end);
    }
}

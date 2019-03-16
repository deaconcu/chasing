package com.prosper.chasing.game.navmesh;

import com.prosper.chasing.game.base.Point3;
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
public class NavMeshGroup {

    private Map<String, NavMesh> navMeshMap = new HashMap<>();

    @PostConstruct
    public void load() throws URISyntaxException, IOException {
        String[] fileNames = new File(getClass().getClassLoader().getResource("navimesh").toURI()).list();
        for (String fileName: fileNames) {
            NavMesh naviMesh = new NavMesh();
            naviMesh.load("navimesh/" + fileName);
            navMeshMap.put(fileName.split("\\.")[0], naviMesh);
        }
    }

    public static void main(String[] s) throws IOException, URISyntaxException {
        NavMeshGroup navMeshGroup = new NavMeshGroup();
        navMeshGroup.load();
    }

    public Point3 getRandomPositionPoint(String metagameCode) {
        if (!navMeshMap.containsKey(metagameCode)) {
            return null;
        }
        return navMeshMap.get(metagameCode).getRandomPositionPoint();
    }

    public Deque<Point3> getPath(String metagameCode, Point3 start, Point3 end) {
        if (!navMeshMap.containsKey(metagameCode)) {
            return null;
        }
        return navMeshMap.get(metagameCode).getPath(start, end);
    }
}

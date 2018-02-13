package com.prosper.chasing.game.navmesh;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prosper.chasing.game.base.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by deacon on 2018/2/2.
 */
@Component
public class Navimesh {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Random random = new Random();

    private Map<String, List<Triangle>> triangleCellMap =  new HashMap<>();

    public List<Triangle> triangles;

    private enum PointType {
        a, b, c
    }

    private static class Triangle {
        public Point a;
        public Point b;
        public Point c;

        Triangle ab;
        Triangle ac;
        Triangle bc;

        Point vectorAB;
        Point vectorBC;
        Point vectorAC;

        List<String> cellList = new LinkedList<>();

        @Override
        public int hashCode() {
            return a.hashCode() + b.hashCode() + c.hashCode();
        }

        public String toString() {
            return "point a: " + a + ", point b: " + b + ", point c: " + c +
                    ", cell list: " + cellList;
        }

    }

    @PostConstruct
    public void load() throws IOException, URISyntaxException {
        load("navmesh01.json");
    }


    public void load(String file) throws URISyntaxException, IOException {
        // 从文件中读取navmesh
        final ObjectMapper mapper = new ObjectMapper();

        Path path = Paths.get(getClass().getClassLoader().getResource(file).toURI());
        StringBuilder data = new StringBuilder();
        Stream<String> lines = Files.lines(path);
        lines.forEach(line -> data.append(line).append("\n"));
        lines.close();

        System.out.println(data.toString());

        Navimesh navimashJson = mapper.readValue(data.toString(), getClass());
        List<Triangle>  allTriangles = navimashJson.triangles;

        // 计算三角形面的三条边向量
        for (Triangle t: allTriangles) {
            t.vectorAB = new Point(t.b.x - t.a.x, t.b.y - t.a.y, t.b.z - t.a.z);
            t.vectorBC = new Point(t.c.x - t.b.x, t.c.y - t.b.y, t.c.z - t.b.z);
            t.vectorAC = new Point(t.c.x - t.a.x, t.c.y - t.a.y, t.c.z - t.a.z);
        }

        // 获得navmesh共点的三角形面
        int count = 0;
        Map<Point, List<Triangle>> pointMap = new HashMap<>();
        for (Triangle t: allTriangles) {
            if (pointMap.get(t.a) == null) {
                List<Triangle> pointTriangleList = new LinkedList<>();
                pointMap.put(t.a, pointTriangleList);
            }
            pointMap.get(t.a).add(t);
            if (pointMap.get(t.b) == null) {
                List<Triangle> pointTriangleList = new LinkedList<>();
                pointMap.put(t.b, pointTriangleList);
            }
            pointMap.get(t.b).add(t);
            if (pointMap.get(t.c) == null) {
                List<Triangle> pointTriangleList = new LinkedList<>();
                pointMap.put(t.c, pointTriangleList);
            }
            pointMap.get(t.c).add(t);
        }

        // 计算共边的三角形面数据
        for (Triangle t: allTriangles) {
            List<Triangle> pointTriangleList = pointMap.get(t.a);
            for(Triangle pointTriangle: pointTriangleList) {
                if (pointTriangle == t) continue;
                PointType pointType = isNear(t, pointTriangle, PointType.a);
                if (pointType != null) {
                    if (pointType == PointType.b) {
                        t.ab = pointTriangle;
                    } else if (pointType == PointType.c) {
                        t.ac = pointTriangle;
                    }
                }
            }

            pointTriangleList = pointMap.get(t.b);
            for(Triangle pointTriangle: pointTriangleList) {
                if (pointTriangle == t) continue;
                PointType pointType = isNear(t, pointTriangle, PointType.b);
                if (pointType != null) {
                    if (pointType == PointType.a) {
                        t.ab = pointTriangle;
                    } else if (pointType == PointType.c) {
                        t.bc = pointTriangle;
                    }
                }
            }

            pointTriangleList = pointMap.get(t.c);
            for(Triangle pointTriangle: pointTriangleList) {
                if (pointTriangle == t) continue;
                PointType pointType = isNear(t, pointTriangle, PointType.c);
                if (pointType != null) {
                    if (pointType == PointType.a) {
                        t.ac = pointTriangle;
                    } else if (pointType == PointType.b) {
                        t.bc = pointTriangle;
                    }
                }
            }
        }

        // 获取点最多的连通图，设置为游戏可运动区域
        Set<Triangle> triangleSet = new HashSet<>();
        for (Triangle t: allTriangles) {
            triangleSet.add(t);
        }

        List<List<Triangle>> triangleMapList = new LinkedList<>();
        while (!triangleSet.isEmpty()) {
            int total = 0;
            for(List<Triangle> triangleList: triangleMapList) {
                total += triangleList.size();
            }
            Triangle first = (Triangle) triangleSet.toArray()[0];
            List<Triangle> list = new ArrayList<>();
            list.add(first);
            triangleSet.remove(first);
            int i = 0;
            while(i < list.size()) {
                Triangle t = list.get(i++);
                if (t.ab != null && !list.contains(t.ab))  {
                    list.add(t.ab);
                    triangleSet.remove(t.ab);
                }
                if (t.ac != null && !list.contains(t.ac))  {
                    list.add(t.ac);
                    triangleSet.remove(t.ac);
                }
                if (t.bc != null && !list.contains(t.bc))  {
                    list.add(t.bc);
                    triangleSet.remove(t.bc);
                }
            }
            triangleMapList.add(list);
        }

        int max = 0;
        for(List<Triangle> triangleList: triangleMapList) {
            if (triangleList.size() > max) {
                this.triangles = triangleList;
                max = triangleList.size();
            }
        }

        // 将三角形面分散到cell中
        loadCellMap();
    }

    private enum BoundaryType {min, max}
    private enum Axis {x, z}

    /**
     * 将三角形分散到cell中, cell用左下角x/10, y/10坐标来标识
     */
    private void loadCellMap() {
        for (Triangle triangle: triangles) {
            int minX = (int) Math.floor(
                    (double)getBoundary(triangle.a, triangle.b, triangle.c, BoundaryType.min, Axis.x) / 10000);
            int maxX = (int) Math.ceil(
                    (double)getBoundary(triangle.a, triangle.b, triangle.c, BoundaryType.max, Axis.x) / 10000);

            int minZ = (int) Math.floor(
                    (double)getBoundary(triangle.a, triangle.b, triangle.c, BoundaryType.min, Axis.z) / 10000);
            int maxZ = (int) Math.ceil(
                    (double)getBoundary(triangle.a, triangle.b, triangle.c, BoundaryType.max, Axis.z) / 10000);

            for (int x = minX; x < maxX; x ++) {
                for (int z = minZ; z < maxZ; z ++) {
                    String cellId = getCellIdByAxis(x, z);
                    if (!triangleCellMap.containsKey(cellId)) {
                        triangleCellMap.put(cellId, new ArrayList<>());
                    }
                    triangleCellMap.get(cellId).add(triangle);
                    triangle.cellList.add(cellId);
                }
            }
        }
    }

    private int getBoundary(Point a, Point b, Point c, BoundaryType type, Axis axis) {
        if (type == BoundaryType.max) {
            if (axis == Axis.x) {
                return max(a.x, b.x, c.x);
            } else {
                return max(a.z, b.z, c.z);
            }
        } else {
            if (axis == Axis.x) {
                return min(a.x, b.x, c.x);
            } else {
                return min(a.z, b.z, c.z);
            }
        }
    }

    private int max(int i1, int i2, int i3) {
        int temp = i1 > i2 ? i1 : i2;
        if (i3 > temp) {
            return i3;
        }
        return temp;
    }

    private int min(int i1, int i2, int i3) {
        int temp = i1 < i2 ? i1 : i2;
        if (i3 < temp) {
            return i3;
        }
        return temp;
    }

    /**
     * 参数x, y 为(int)实际坐标 / 10, 或者转换后的坐标 / 10000
     *
     */
    private String getCellIdByAxis(int x, int z) {
        return Integer.toString(x) + "/" + Integer.toString(z);
    }

    /**
     * 通过一个点获得这个点所在的三角形面
     * 参数x, y为 实际坐标值 * 1000
     * https://stackoverflow.com/questions/2049582/how-to-determine-if-a-point-is-in-a-2d-triangle
     */
    private Triangle getTriangleByAxis(int x, int z) {
        List<Triangle> triangleList = triangleCellMap.get(
                getCellIdByAxis((int)Math.floor((float)x /10000), (int)Math.floor((float)z / 10000)));
        for (Triangle triangle : triangleList) {
            int as_x = x - triangle.a.x;
            int as_z = z - triangle.a.z;

            boolean s_ab = (triangle.b.x - triangle.a.x) * as_z - (triangle.b.z - triangle.a.z)*as_x > 0;
            if((triangle.c.x - triangle.a.x) * as_z - (triangle.c.z - triangle.a.z)*as_x > 0 == s_ab) continue;
            if((triangle.c.x - triangle.b.x) * (z - triangle.b.z) - (triangle.c.z - triangle.b.z) * (x - triangle.b.x)
                    > 0 != s_ab)
                continue;

            return triangle;
        }
        return null;
    }

    private PointType isNear(Triangle t, Triangle pointTriangle, PointType pointType) {
        if (pointType == PointType.a) {
            if (t.b.equals(pointTriangle.a) ||  t.b.equals(pointTriangle.b) || t.b.equals(pointTriangle.c)) {
                return PointType.b;
            }
            if (t.c.equals(pointTriangle.a) ||  t.c.equals(pointTriangle.b) || t.c.equals(pointTriangle.c)) {
                return PointType.c;
            }
        }

        if (pointType == PointType.b) {
            if (t.a.equals(pointTriangle.a) ||  t.a.equals(pointTriangle.b) || t.a.equals(pointTriangle.c)) {
                return PointType.a;
            }
            if (t.c.equals(pointTriangle.a) ||  t.c.equals(pointTriangle.b) || t.c.equals(pointTriangle.c)) {
                return PointType.c;
            }
        }

        if (pointType == PointType.c) {
            if (t.a.equals(pointTriangle.a) ||  t.a.equals(pointTriangle.b) || t.a.equals(pointTriangle.c)) {
                return PointType.a;
            }
            if (t.b.equals(pointTriangle.a) ||  t.b.equals(pointTriangle.b) || t.b.equals(pointTriangle.c)) {
                return PointType.b;
            }
        }
        return null;
    }

    /**
     * 通过当前位置获得一条随机路径
     * 参考：https://adamswaab.wordpress.com/2009/12/11/random-point-in-a-triangle-barycentric-coordinates/
     */
    public Deque<Point> getPath(Point point, int minDistance) {
        Triangle triangle = getTriangleByAxis(point.x, point.z);

        int distance = 0;
        int count = 0;

        LinkedList<Point> pointList = new LinkedList<>();
        Triangle nextTriangle;
        Point nextPoint;
        List<Integer> triangleListForRandom = new LinkedList<>();
        while (distance < minDistance && count < 100) {
            if (triangle.ab != null) {
                triangleListForRandom.add(0);
            }
            if (triangle.bc != null) {
                triangleListForRandom.add(1);
            }
            if (triangle.ac != null) {
                triangleListForRandom.add(2);
            }
            Collections.shuffle(triangleListForRandom);
            int i = triangleListForRandom.get(0);

            if (i == 0) {
                nextTriangle = triangle.ab;
                nextPoint = triangle.a.add(triangle.vectorAB, random.nextDouble());
            } else if (i == 1) {
                nextTriangle = triangle.bc;
                nextPoint = triangle.b.add(triangle.vectorBC, random.nextDouble());
            } else {
                nextTriangle = triangle.ac;
                nextPoint = triangle.c.add(triangle.vectorAC, random.nextDouble());
            }

            distance += nextPoint.distance(point);
            pointList.add(nextPoint);
            count ++;

            point = nextPoint;
            triangle = nextTriangle;
            triangleListForRandom.clear();
        }

        if (count >= 100) {
            log.warn("can't reach distance after 100 times");
        }

        nextPoint = getRandomPositionInTriangle(triangle);
        pointList.add(nextPoint);

        return pointList;
    }

    public Point getRandomPosition() {
        Triangle triangle = triangles.get(random.nextInt(triangles.size()));
        return getRandomPositionInTriangle(triangle);
    }

    private Point getRandomPositionInTriangle(Triangle triangle) {
        double r = random.nextDouble();
        double s = random.nextDouble();

        if ((r + s) >= 1) {
            r = 1 - r;
            s = 1 - s;
        }
        return triangle.a.add(triangle.vectorAB, r).add(triangle.vectorAC, s);
    }

    public static void main(String[] s) throws IOException, URISyntaxException {
        Navimesh navmesh = new Navimesh();
        navmesh.load("navmesh01.json");

        for(int i = 0; i < 100; i ++) {
            System.out.println("try times: " + i);

            Point point = navmesh.getRandomPosition();
            System.out.println("point: " + point);
            Deque<Point> path = navmesh.getPath(point, 10000);
            System.out.println("path: " + path);
        }
    }



}

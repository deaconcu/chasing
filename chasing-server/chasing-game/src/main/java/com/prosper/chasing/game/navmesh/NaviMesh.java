package com.prosper.chasing.game.navmesh;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class NaviMesh {

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

        public Point center;

        Triangle ab;
        Triangle ac;
        Triangle bc;

        int distanceAB;
        int distanceAC;
        int distanceBC;

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

        NaviMesh navimashJson = mapper.readValue(data.toString(), getClass());
        List<Triangle>  allTriangles = navimashJson.triangles;

        // 计算中心点
        for(Triangle t: allTriangles) {
            t.center = new Point(
                    t.a.x + t.b.x + t.c.x / 3,
                    t.a.y + t.b.y + t.c.y / 3,
                    t.a.z + t.b.z + t.c.z / 3
            );
        }

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

        // 计算周边三角形面的距离，中心点到中心点
        for (Triangle t: allTriangles) {
            if (t.ab != null) {
                t.distanceAB = t.center.distance(t.ab.center);
            }
            if (t.bc != null) {
                t.distanceBC = t.center.distance(t.bc.center);
            }
            if (t.ac != null) {
                t.distanceAC = t.center.distance(t.ac.center);
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

    private static class PathTriangle {
        Triangle self;
        Triangle parent;
        int f;

        PathTriangle(Triangle self, Triangle parent, int f) {
            this.self = self;
            this.parent = parent;
            this.f = f;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof PathTriangle)) {
                return false;
            }
            return self.equals(((PathTriangle)object).self);
        }

        @Override
        public int hashCode() {
            return self.hashCode();
        }
    }

    private static class PathInfo {
        int f;
        int g;
        Triangle parent;

        PathInfo(int f, int g, Triangle parent) {
            this.f = f;
            this.g = g;
            this.parent = parent;
        }
    }

    public Deque<Point> getPath(Point start, Point end) {
        LinkedList<Point> pointList = new LinkedList<>();

        Map<Triangle, PathInfo> pathInfoMap = new HashMap<>();
        Set<Triangle> openSet = new HashSet<>();
        Set<Triangle> closeSet = new HashSet<>();

        Triangle startTri = getTriangleByAxis(start.x, start.z);
        Triangle endTri = getTriangleByAxis(end.x, end.z);

        if (startTri == endTri) {
            pointList.add(start);
            pointList.add(end);
            return pointList;
        }

        PathTriangle parent = null;
        Triangle current = startTri;
        pathInfoMap.put(current, new PathInfo(
                0 + computerF(current, 0, end, pathInfoMap),
                0 + computerG(current, 0, pathInfoMap),
                null));
        closeSet.add(current);
        while(true) {
            if (current.ab != null && !closeSet.contains(current.ab)) {
                int f = computerF(current.ab, current.distanceAB, end, pathInfoMap);
                int g = computerG(current.ab, current.distanceAB, pathInfoMap);
                if (!openSet.contains(current.ab) || f < pathInfoMap.get(current.ab).f) {
                    openSet.add(current.ab);
                    pathInfoMap.put(current.ab, new PathInfo(f, g, current));
                }
            }
            if (current.bc != null && !closeSet.contains(current.bc)) {
                int f = computerF(current.bc, current.distanceBC, end, pathInfoMap);
                int g = computerG(current.bc, current.distanceBC, pathInfoMap);
                if (!openSet.contains(current.bc) || f < pathInfoMap.get(current.bc).f) {
                    openSet.add(current.bc);
                    pathInfoMap.put(current.bc, new PathInfo(f, g, current));
                }
            }
            if (current.ac != null && !closeSet.contains(current.ac)) {
                int f = computerF(current.ac, current.distanceAC, end, pathInfoMap);
                int g = computerG(current.ac, current.distanceAC, pathInfoMap);
                if (!openSet.contains(current.ac) || f < pathInfoMap.get(current.ac).f) {
                    openSet.add(current.ac);
                    pathInfoMap.put(current.ac, new PathInfo(f, g, current));
                }
            }

            if (openSet.size() == 0 || openSet.contains(endTri)) {
                break;
            }

            openSet.remove(current);
            closeSet.add(current);
            current = getSmallestTriangle(openSet, pathInfoMap);
        }

        // 如果是因为没有了节点结束了循环，也就是没有找到终结点，返回空列表
        if (openSet.size() == 0) {
            return pointList;
        }

        LinkedList<Triangle> triPathList = new LinkedList<>();
        triPathList.add(endTri);
        Triangle parentTri = pathInfoMap.get(endTri).parent;
        while (parentTri != null) {
            triPathList.add(parentTri);
            parentTri = pathInfoMap.get(parentTri).parent;
        }

        triPathList.pollLast();
        pointList.add(start);
        while (true) {
            Triangle currentTri = triPathList.pollLast();
            if (currentTri == endTri) {
                break;
            }
            pointList.add(getRandomPositionInTriangle(currentTri));
        }
        pointList.add(end);
        return pointList;
    }

    private Triangle getSmallestTriangle(Set<Triangle> openSet, Map<Triangle, PathInfo> pathInfoMap) {
        int smallestF = Integer.MAX_VALUE;
        Triangle smallestTri = null;
        for (Triangle triangle: openSet) {
            if (pathInfoMap.get(triangle).f < smallestF) {
                smallestTri = triangle;
            }
        }
        return smallestTri;
    }

    /**
     * 计算g值
     */
    private int computerG(Triangle currentTri, int distance, Map<Triangle, PathInfo> pathInfo) {
        if (pathInfo.get(currentTri) == null || pathInfo.get(currentTri).parent == null) {
            return 0;
        }

        Triangle parent = pathInfo.get(currentTri).parent;
        if (pathInfo.get(parent) == null) throw new RuntimeException("parent path info not found");

        return pathInfo.get(parent).g + distance;
    }

    /**
     * 计算F值
     */
    private int computerF(Triangle currentTri, int distance, Point endPoint, Map<Triangle, PathInfo> pathInfo) {
        return computerG(currentTri, distance, pathInfo) + currentTri.center.distance(endPoint);
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

    public Point getRandomPositionPoint() {
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
        NaviMesh navmesh = new NaviMesh();
        navmesh.load("navimesh/king.json");


        for(int i = 0; i < 100; i ++) {
            System.out.println("try times: " + i);

            Point start = navmesh.getRandomPositionPoint();
            Point end = navmesh.getRandomPositionPoint();
            System.out.println("start: " + start + ", end: " + end);
            Deque<Point> path = navmesh.getPath(start, end);
            System.out.println("path: " + path);

            System.out.println("head: " + path.peek());
        }
    }



}

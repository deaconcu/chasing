package com.prosper.chasing.game.util;

import java.util.*;

/**
 * Created by deacon on 2019/2/13.
 */
public class Graph {

    private static final int UNREACHABLE_DISTANCE = 100000000;

    private Map<Integer, Integer> vertexIndexMap;
    private int[] vertices;
    private int[][] edges;
    private int[][] edgeImportance;

    public Graph(int[] vertices) {
        this.vertices = vertices;
        this.edges = new int[vertices.length][vertices.length];
        this.edgeImportance = new int[vertices.length][vertices.length];

        for (int i = 0; i < vertices.length; i ++) {
            for (int j = 0; j < vertices.length; j ++) {
                if (i != j) edges[i][j] = -1;
                else edges[i][j] = 0;
            }
        }

        vertexIndexMap = new HashMap<>();
        int index = 0;
        for (int vertexId: vertices) {
            vertexIndexMap.put(vertexId, index ++);
        }
    }

    public Graph(int[] vertices, int[][] edge) {
        this(vertices);
        this.edges = edge;
    }

    public void setEdge(int vertexA, int vertexB, int weight) {
        int vertexAIndex = vertexIndexMap.get(vertexA);
        int vertexBIndex = vertexIndexMap.get(vertexB);
        if (edges[vertexAIndex][vertexBIndex] == -1 || weight < edges[vertexAIndex][vertexBIndex]) {
            edges[vertexAIndex][vertexBIndex] = weight;
            edges[vertexBIndex][vertexAIndex] = weight;
        }
    }

    public Map<Integer, Map<Integer, List<Integer>>> countPath() {
        Map<Integer, Map<Integer, List<Integer>>> pathMap = new HashMap<>();
        for (int vertexId : vertices) {
            pathMap.put(vertexId, countPathWithVertexId(vertexId));
        }
        return pathMap;
    }

    public Map<Integer, List<Integer>> countPathWithVertexId(int vertexId) {
        int index = vertexIndexMap.get(vertexId);
        int[] path = countPath(index);

        Map<Integer, List<Integer>> pathMap = new HashMap<>();

        for (int i = 0; i < path.length; i ++) {
            if (index == i) continue;
            List<Integer> pathList = new ArrayList<>();
            pathList.add(vertices[i]);
            int previousPathIndex = path[i];
            while(true) {
                if (previousPathIndex == -1) break;
                pathList.add(vertices[previousPathIndex]);
                previousPathIndex = path[previousPathIndex];
            }

            Collections.reverse(pathList);
            pathMap.put(vertices[i], pathList);
        }
        return pathMap;
    }

    public int countDetourDistance(int vertexIdA, int vertexIdB) {
        int indexA = vertexIndexMap.get(vertexIdA);
        int indexB = vertexIndexMap.get(vertexIdB);

        int distance = edges[indexA][indexB];
        edges[indexA][indexB] = -1;
        int importance = countDistance(indexA, indexB);

        edges[indexA][indexB] = distance;
        return importance;
    }

    /**
     * 计算给定顶点到其余各点的最短路径
     * @param index 给定顶点的下标
     * @return 顶点到其余各点的最短路径数组 int[i] 表示顶点i到给定顶点最短路径上的前一个顶点下标
     */
    public int[] countPath(int index) {
        boolean[] aSet = new boolean[vertices.length];
        int[] dSet = new int[vertices.length];
        int[] path = new int[vertices.length];

        for (int i = 0; i < aSet.length; i ++) {
            aSet[i] = false;
            dSet[i] = -1;
            path[i] = -1;
        }

        aSet[index] = true;
        for (int i = 0; i < aSet.length; i ++) {
            if (i == index) dSet[i] = 0;
            else if (edges[index][i] > 0) {
                dSet[i] = edges[index][i];
                path[i] = index;
            }
        }

        while (true) {
            int minDistance = Integer.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i < dSet.length; i ++) {
                if (aSet[i] || dSet[i] < 0) continue;
                if (dSet[i] < minDistance) {
                    minDistance = dSet[i];
                    minIndex = i;
                }
            }
            if (minIndex == -1) break;

            aSet[minIndex] = true;
            for (int i = 0; i < aSet.length; i ++) {
                if (edges[minIndex][i] > 0) {
                    int distance = edges[minIndex][i] + dSet[minIndex];
                    if (dSet[i] == -1 || dSet[i] > distance) {
                        dSet[i] = distance;
                        path[i] = minIndex;
                    }
                }
            }
        }
        return path;
    }

    public Map<Integer, Integer> countDistanceWithVertexId(int vertexId) {
        int[] distances = countDistance(vertexIndexMap.get(vertexId));

        Map<Integer, Integer> distanceMap = new HashMap<>();
        for (int i = 0; i < vertices.length; i ++) {
            distanceMap.put(vertices[i], distances[i]);
        }
        return distanceMap;
    }

    public int countDistance(int indexA, int indexB) {
        int[] distances = countDistance(indexA);
        return distances[indexB] >= UNREACHABLE_DISTANCE ? -1 : distances[indexB];
    }

    public int[] countDistance(int indexA) {
        boolean[] aSet = new boolean[vertices.length];
        int[] dSet = new int[vertices.length];

        for (int i = 0; i < aSet.length; i ++) {
            aSet[i] = false;
            dSet[i] = -1;
        }

        aSet[indexA] = true;
        for (int i = 0; i < aSet.length; i ++) {
            if (i == indexA) dSet[i] = 0;
            else if (edges[indexA][i] > 0) dSet[i] = edges[indexA][i];
        }
        //System.out.println(Arrays.toString(aSet) + ", " Arrays.toString(dSet);

        while (true) {
            int minDistance = Integer.MAX_VALUE;
            int minIndex = -1;
            for (int i = 0; i < dSet.length; i ++) {
                if (aSet[i] || dSet[i] <= 0) continue;
                if (dSet[i] < minDistance) {
                    minDistance = dSet[i];
                    minIndex = i;
                }
            }
            if (minIndex == -1) break;

            aSet[minIndex] = true;
            for (int i = 0; i < aSet.length; i ++) {
                if (edges[minIndex][i] > 0) {
                    int distance = edges[minIndex][i] + dSet[minIndex];
                    if (dSet[i] == -1 || dSet[i] > distance) dSet[i] = distance;
                }
            }
            //if (minIndex == indexB) break;
            //System.out.println(Arrays.toString(aSet) + ", " Arrays.toString(dSet);
        }
        return dSet;
    }

    public static void main(String... args) {
        Graph graph = new Graph(
                new int[]{1,2,3,4,5,6,7,8,9},
                new int[][]{
                    {0, 4, 0, 0, 0, 0, 0, 8, 0},
                    {4, 0, 8, 0, 0, 0, 0, 1, 0},
                    {0, 8, 0, 7, 0, 4, 0, 0, 2},
                    {0, 0, 7, 0, 9, 14, 0, 0, 0},
                    {0, 0, 0, 9, 0, 10, 0, 0, 0},
                    {0, 0, 4, 0, 10, 0, 2, 0, 0},
                    {0, 0, 0, 14, 0, 2, 0, 1, 6},
                    {8, 1, 0, 0, 0, 0, 1, 0, 7},
                    {0, 0, 2, 0, 0, 0, 6, 7, 0}
                });

        graph.countDistance(0, 1);
        Map<Integer, Map<Integer, List<Integer>>> pathMap = graph.countPath();
        System.out.print(pathMap);
    }
}

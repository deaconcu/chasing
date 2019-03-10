package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums.HexagonDirection;
import com.prosper.chasing.game.util.Graph;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPOutputStream;

/**
 * Created by deacon on 2019/3/5.
 */
public class MapSkeleton {

    // 已使用的路径块
    public Map<Integer, Hexagon> occupiedMap;
    // 空闲的空白块
    protected Map<Integer, Hexagon> freeMap;

    public int start;

    public int end;

    public int bound;

    public Set<Hexagon> vertexSet = new HashSet<>();
    public Set<Branch> branchSet = new HashSet<>();
    public Map<Integer, Integer> distancesToEnd = new HashMap<>();
    public Map<Integer, Map<Integer, List<Integer>>> pathMap;
    public Map<Integer, Segment> segmentMap = new HashMap<>();

    public static class TreeNode {
        int hexagonId;
        List<TreeNode> children;
        TreeNode parents;
        boolean onPath;

        public TreeNode(int hexagonId, TreeNode parents) {
            this.hexagonId = hexagonId;
            this.parents = parents;
            children = new LinkedList<>();
            onPath = false;
        }

        public void addChild(int childHexagonId) {
            if (childHexagonId == -1 || (parents != null && childHexagonId == parents.hexagonId)) return;
            children.add(new TreeNode(childHexagonId, this));
        }

        @Override
        public String toString() {
            String childString = "";
            for (TreeNode treeNode: children) {
                childString += treeNode.hexagonId + ",";
            }
            return "block id:" + hexagonId + ", child: [" + childString + "]";
        }
    }

    private MapSkeleton(int bound) {
        if (bound % 2 != 0) throw new RuntimeException("map bound must be even");
        this.bound = bound;
        this.start = 1;
        this.end = bound * bound;

        init();
    }

    public MapSkeleton(int bound, int length) {
        if (bound % 2 != 0) throw new RuntimeException("map bound must be even");
        this.bound = bound;
        this.start = 1;
        this.end = bound * bound;

        int fillCount = 1;
        int pruningCount = 1;
        while (true) {
            while (true) {
                init();
                fill();
                System.out.println("fill: " + fillCount);
                fillCount ++;
                if (isContainStartEnd()) {
                    break;
                }
            }
            pruning(start, end);
            System.out.println("pruning: " + pruningCount);
            pruningCount ++;
            if (length() > length) {
                break;
            }
        }
    }

    private void fill() {
        Hexagon hexagon = randomUnusedHexagon();
        addBlock(hexagon, null);

        LinkedList<Hexagon> pathBlockIdList = new LinkedList<>();
        pathBlockIdList.add(hexagon);
        while (true) {
            Hexagon nextFreeHexagon = getRandomFreeSibling(hexagon);
            while (nextFreeHexagon == null && pathBlockIdList.size() > 0) {
                hexagon = pathBlockIdList.get(ThreadLocalRandom.current().nextInt(pathBlockIdList.size()));

                nextFreeHexagon = getRandomFreeSibling(hexagon);
                if (nextFreeHexagon == null) {
                    pathBlockIdList.remove(hexagon);
                }
            }
            if (pathBlockIdList.size() == 0) break;

            addBlock(nextFreeHexagon, hexagon);
            hexagon = nextFreeHexagon;
            if (!pathBlockIdList.contains(hexagon)) {
                pathBlockIdList.add(hexagon);
            }
        }
    }

    private void pruning(int startHexagonId, int endHexagonId) {
        TreeNode startNode = new TreeNode(startHexagonId, null);
        TreeNode endNode = null;
        TreeNode currentNode = startNode;
        LinkedList<TreeNode> treeNodeList = new LinkedList<>();
        while(true) {
            if (currentNode == null) break;
            if (currentNode.hexagonId == endHexagonId) endNode = currentNode;

            List<Hexagon> siblings = getRoadSibling(occupiedMap.get(currentNode.hexagonId));
            for (Hexagon hexagon: siblings) {
                currentNode.addChild(hexagon.getId());
            }

            for (TreeNode childNode: currentNode.children) {
                treeNodeList.add(childNode);
            }
            currentNode = treeNodeList.pollFirst();
        }

        currentNode = endNode;
        while (currentNode != null) {
            currentNode.onPath = true;
            currentNode = currentNode.parents;
        }

        currentNode = startNode;
        treeNodeList.clear();
        while(true) {
            if (currentNode == null) break;
            Hexagon hexagon = occupiedMap.get(currentNode.hexagonId);
            if (!currentNode.onPath) {
                occupiedMap.remove(hexagon.getId());
                freeMap.put(hexagon.getId(), hexagon);
            }
            hexagon.clear();

            for (TreeNode childNode: currentNode.children) {
                treeNodeList.add(childNode);
            }
            currentNode = treeNodeList.pollFirst();
        }

        currentNode = endNode;
        while (currentNode != null) {
            Hexagon hexagon = occupiedMap.get(currentNode.hexagonId);
            if (currentNode.parents != null) {
                 Hexagon parentHexagon = occupiedMap.get(currentNode.parents.hexagonId);
                 hexagon.setBridges(hexagon.getDirection(parentHexagon), true);
                 parentHexagon.setBridges(parentHexagon.getDirection(hexagon), true);
            }
            currentNode = currentNode.parents;
        }
    }

    public MapSkeleton merge(MapSkeleton mapSkeleton) {
        for (Hexagon hexagon: mapSkeleton.occupiedMap.values()) {
            if (occupiedMap.containsKey(hexagon.getId())) {
                occupiedMap.get(hexagon.getId()).merge(hexagon);
            } else {
                occupiedMap.put(hexagon.getId(), hexagon);
            }
        }
        return this;
    }

    private void removeBranch(Branch branch) {
        if (branch.distance() > 1) {
            for (Hexagon hexagon: branch.hexagonList) {
                removeBlock(hexagon, false);
            }
        } else {
            branch.head.setBridges(branch.head.getDirection(branch.tail), false);
            branch.tail.setBridges(branch.tail.getDirection(branch.head), false);
        }
    }

    protected void removeBlock(Hexagon hexagon, boolean partlyBridge) {
        if (hexagon == null) return;
        if (partlyBridge) {
            // TODO
        } else {
            for (Hexagon sibling: getRoadSibling(hexagon)) {
                sibling.setBridges(sibling.getDirection(hexagon), false);
            }
        }

        occupiedMap.remove(hexagon.getId());
        hexagon.clear();
        freeMap.put(hexagon.getId(), hexagon);
    }

    public void optimize() {
        while (true) {
            boolean branchRemoved = false;
            build();

            for (Branch branch: branchSet) {
                if (branch.head.bridgeCount() < 3 || branch.tail.bridgeCount() < 3) continue;
                else if (branch.distance() < 6 && branch.detourDistance > 0 &&
                        Math.abs(branch.detourDistance - branch.distance()) < 5) {
                    removeBranch(branch);
                    branchRemoved = true;
                    System.out.println("branch removed, " + branch);
                    break;
                }
            }
            if (!branchRemoved) break;
        }

        doSegment();
    }

    private void doSegment() {
        for (Hexagon hexagon: occupiedMap.values()) {
            for (Hexagon sibling: getRoadSibling(hexagon)) {
                if (hexagon.getId() > sibling.getId()) continue;
                int id = hexagon.getId() * 10000 + sibling.getId();
                segmentMap.put(id, new Segment(hexagon, sibling));
            }
        }
    }

    protected void addBlock(Hexagon hexagon, Hexagon fromHexagon) {
        if (hexagon == null) return;

        if (fromHexagon != null) {
            hexagon.setBridges(hexagon.getDirection(fromHexagon), true);
            fromHexagon.setBridges(fromHexagon.getDirection(hexagon), true);
        }

        if (occupiedMap.containsKey(hexagon)) return;

        occupiedMap.put(hexagon.getId(), hexagon);
        freeMap.remove(hexagon.getId());

        if (fromHexagon != null) {
            //System.out.println("add Block: [" + hexagon.getId() + ", " + fromHexagon.getId() + "]");
        } else {
            //System.out.println("add Block: [" + hexagon.getId() + ", null]");
        }
    }

    public void build() {
        trimAndCalculate(1);
        trimAndCalculate(2);
    }

    /**
     * 删除不需要的branch
     * 1:起点和终点相同的
     * 2:起点和终点间有多条路线，保留最长的那条，其余删除
     *
     * @param step 1:trim 2:calculate
     */
    public void trimAndCalculate(int step) {
        // get vertex set
        vertexSet.clear();
        branchSet.clear();

        vertexSet.add(occupiedMap.get(start));
        vertexSet.add(occupiedMap.get(end));

        for (Hexagon hexagon: occupiedMap.values()) {
            if (hexagon.bridgeCount() > 2) {
                vertexSet.add(hexagon);
            }
        }

        System.out.println("vertex set count: " + vertexSet.size());
        for (Hexagon hexagon: vertexSet) {
            System.out.println("vertex - " + hexagon.getId() + ": [" + hexagon.getX() + "," + hexagon.getY() + "]");
        }

        // get edge set
        for (Hexagon hexagon: vertexSet) {
            for (Hexagon current : getOccupiedSibling(hexagon)) {
                if (vertexSet.contains(current)) {
                    if (hexagon.getId() < current.getId()) {
                        addBranch(new Branch(hexagon, current));
                    }
                } else {
                    List<Hexagon> blockList = new LinkedList<>();
                    blockList.add(current);

                    boolean reachEnd = false;
                    while (!reachEnd) {
                        for (Hexagon sibling : getOccupiedSibling(current)) {
                            if (blockList.contains(sibling)) {
                                continue;
                            }
                            else if (blockList.size() == 1 && sibling.getId() == hexagon.getId()) {
                                continue;
                            }
                            else if (vertexSet.contains(sibling)) {
                                if (hexagon.getId() <= sibling.getId()) {
                                    Branch branch = new Branch(hexagon, sibling);
                                    branch.hexagonList = blockList;
                                    addBranch(branch);
                                }
                                reachEnd = true;
                            } else {
                                blockList.add(sibling);
                                current = sibling;
                                break;
                            }
                        }
                    }
                }
            }
        }

        int[] blockIds = new int[vertexSet.size()];
        int index = 0;
        for (Hexagon hexagon: vertexSet) {
            blockIds[index++] = hexagon.getId();
        }

        if (step > 1) {
            Graph graph = new Graph(blockIds);
            for (Branch branch : branchSet) {
                graph.setEdge(branch.head.getId(), branch.tail.getId(), branch.hexagonList.size() + 1);
            }

            for (Branch branch : branchSet) {
                int detourDistance = graph.countDetourDistance(branch.head.getId(), branch.tail.getId());
                branch.detourDistance = detourDistance;
            }

            distancesToEnd = graph.countDistanceWithVertexId(end);
            pathMap = graph.countPath();

            // 按branch长度排序后打印, 用来调试，可以注释
            Branch[] branches = new Branch[branchSet.size()];
            int i = 0;
            for (Branch branch : branchSet) {
                int m = i;
                for (int j = 0; j < i; j ++) {
                    if (branch.hexagonList.size() >= branches[j].hexagonList.size()) continue;
                    else {
                        m = j;
                        break;
                    }
                }
                for (int k = i; k > m; k --) {
                    branches[k] = branches[k - 1];
                }
                branches[m] = branch;
                i ++;
            }

            for (Branch branch : branches) {
                System.out.println("branch: " + branch);
            }
        }
    }

    private void addBranch(Branch branch) {
        if (branch.head == branch.tail) {
            removeBranch(branch);
            return;
        }

        for(Branch existBranch: branchSet) {
            if (existBranch.head.getId() == branch.head.getId() &&
                    existBranch.tail.getId() == branch.tail.getId()) {
                if (existBranch.distance() > branch.distance()) {
                    removeBranch(branch);
                } else {
                    removeBranch(existBranch);
                    branchSet.add(branch);
                }
                return;
            }
        }
        branchSet.add(branch);
    }

    private int getHexagonIdByDirection(Hexagon hexagon, HexagonDirection direction) {
        int x = hexagon.getX();
        int y = hexagon.getY();
        if (hexagon.getY() % 2 == 0) {
            if (direction == HexagonDirection.RIGHT) {
                if (isInBound(x + 1, y)) return getHexagonId(x + 1, y);
            } else if (direction == HexagonDirection.DOWN_RIGHT) {
                if (isInBound(x + 1, y - 1)) return getHexagonId(x + 1, y - 1);
            } else if (direction == HexagonDirection.DOWN_LEFT) {
                if (isInBound(x, y - 1)) return getHexagonId(x, y - 1);
            } else if (direction == HexagonDirection.LEFT) {
                if (isInBound(x - 1, y)) return getHexagonId(x - 1, y);
            } else if (direction == HexagonDirection.UP_LEFT) {
                if (isInBound(x, y + 1)) return getHexagonId(x, y + 1);
            } else if (direction == HexagonDirection.UP_RIGHT) {
                if (isInBound(x + 1, y + 1)) return getHexagonId(x + 1, y + 1);
            }
        } else {
            if (direction == HexagonDirection.RIGHT) {
                if (isInBound(x + 1, y)) return getHexagonId(x + 1, y);
            } else if (direction == HexagonDirection.DOWN_RIGHT) {
                if (isInBound(x, y - 1)) return getHexagonId(x, y - 1);
            } else if (direction == HexagonDirection.DOWN_LEFT) {
                if (isInBound(x - 1, y - 1)) return getHexagonId(x - 1, y - 1);
            } else if (direction == HexagonDirection.LEFT) {
                if (isInBound(x - 1, y)) return getHexagonId(x - 1, y);
            } else if (direction == HexagonDirection.UP_LEFT) {
                if (isInBound(x - 1, y + 1)) return getHexagonId(x - 1, y + 1);
            } else if (direction == HexagonDirection.UP_RIGHT) {
                if (isInBound(x, y + 1)) return getHexagonId(x, y + 1);
            }
        }
        return -1;
    }

    private Hexagon getOccupiedSibling(Hexagon hexagon, HexagonDirection direction) {
        int hexagonId = getHexagonIdByDirection(hexagon, direction);
        return occupiedMap.get(hexagonId);
    }

    private Hexagon[] getOccupiedSibling(Hexagon hexagon) {
        Hexagon[] hexagons = new Hexagon[hexagon.bridgeCount()];
        int index = 0;
        for (HexagonDirection direction: hexagon.getRoadDirection()) {
            int hexagonId = getHexagonIdByDirection(hexagon, direction);
            hexagons[index ++] = occupiedMap.get(hexagonId);
        }
        return hexagons;
    }

    private Hexagon getRandomFreeSibling(Hexagon hexagon) {
        Hexagon[] siblings = new Hexagon[6];
        int siblingCount = 0;

        //for (HexagonDirection direction: HexagonDirection.values()) {
        for (HexagonDirection direction: hexagon.getPossibleRoadDirection()) {
            if (direction == null || direction == HexagonDirection.FREE) continue;
            int hexagonId = getHexagonIdByDirection(hexagon, direction);
            if (hexagonId != -1 && freeMap.containsKey(hexagonId)) {
                if (hexagonId == start || hexagonId == end) {
                    return freeMap.get(hexagonId);
                }
                siblings[siblingCount ++] = freeMap.get(hexagonId);
            }
        }

        if (siblingCount == 0) return null;
        else return siblings[ThreadLocalRandom.current().nextInt(siblingCount)];
    }

    public List<Hexagon> getRoadSibling(Hexagon hexagon) {
        List<Hexagon> roadSiblingList = new LinkedList<>();
        for (HexagonDirection direction: HexagonDirection.values()) {
            if (direction == HexagonDirection.FREE) continue;
            if (hexagon.getBridge(direction)) roadSiblingList.add(
                    occupiedMap.get(getHexagonIdByDirection(hexagon, direction)));
        }

        for (Hexagon hexagon1: roadSiblingList) {
            if (hexagon1 == null) {
                int a  = 1;
            }
        }
        return roadSiblingList;
    }

    /**
     * 返回一个随机的空白点
     * @return
     */
    protected Hexagon randomUnusedHexagon() {
        if (freeMap == null || freeMap.size() == 0) return null;
        int choice = ThreadLocalRandom.current().nextInt(freeMap.size());
        for (Hexagon hexagon: freeMap.values()) {
            if (choice -- == 0) {
                return hexagon;
            }
        }
        return null;
    }

    public void init() {
        occupiedMap = new HashMap<>();
        freeMap = new HashMap<>();

        for (int height = 1; height <= bound; height ++) {
            for (int width = 1; width <= bound; width ++) {
                int id = getHexagonId(width, height);
                freeMap.put(id, new Hexagon(id, width, height));
            }
        }
    }

    public int getHexagonId(int width, int height) {
        return (height - 1) * bound + width;
    }

    private boolean isInBound(int width, int height) {
        if (width < 1 || height < 1 || width > bound || height > bound) return false;
        return true;
    }

    public boolean isContainStartEnd() {
        if (!occupiedMap.containsKey(start) || !occupiedMap.containsKey(end)) return false;
        return true;
    }

    public int length() {
        return occupiedMap.size();
    }

    public int getDistanceToEnd(int id) {
        return distancesToEnd.get(id);
    }

    public HexagonDirection getRoadDirectionToEnd(int hexagonId) {
        return getRoadDirection(hexagonId, end);
    }

    public HexagonDirection getRoadDirection(int fromHexagonId, int toHexagonId) {
        if (!isCrossPoint(fromHexagonId) || !isCrossPoint(toHexagonId)) return null;

        List<Integer> path = getPath(fromHexagonId, toHexagonId);
        if (path == null || path.size() == 0) return null;

        int firstWayPointId = path.get(1);
        Branch branch = getBranch(fromHexagonId, firstWayPointId);
        if (branch == null) return null;

        int nextHexagonId;
        if (fromHexagonId == branch.head.getId()) {
            if (branch.distance() == 1) {
                nextHexagonId = branch.tail.getId();
            } else {
                nextHexagonId = branch.hexagonList.get(0).getId();
            }
        } else {
            if (branch.distance() == 1) {
                nextHexagonId = branch.head.getId();
            } else {
                nextHexagonId = branch.hexagonList.get(branch.hexagonList.size() - 1).getId();
            }
        }
        return getOccupiedHexagon(fromHexagonId).getDirection(getOccupiedHexagon(nextHexagonId));
    }

    public Hexagon getOccupiedHexagon(int id) {
        return occupiedMap.get(id);
    }

    /**
     * 获取从节点a到节点b的所有crossPoint的list集合
     */
    private List<Integer> getPath(int fromHexagonId, int toHexagonId) {
        Map<Integer, List<Integer>> pointPath = pathMap.get(fromHexagonId);
        if (pointPath != null)  return pointPath.get(toHexagonId);
        return null;
    }

    /**
     * 判断是否为交叉点
     */
    public boolean isCrossPoint(int hexagonId) {
        return vertexSet.contains(getOccupiedHexagon(hexagonId));
    }

    /**
     * 根据起点和终点获取branch
     */
    private Branch getBranch(int startBlockId, int endBlockId) {
        for  (Branch branch: branchSet) {
            if ((branch.head.getId() == startBlockId && branch.tail.getId()  == endBlockId) ||
                    (branch.head.getId()  == endBlockId && branch.tail.getId() == startBlockId) ) {
                return branch;
            }
        }
        return null;
    }

    public MapSkeleton expand() {
        MapSkeleton expandedMap = new MapSkeleton(bound * 2);
        for (Hexagon hexagon: occupiedMap.values()) {
            System.out.println("old hexagon:" + hexagon);
            int hexagonId;
            hexagonId = expandedMap.getHexagonId((hexagon.getX() - 1) * 2 + 1, (hexagon.getY() - 1) * 2 + 1);
            /*
            if (hexagon.getY() % 2 == 1) {
            } else {
                hexagonId = expandedMap.getHexagonId(hexagon.getX() * 2, (hexagon.getY() - 1) * 2 + 1);
            }
            */
            Hexagon newHexagon = expandedMap.freeMap.get(hexagonId);
            newHexagon.bridges = hexagon.bridges;

            expandedMap.occupiedMap.put(hexagonId, newHexagon);
            expandedMap.freeMap.remove(hexagonId);
            System.out.println("new hexagon:" + expandedMap.occupiedMap.get(hexagonId));
        }

        List<Hexagon> occupiedList = new LinkedList<>();
        for (Hexagon hexagon: expandedMap.occupiedMap.values()) {
            occupiedList.add(hexagon);
        }

        for (Hexagon current: occupiedList) {
            System.out.println("o current:" + current);
            for (HexagonDirection direction: current.getRoadDirection()) {
                System.out.println("o direction:" + direction);
                int hexagonId = expandedMap.getHexagonIdByDirection(current, direction);
                expandedMap.addBlock(expandedMap.freeMap.get(hexagonId), current);
                expandedMap.addBlock(expandedMap.freeMap.get(hexagonId), current);

                System.out.println("o hexagon id:" + hexagonId);
                while (!expandedMap.occupiedMap.containsKey(hexagonId)) {
                    current = expandedMap.getOccupiedHexagon(hexagonId);
                    System.out.println("current:" + current);
                    hexagonId = expandedMap.getHexagonIdByDirection(current, direction);
                    System.out.println("hexagon id:" + hexagonId);
                }
            }
        }
        expandedMap.build();
        return expandedMap;
    }

    public byte[] toBytes() {
        Map<Integer, int[]> segmentIndexIdMap = new HashMap<>();

        int currentIndex = 0;
        for (Hexagon hexagon: occupiedMap.values()) {
            if (currentIndex <= hexagon.getId()) currentIndex = hexagon.getId() + 1;
        }

        System.out.println("current index: " + currentIndex);

        for (Segment segment: segmentMap.values()) {
            segmentIndexIdMap.put(segment.getId(), new int[segment.getPoints().length]);
            for (int i = 0; i < segment.getPoints().length; i ++) {
                segmentIndexIdMap.get(segment.getId())[i] = currentIndex ++;
            }
        }
        System.out.println("current index: " + currentIndex);

        ByteBuilder byteBuilder = new ByteBuilder();
        byteBuilder.append(occupiedMap.size());
        for (Hexagon hexagon: occupiedMap.values()) {
            byteBuilder.append((short) hexagon.getId());
            byteBuilder.append((int)(hexagon.coordinateX() * 1000));
            byteBuilder.append((int)(hexagon.coordinateY() * 1000));
            byteBuilder.append((byte) hexagon.bridgeCount());
            for (HexagonDirection direction: hexagon.getRoadDirection()) {
                //byteBuilder.append((short)getHexagonIdByDirection(hexagon, direction));

                int siblingId = getHexagonIdByDirection(hexagon, direction);
                Segment segment = segmentMap.get(Segment.getId(hexagon.getId(), siblingId));
                int index = segment.getAdjacentPointIndex(hexagon.getId());
                int id = segmentIndexIdMap.get(segment.getId())[index];
                byteBuilder.append((short)id);
            }
        }

        byteBuilder.append(segmentMap.size());
        for (Segment segment: segmentMap.values()) {
            //byteBuilder.append(segment.getId());
            byteBuilder.append(segment.getPoints().length);

            int[] ids = segmentIndexIdMap.get(segment.getId());
            for (int i = 0; i < segment.getPoints().length; i ++) {
                byteBuilder.append((short)ids[i]);
                byteBuilder.append(segment.getPoints()[i].x);
                byteBuilder.append(segment.getPoints()[i].y);
                byteBuilder.append((byte) 2);
                if (i == 0) {
                    byteBuilder.append((short)segment.getH1().getId());
                    byteBuilder.append((short)ids[i + 1]);
                } else if (i == segment.getPoints().length - 1) {
                    byteBuilder.append((short)ids[i - 1]);
                    byteBuilder.append((short)segment.getH2().getId());
                } else {
                    byteBuilder.append((short)ids[i - 1]);
                    byteBuilder.append((short)ids[i + 1]);
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(byteBuilder.getBytes());
            gzip.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] mapBytes = out.toByteArray();

        System.out.println("bytes: " + mapBytes.length);
        int line = 1;
        for (byte blockByte: mapBytes) {
            System.out.print(blockByte & 0xFF);
            System.out.print(",");

            if (line ++ % 100 == 0) {
                System.out.print("\n");
            }
        }
        System.out.print("\n");

        return mapBytes;
    }

}

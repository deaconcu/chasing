package com.prosper.chasing.game.mapV2;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.prosper.chasing.game.base.Point2;
import com.prosper.chasing.game.base.RoadPoint;
import com.prosper.chasing.game.util.Enums.*;
import com.prosper.chasing.game.util.Graph;
import com.prosper.chasing.game.util.Util;

/**
 * Created by deacon on 2019/5/13.
 */
public class GameMap {

    // 地图上的全部块
    public Map<Integer, Block> blockMap;
    // 已使用的路径块
    public Map<Integer, Block> occupiedMap;
    // 空闲的空白块
    public Map<Integer, Block> freeMap;
    // 交叉点
    public Set<Block> crossBlockSet = new HashSet<>();
    // 道路集合
    public Set<Branch> branchSet = new HashSet<>();
    // 道路段集合
    public Map<Integer, Segment> segmentMap = new HashMap<>();
    // 每一个block到终点的最短距离集合
    public Map<Integer, Integer> distancesToEnd = new HashMap<>();
    // 每一个block到另一个block的最短路径
    public Map<Integer, Map<Integer, List<Integer>>> pathMap;

    // 起点
    public int start;
    // 终点
    public int end;
    // 地图宽度，定义为地图边长的block数量
    public int blockBound;

    public static class TreeNode {
        int blockId;
        List<TreeNode> children;
        TreeNode parents;
        boolean onPath;

        public TreeNode(int blockId, TreeNode parents) {
            this.blockId = blockId;
            this.parents = parents;
            children = new LinkedList<>();
            onPath = false;
        }

        public void addChild(int childBlockId) {
            if (childBlockId == -1 || (parents != null && childBlockId == parents.blockId)) return;
            children.add(new TreeNode(childBlockId, this));
        }

        @Override
        public String toString() {
            String childString = "";
            for (TreeNode treeNode: children) {
                childString += treeNode.blockId + ",";
            }
            return "block objectId:" + blockId + ", child: [" + childString + "]";
        }
    }

    /**
     *
     * 初始化地图
     * @param blockBound 地图宽度
     * @param length 从起点到终点的线路最小长度
     */
    public GameMap(int blockBound, int length) {
        this.blockBound = blockBound;
        this.start = 1;
        this.end = blockBound * blockBound;

        int fillCount = 1;
        int pruningCount = 1;
        while (true) {
            // 产生一个保护起始点和终点的地图
            while (true) {
                init();
                fill();

                System.out.println("fill: " + fillCount);
                fillCount ++;

                if (occupiedMap.containsKey(start) && occupiedMap.containsKey(end)) break;
            }

            // 剪枝
            pruning(start, end);
            System.out.println("pruning: " + pruningCount);
            pruningCount ++;
            if (occupiedMap.size() > length) {
                break;
            }
        }
    }

    /**
     * 初始化地图上的所有块
     */
    public void init() {
        blockMap = new HashMap<>();
        occupiedMap = new HashMap<>();
        freeMap = new HashMap<>();

        for (int x = 1; x <= blockBound; x ++) {
            for (int y = 1; y <= blockBound; y ++) {
                Block block = new Block(getBlockId(x, y), x, y);
                blockMap.put(block.getId(), block);
                freeMap.put(block.getId(), block);
            }
        }
    }

    /**
     * 随机产生一个迷宫
     */
    private void fill() {
        Block block = randomUnusedBlock();
        addBlock(block, null);

        LinkedList<Block> pathBlockIdList = new LinkedList<>();
        pathBlockIdList.add(block);
        while (true) {
            Block nextBlock = getRandomFreeSibling(block);
            while (nextBlock == null && pathBlockIdList.size() > 0) {
                block = Util.random(pathBlockIdList);

                nextBlock = getRandomFreeSibling(block);
                if (nextBlock == null) {
                    pathBlockIdList.remove(block);
                }
            }
            if (pathBlockIdList.size() == 0) break;

            addBlock(nextBlock, block);
            block = nextBlock;
            if (!pathBlockIdList.contains(block)) {
                pathBlockIdList.add(block);
            }
        }
    }

    /**
     * 对迷宫进行剪枝
     * @param startBlockId 起点
     * @param endBlockId 终点
     */
    private void pruning(int startBlockId, int endBlockId) {
        TreeNode startNode = new TreeNode(startBlockId, null);
        TreeNode endNode = null;
        TreeNode currentNode = startNode;
        LinkedList<TreeNode> treeNodeList = new LinkedList<>();

        while(true) {
            if (currentNode == null) break;
            if (currentNode.blockId == endBlockId) endNode = currentNode;

            List<Block> siblings = getRoadSibling(occupiedMap.get(currentNode.blockId));
            for (Block block: siblings) {
                currentNode.addChild(block.getId());
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
            Block block = occupiedMap.get(currentNode.blockId);
            if (!currentNode.onPath) {
                removeBlock(block);
            }
            block.clear();

            for (TreeNode childNode: currentNode.children) {
                treeNodeList.add(childNode);
            }
            currentNode = treeNodeList.pollFirst();
        }

        currentNode = endNode;
        while (currentNode != null) {
            Block block = occupiedMap.get(currentNode.blockId);
            if (currentNode.parents != null) {
                Block parentBlock = occupiedMap.get(currentNode.parents.blockId);
                block.setBridges(block.getDirection(parentBlock), true);
                parentBlock.setBridges(parentBlock.getDirection(block), true);
            }
            currentNode = currentNode.parents;
        }
    }

    /**
     * 合并其他地图
     * @param
     * @return
     */
    public GameMap merge(GameMap map) {
        for (Block block: map.occupiedMap.values()) {
            if (occupiedMap.containsKey(block.getId())) occupiedMap.get(block.getId()).merge(block);
            else occupiedMap.put(block.getId(), block);
            freeMap.remove(block.getId());
        }
        return this;
    }


    public void optimize() {
        while (true) {
            boolean branchRemoved = false;
            trimAndCalculate(1);
            trimAndCalculate(2);

            // 移除一些多余的道路, 比如距离比较短，不是关键路径且绕路距离和本身距离相差不多的道路
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

    /**
     * 删除不需要的branch
     * 1:起点和终点相同的
     * 2:起点和终点间有多条路线，保留最长的那条，其余删除
     *
     * @param step 1:trim 2:calculate
     */
    public void trimAndCalculate(int step) {
        // get vertex set
        crossBlockSet.clear();
        branchSet.clear();

        crossBlockSet.add(occupiedMap.get(start));
        crossBlockSet.add(occupiedMap.get(end));

        for (Block block: occupiedMap.values()) {
            if (block.bridgeCount() > 2) crossBlockSet.add(block);
        }

        System.out.println("vertex set count: " + crossBlockSet.size());
        for (Block block: crossBlockSet) System.out.println(block);

        // get edge set
        for (Block block: crossBlockSet) {
            for (Block current : getRoadSibling(block)) {
                if (crossBlockSet.contains(current)) {
                    if (block.getId() < current.getId()) {
                        addBranch(new Branch(block, current));
                    }
                } else {
                    List<Block> blockList = new LinkedList<>();
                    blockList.add(current);

                    boolean reachEnd = false;
                    while (!reachEnd) {
                        for (Block sibling : getRoadSibling(current)) {
                            if (blockList.contains(sibling)) {
                                continue;
                            }
                            else if (blockList.size() == 1 && sibling.getId() == block.getId()) {
                                continue;
                            }
                            else if (crossBlockSet.contains(sibling)) {
                                if (block.getId() <= sibling.getId()) {
                                    Branch branch = new Branch(block, sibling);
                                    branch.blockList = blockList;
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

        int[] blockIds = new int[crossBlockSet.size()];
        int index = 0;
        for (Block block: crossBlockSet) {
            blockIds[index++] = block.getId();
        }

        if (step > 1) {
            Graph graph = new Graph(blockIds);
            for (Branch branch : branchSet) {
                graph.setEdge(branch.head.getId(), branch.tail.getId(), branch.blockList.size() + 1);
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
                    if (branch.blockList.size() >= branches[j].blockList.size()) continue;
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

    /**
     * 计算segment
     */
    private void doSegment() {
        for (Block block: occupiedMap.values()) {
            for (Block sibling: getRoadSibling(block)) {
                if (block.getId() > sibling.getId()) continue;
                // TODO 可能有溢出问题
                int id = block.getId() * 10000 + sibling.getId();
                segmentMap.put(id, new Segment(block, sibling));
            }
        }

        /*
        Map<Hexagon, List<Segment>> forkMap = new HashMap<>();
        for (Segment segment1: segmentMap.values()) {
            for (Segment segment2: segmentMap.values()) {
                if (!vertexSet.contains(segment1.getH1())) {
                    if (segment1.getH1().getId() == segment2.getH2().getId()) {
                        double deflection = (segment1.getFirstRoadSection().getStart().getDeflection() +
                                segment2.getLastRoadSection().getEnd().getDeflection()) / 2;
                        segment1.getFirstRoadSection().resetP1Deflection(deflection);
                        segment2.getLastRoadSection().resetP2Deflection(deflection);
                    }
                    if (segment1.getH1().getId() == segment2.getH1().getId()) {
                        double deflection = (segment1.getFirstRoadSection().getStart().getDeflection() +
                                segment2.getFirstRoadSection().getStart().getDeflection()) / 2;
                        segment1.getFirstRoadSection().resetP1Deflection(deflection + Math.PI);
                        segment2.getFirstRoadSection().resetP1Deflection(deflection);
                    }
                }
                if (!vertexSet.contains(segment1.getH2())) {
                    if (segment1.getH2().getId() == segment2.getH1().getId()) {
                        double deflection = (segment1.getLastRoadSection().getEnd().getDeflection() +
                                segment2.getFirstRoadSection().getStart().getDeflection()) / 2;
                        segment1.getLastRoadSection().resetP2Deflection(deflection);
                        segment2.getFirstRoadSection().resetP1Deflection(deflection);
                    }
                    if (segment1.getH2().getId() == segment2.getH2().getId()) {
                        double deflection = (segment1.getLastRoadSection().getEnd().getDeflection() +
                                segment2.getLastRoadSection().getEnd().getDeflection()) / 2;
                        segment1.getLastRoadSection().resetP2Deflection(deflection + Math.PI);
                        segment2.getLastRoadSection().resetP2Deflection(deflection);
                    }
                }
            }

            if (!forkMap.containsKey(segment1.getH1()))
                forkMap.put(segment1.getH1(), new LinkedList<>());
            if (!forkMap.containsKey(segment1.getH2()))
                forkMap.put(segment1.getH2(), new LinkedList<>());

            forkMap.get(segment1.getH1()).add(segment1);
            forkMap.get(segment1.getH2()).add(segment1);
        }

        for (Hexagon hexagon: vertexSet) {
            List<Segment> segmentList = forkMap.get(hexagon);
            if (segmentList.size() != 3) continue;

            double[] roadDeflections = new double[3];
            int index = 0;
            for (Segment segment: segmentList) {
                if (hexagon.getId() == segment.getH1().getId())
                    roadDeflections[index ++] = segment.getFirstRoadSection().getStart().getDeflection();
                else {
                    double value = segment.getLastRoadSection().getEnd().getDeflection() + Math.PI;
                    roadDeflections[index ++] = value > 2 * Math.PI ? (value - 2 * Math.PI) : value;
                }
            }

            double[] deflection = new double[3];
            for (int i = 0; i < 3; i ++) {
                int j = (i + 1) > 2 ? 0 : i + 1;
                if (Math.abs(roadDeflections[i] - roadDeflections[j]) > Math.PI)
                    deflection[i] = (roadDeflections[i] + roadDeflections[j]) / 2 + Math.PI + Math.PI / 2;
                else
                    deflection[i] = (roadDeflections[i] + roadDeflections[j]) / 2 + Math.PI / 2;
            }

            RoadPoint[] roadPoints = new RoadPoint[3];
            for (int i = 0; i < 3; i ++) {
                roadPoints[i] = new RoadPoint(Point2.getEdge(hexagon.coordinatePoint(),
                        deflection[i], RoadSection.ROAD_WIDTH * 3 / 2)[1], deflection[i]);
            }
            branchCrossPoint.put(hexagon, roadPoints);

            for (Segment segment: segmentList) {
                if (hexagon.getId() == segment.getH1().getId()) segment.getFirstRoadSection().setEdgeOfStart(roadPoints);
                else segment.getLastRoadSection().setEdgeOfEnd(roadPoints);
            }
        }
        */
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

    private void removeBranch(Branch branch) {
        if (branch.distance() > 1) {
            for (Block block: branch.blockList) {
                removeBlock(block);
            }
        } else {
            branch.head.setBridges(branch.head.getDirection(branch.tail), false);
            branch.tail.setBridges(branch.tail.getDirection(branch.head), false);
        }
    }

    /**
     * 返回一个随机的空白块
     * @return
     */
    protected Block randomUnusedBlock() {
        if (freeMap == null || freeMap.size() == 0) return null;
        int chosenBlockId = ThreadLocalRandom.current().nextInt(freeMap.size());
        for (Block block: freeMap.values()) {
            if (chosenBlockId -- == 0) {
                return block;
            }
        }
        return null;
    }

    /**
     * 随机获取一个block周围的空闲块
     * @param block 给定的block
     * @return
     */
    private Block getRandomFreeSibling(Block block) {
        Block[] siblings = new Block[4];
        int siblingCount = 0;

        for (BlockDirection direction: BlockDirection.values()) {
            int blockId = getBlockId(block, direction);
            if (blockId != -1 && freeMap.containsKey(blockId)) {
                if (blockId == start || blockId == end) {
                    return freeMap.get(blockId);
                }
                siblings[siblingCount ++] = freeMap.get(blockId);
            }
        }
        return Util.random(siblings, siblingCount);
    }

    /**
     * 获取临近的道路块列表
     * @param block
     */
    public List<Block> getRoadSibling(Block block) {
        List<Block> roadSiblingList = new LinkedList<>();
        for (BlockDirection direction: BlockDirection.values()) {
            if (block.hasBridge(direction)) {
                Block roadBlock = occupiedMap.get(getBlockId(block, direction));
                if (roadBlock != null) roadSiblingList.add(roadBlock);
            }
        }
        return roadSiblingList;
    }

    /**
     * 添加一个block
     * @param block
     * @param fromBlock
     */
    protected void addBlock(Block block, Block fromBlock) {
        if (block == null) return;
        if (occupiedMap.containsKey(block)) return;

        if (fromBlock != null) {
            block.setBridges(block.getDirection(fromBlock), true);
            fromBlock.setBridges(fromBlock.getDirection(block), true);
        }

        occupiedMap.put(block.getId(), block);
        freeMap.remove(block.getId());
    }

    /**
     * 移除一个block
     * @param block
     */
    protected void removeBlock(Block block) {
        if (block == null) return;
        for (Block sibling: getRoadSibling(block)) {
            sibling.setBridges(sibling.getDirection(block), false);
        }

        block.clear();
        occupiedMap.remove(block.getId());
        freeMap.put(block.getId(), block);
    }

    /**
     * 获取block id
     * @param x block的x坐标, 定义为(1, blockBound)
     * @param y block的y坐标, 定义为(1, blockBound)
     * @return id 范围为(1, blockBound * blockBound)
     */
    public int getBlockId(int x, int y) {
        return (y - 1) * blockBound + x;
    }

    /**
     * 获取block id
     * @param block 给定的block
     * @param direction 方向
     * @return
     */
    private int getBlockId(Block block, BlockDirection direction) {
        int x = block.getX();
        int y = block.getY();

        if (direction == BlockDirection.RIGHT) {
            if (isInBlockBound(x + 1, y)) return getBlockId(x + 1, y);
        } else if (direction == BlockDirection.DOWN) {
            if (isInBlockBound(x, y - 1)) return getBlockId(x, y - 1);
        } else if (direction == BlockDirection.LEFT) {
            if (isInBlockBound(x - 1, y)) return getBlockId(x - 1, y);
        } else if (direction == BlockDirection.UP) {
            if (isInBlockBound(x, y + 1)) return getBlockId(x, y + 1);
        }
        return -1;
    }

    /**
     * 检查给定坐标是否在地图范围内
     * @return
     */
    private boolean isInBlockBound(int x, int y) {
        if (x < 1 || y < 1 || x > blockBound || y > blockBound) return false;
        return true;
    }
}

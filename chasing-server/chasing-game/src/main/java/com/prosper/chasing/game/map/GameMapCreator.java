package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.prosper.chasing.game.util.Constant.Direction.*;
import static com.prosper.chasing.game.util.Enums.*;

/**
 * Created by deacon on 2018/4/25.
 */
public class GameMapCreator {

    // 已生成的路径块
    public Map<Integer, Block> usedBlockMap;
    // 墙的集合
    public Set<Integer> bridgeSet;
    // 未生成的空白块
    protected Set<Integer> unusedBlockSet;
    // 短路径列表
    public LinkedList<SegmentOld> shortcutList;

    Set<Block> vertexSet = new HashSet<>();
    Set<SegmentOld> segmentSet = new HashSet<>();

    public int start;

    public int end;

    public int boundX;

    public int boundY;

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
            return "block id:" + blockId + ", child: [" + childString + "]";
        }
    }

    public GameMapCreator(int boundX, int boundY) {
        this.boundX = boundX;
        this.boundY = boundY;

        clear();
    }

    public void clear() {
        usedBlockMap = new HashMap<>();
        unusedBlockSet = new HashSet<>();
        bridgeSet = new HashSet<>();
        shortcutList = new LinkedList<>();

        for (int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                unusedBlockSet.add(getBlockId(i, j));
            }
        }
    }

    protected void fillWithRandomRoads() {
        int startBlockId = randomUnusedPoint();
        addBlock(startBlockId, -1, BlockType.MAIN_ROAD);

        LinkedList<Integer> pathBlockIdList = new LinkedList<>();

        int blockId = startBlockId;
        pathBlockIdList.add(blockId);
        while (true) {
            int nextBlockId = getRandomUnusedSibling(blockId);
            while (nextBlockId == -1 && pathBlockIdList.size() > 0) {
                blockId = pathBlockIdList.get(ThreadLocalRandom.current().nextInt(pathBlockIdList.size()));
                //System.out.println("get block id from nodes, x:" + getX(hexagonId) + ", y:" + getY(hexagonId));

                nextBlockId = getRandomUnusedSibling(blockId);
                //System.out.println("get next block id, x:" + getX(nextBlockId) + ", y:" + getY(nextBlockId));
                if (nextBlockId == -1) {
                    pathBlockIdList.remove(new Integer(blockId));
                }
            }
            if (pathBlockIdList.size() == 0) break;

            addBlock(nextBlockId, blockId, BlockType.MAIN_ROAD);
            blockId = nextBlockId;
            if (!pathBlockIdList.contains(blockId)) {
                pathBlockIdList.add(blockId);
            }
        }
    }

    /**
     * 移除分支路径
     */
    protected void removeBrand(int startBlockId, int endBlockId) {
        TreeNode startNode = new TreeNode(startBlockId, null);
        TreeNode endNode = null;
        TreeNode currentNode = startNode;
        LinkedList<TreeNode> treeNodeList = new LinkedList<>();
        while(true) {
            if (currentNode == null) break;
            if (currentNode.blockId == endBlockId) endNode = currentNode;

            currentNode.addChild(getRoadBlockIdAround(currentNode.blockId, RIGHT));
            currentNode.addChild(getRoadBlockIdAround(currentNode.blockId, DOWN));
            currentNode.addChild(getRoadBlockIdAround(currentNode.blockId, LEFT));
            currentNode.addChild(getRoadBlockIdAround(currentNode.blockId, UP));

            for (TreeNode childNode: currentNode.children) {
                treeNodeList.add(childNode);
                //System.out.println("add child node: " + getX(childNode.hexagonId) + ", " + getY(childNode.hexagonId));
            }
            currentNode = treeNodeList.pollFirst();
            //System.out.println("current node: " + getX(currentNode.hexagonId) + ", " + getY(currentNode.hexagonId));
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
            if (!currentNode.onPath) removeBlock(usedBlockMap.get(currentNode.blockId), false);

            for (TreeNode childNode: currentNode.children) {
                treeNodeList.add(childNode);
            }
            currentNode = treeNodeList.pollFirst();
        }
    }

    public void merge(GameMapCreator gameMapCreator) {
        Set<Block> removeBlockSet = new HashSet<>();
        for (Block block: gameMapCreator.usedBlockMap.values()) {
            if (usedBlockMap.containsKey(block.blockId)) {
                removeBlockSet.add(block);
            }
        }

        for (Block block: removeBlockSet) {
            gameMapCreator.removeBlock(block, true);
        }

        for (Block block: gameMapCreator.usedBlockMap.values()) {
            addBranch(block);
        }
        bridgeSet.addAll(gameMapCreator.bridgeSet);

        generateShortcut();
    }

    public void mergeV2(GameMapCreator gameMapCreator) {
        Set<Block> removeBlockSet = new HashSet<>();
        for (Block block: gameMapCreator.usedBlockMap.values()) {
            if (usedBlockMap.containsKey(block.blockId)) {
                removeBlockSet.add(block);
            }
        }

        for (Block block: removeBlockSet) {
            gameMapCreator.removeBlock(block, true);
        }

        for (Block block: gameMapCreator.usedBlockMap.values()) {
            addBlock(block, BlockType.MAIN_ROAD);
        }
        bridgeSet.addAll(gameMapCreator.bridgeSet);

        printTerrainBlocks(3);

        //generateShortcut();
    }

    public void computerEdgeInfo() {
        // get vertex set
        vertexSet.clear();
        segmentSet.clear();

        vertexSet.add(usedBlockMap.get(start));
        vertexSet.add(usedBlockMap.get(end));

        for (Block block: usedBlockMap.values()) {
            int aroundBlockCount = 0;
            for (int i = 0; i <= 3; i ++) {
                int aroundBlockId = getRoadBlockIdAround(block.blockId, i);
                if (aroundBlockId != -1) {
                    aroundBlockCount ++;
                }
            }
            if (aroundBlockCount > 2) {
                vertexSet.add(block);
            }
        }

        int[] blockIds = new int[vertexSet.size()];
        int index = 0;
        for (Block block: vertexSet) {
            blockIds[index++] = block.blockId;
        }

        //Graph graph = new Graph(blockIds);

        System.out.println("vertex set count: " + vertexSet.size());
        for (Block block: vertexSet) {
            System.out.println("vertex - " + block.blockId + ": [" + block.position.x + "," + block.position.y + "]");
        }


        // get edge set
        for (Block block: vertexSet) {
            for (int i = 0; i <= 3; i ++) {
                int currentBlockId = getRoadBlockIdAround(block.blockId, i);
                if (currentBlockId != -1) {
                    if (vertexSet.contains(usedBlockMap.get(currentBlockId))) {
                        if (block.blockId < usedBlockMap.get(currentBlockId).blockId)  {
                            segmentSet.add(new SegmentOld(block, usedBlockMap.get(currentBlockId)));
                        }
                    } else {
                        List<Block> blockList = new LinkedList<>();
                        blockList.add(usedBlockMap.get(currentBlockId));

                        boolean reachEnd = false;
                        while (!reachEnd) {
                            for (int j = 0; j <= 3; j ++) {
                                int aroundBlockId = getRoadBlockIdAround(currentBlockId, j);
                                if (aroundBlockId == -1 || blockList.contains(usedBlockMap.get(aroundBlockId))
                                        || aroundBlockId == block.blockId) continue;
                                else if (vertexSet.contains(usedBlockMap.get(aroundBlockId))) {
                                    if (block.blockId < usedBlockMap.get(aroundBlockId).blockId) {
                                        SegmentOld segment = new SegmentOld(block, usedBlockMap.get(aroundBlockId));
                                        segment.blockList =  blockList;
                                        segmentSet.add(segment);

                                        /*
                                        hexagonList.get(0).previous = block;
                                        block.next = hexagonList.get(0);
                                        hexagonList.get(hexagonList.size() - 1).next = usedBlockMap.get(aroundBlockId);
                                        usedBlockMap.get(aroundBlockId).previous = hexagonList.get(hexagonList.size() - 1);
                                        */
                                    }
                                    reachEnd = true;
                                } else {
                                    blockList.add(usedBlockMap.get(aroundBlockId));
                                    currentBlockId = aroundBlockId;
                                }
                            }
                        }
                    }
                }
            }
        }

        /*
        for (SegmentOld segment : branchSet) {
            graph.setEdge(segment.head.hexagonId, segment.tail.hexagonId, segment.hexagonList.size() + 1);
        }

        Map<Integer, Map<Integer, List<Integer>>> pathMap = graph.countPath();
        for (SegmentOld segment : branchSet) {
            int detourDistance = graph.countDetourDistance(segment.head.hexagonId, segment.tail.hexagonId);
            segment.detourDistance = detourDistance;
        }
        */

        // 按branch长度排序后打印, 用来调试，可以注释
        SegmentOld[] segments = new SegmentOld[segmentSet.size()];
        int i = 0;
        for (SegmentOld segment : segmentSet) {
            int m = i;
            for (int j = 0; j < i; j ++) {
                if (segment.blockList.size() >= segments[j].blockList.size()) continue;
                else {
                    m = j;
                    break;
                }
            }
            for (int k = i; k > m; k --) {
                segments[k] = segments[k - 1];
            }
            segments[m] = segment;
            i ++;
        }

        for (SegmentOld segment : segments) {
            System.out.println("segment: " +
                    "\t" + segment.head.blockId + "[" + segment.head.position.x + "," + segment.head.position.y + "], " +
                    "\t" + segment.tail.blockId + "[" + segment.tail.position.x + "," + segment.tail.position.y + "], " +
                    "\tdistance: " + segment.distance() + ", \tdetour distance: " + segment.detourDistance);
        }
    }

    protected Block[] getRoadSibling(int blockId) {
        int x = getX(blockId);
        int y = getY(blockId);

        int[] validSiblings = new int[4];
        int validSiblingCount = 0;
        if (isInBound(x, y + 1) && usedBlockMap.containsKey(getBlockId(x, y + 1)))
            if (usedBlockMap.get(getBlockId(x, y + 1)).type == BlockType.MAIN_ROAD &&
                    bridgeSet.contains(getBridgeId(blockId, UP))) {
                validSiblings[validSiblingCount ++] = getBlockId(x, y + 1);
            }
        if (isInBound(x + 1, y) && usedBlockMap.containsKey(getBlockId(x + 1, y)))
            if (usedBlockMap.get(getBlockId(x + 1, y)).type == BlockType.MAIN_ROAD &&
                bridgeSet.contains(getBridgeId(blockId, RIGHT))) {
                validSiblings[validSiblingCount ++] = getBlockId(x + 1, y);
            }
        if (isInBound(x, y - 1) && usedBlockMap.containsKey(getBlockId(x, y - 1)))
            if (usedBlockMap.get(getBlockId(x, y - 1)).type == BlockType.MAIN_ROAD &&
                bridgeSet.contains(getBridgeId(blockId, DOWN))) {
                validSiblings[validSiblingCount ++] = getBlockId(x, y - 1);
            }
        if (isInBound(x - 1, y) && usedBlockMap.containsKey(getBlockId(x - 1, y)))
            if (usedBlockMap.get(getBlockId(x - 1, y)).type == BlockType.MAIN_ROAD &&
                bridgeSet.contains(getBridgeId(blockId, LEFT))) {
                validSiblings[validSiblingCount ++] = getBlockId(x - 1, y);
            }

        if (validSiblingCount == 0) return null;

        Block[] siblingBlocks = new Block[validSiblingCount];
        for (int i = 0; i < validSiblingCount; i ++) {
            siblingBlocks[i] = usedBlockMap.get(validSiblings[i]);
        }
        return siblingBlocks;
    }

    protected int getRandomUnusedSibling(int blockId) {
        int x = getX(blockId);
        int y = getY(blockId);

        int[] validSiblings = new int[4];
        int validSiblingCount = 0;
        if (isInBound(x, y + 1) && unusedBlockSet.contains(getBlockId(x, y + 1)))
            validSiblings[validSiblingCount ++] = getBlockId(x, y + 1);
        if (isInBound(x + 1, y) && unusedBlockSet.contains(getBlockId(x + 1, y)))
            validSiblings[validSiblingCount ++] = getBlockId(x + 1, y);
        if (isInBound(x, y - 1) && unusedBlockSet.contains(getBlockId(x, y - 1)))
            validSiblings[validSiblingCount ++] = getBlockId(x, y - 1);
        if (isInBound(x - 1, y) && unusedBlockSet.contains(getBlockId(x - 1, y)))
            validSiblings[validSiblingCount ++] = getBlockId(x - 1, y);

        if (validSiblingCount == 0) return -1;
        else return validSiblings[ThreadLocalRandom.current().nextInt(validSiblingCount)];
    }

    protected int getSiblingCount(int blockId) {
        int x = getX(blockId);
        int y = getY(blockId);

        int validSiblingCount = 0;
        if (isInBound(x, y + 1) && unusedBlockSet.contains(getBlockId(x, y + 1))) validSiblingCount ++;
        if (isInBound(x + 1, y) && unusedBlockSet.contains(getBlockId(x + 1, y))) validSiblingCount ++;
        if (isInBound(x, y - 1) && unusedBlockSet.contains(getBlockId(x, y - 1))) validSiblingCount ++;
        if (isInBound(x - 1, y) && unusedBlockSet.contains(getBlockId(x - 1, y))) validSiblingCount ++;

        return validSiblingCount;
    }

    private Block getRoadBlockAround(int blockId, int direction) {
        Block aroundBlock = null;
        if (direction == RIGHT && isInBound(getX(blockId) + 1, getY(blockId))) {
            if (isBridgeExist(blockId, RIGHT))
                aroundBlock = usedBlockMap.get(getBlockId(getX(blockId) + 1, getY(blockId)));
        } else if (direction == DOWN  && isInBound(getX(blockId), getY(blockId) - 1)) {
            if (isBridgeExist(blockId, DOWN))
                aroundBlock = usedBlockMap.get(getBlockId(getX(blockId), getY(blockId) - 1));
        } else if (direction == LEFT  && isInBound(getX(blockId) - 1, getY(blockId))) {
            if (isBridgeExist(blockId, LEFT))
                aroundBlock = usedBlockMap.get(getBlockId(getX(blockId) - 1, getY(blockId)));
        } else if (direction == UP  && isInBound(getX(blockId), getY(blockId) + 1)) {
            if (isBridgeExist(blockId, UP))
                aroundBlock = usedBlockMap.get(getBlockId(getX(blockId), getY(blockId) + 1));
        }
        if (aroundBlock != null && aroundBlock.type == BlockType.MAIN_ROAD) {
            return aroundBlock;
        }
        return null;
    }

    private Block getBlockAround(int blockId, int direction) {
        Block aroundBlock = null;
        if (direction == RIGHT && isInBound(getX(blockId) + 1, getY(blockId))) {
            if (isBridgeExist(blockId, RIGHT))
                aroundBlock = usedBlockMap.get(getBlockId(getX(blockId) + 1, getY(blockId)));
        } else if (direction == DOWN  && isInBound(getX(blockId), getY(blockId) - 1)) {
            if (isBridgeExist(blockId, DOWN))
                aroundBlock = usedBlockMap.get(getBlockId(getX(blockId), getY(blockId) - 1));
        } else if (direction == LEFT  && isInBound(getX(blockId) - 1, getY(blockId))) {
            if (isBridgeExist(blockId, LEFT))
                aroundBlock = usedBlockMap.get(getBlockId(getX(blockId) - 1, getY(blockId)));
        } else if (direction == UP  && isInBound(getX(blockId), getY(blockId) + 1)) {
            if (isBridgeExist(blockId, UP))
                aroundBlock = usedBlockMap.get(getBlockId(getX(blockId), getY(blockId) + 1));
        }
        if (aroundBlock != null) {
            return aroundBlock;
        }
        return null;
    }

    private int getRoadBlockIdAround(int blockId, int direction) {
        Block block = getRoadBlockAround(blockId, direction);
        if (block == null) return -1;
        return block.blockId;
    }

    private int getBlockIdAround(int blockId, int direction) {
        Block block = getBlockAround(blockId, direction);
        if (block == null) return -1;
        return block.blockId;
    }

    protected void addBranch(Block block) {
        block.type = BlockType.BRANCH;
        usedBlockMap.put(block.blockId, block);
        unusedBlockSet.remove(block.blockId);
    }

    protected void addBlock(Block block, BlockType type) {
        block.type = type;
        usedBlockMap.put(block.blockId, block);
        unusedBlockSet.remove(block.blockId);
    }

    protected void addBlock(int blockId, int fromBlockId, BlockType type) {
        if (usedBlockMap.containsKey(blockId)) return;

        Block block = new Block(new Point2(getX(blockId), getY(blockId)), blockId, type);
        if (fromBlockId != -1) {
            if (getX(blockId) > getX(fromBlockId))  {
                bridgeSet.add(getBridgeId(fromBlockId, RIGHT));
            }
            else if (getX(blockId) < getX(fromBlockId)) {
                bridgeSet.add(getBridgeId(blockId, RIGHT));
            }
            else if (getY(blockId) < getY(fromBlockId)) {
                bridgeSet.add(getBridgeId(fromBlockId, DOWN));
            }
            else if (getY(blockId) > getY(fromBlockId)) {
                bridgeSet.add(getBridgeId(blockId, DOWN));
            }
        }
        usedBlockMap.put(blockId, block);
        unusedBlockSet.remove(blockId);

        try {
            if (type == BlockType.MAIN_ROAD) {
                //Thread.sleep(30);
                //printTerrainBlocks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println("add block to map, block: [x:" +
        // getX(hexagonId) + ", y:" + getY(hexagonId) + ", type: " + type + "]");
    }

    protected void removeBlock(Block block, boolean partlyBridge) {
        if (block == null) return;

        if (!partlyBridge) {
            if (block.next != null) {
                block.next.previous = null;
            }
            if (block.previous != null) {
                block.previous.next = null;
            }
        }

        if (partlyBridge) {
            if (!usedBlockMap.containsKey(getBlockIdByDirection(block.blockId, RIGHT, 1))) {
                bridgeSet.remove(getBridgeId(block.blockId, RIGHT));
            }
            if (!usedBlockMap.containsKey(getBlockIdByDirection(block.blockId, DOWN, 1))) {
                bridgeSet.remove(getBridgeId(block.blockId, DOWN));
            }
            if (!usedBlockMap.containsKey(getBlockIdByDirection(block.blockId, LEFT, 1))) {
                bridgeSet.remove(getBridgeId(block.blockId, LEFT));
            }
            if (!usedBlockMap.containsKey(getBlockIdByDirection(block.blockId, UP, 1))) {
                bridgeSet.remove(getBridgeId(block.blockId, UP));
            }
        } else {
            bridgeSet.remove(getBridgeId(block.blockId, RIGHT));
            bridgeSet.remove(getBridgeId(block.blockId, DOWN));
            bridgeSet.remove(getBridgeId(block.blockId, LEFT));
            bridgeSet.remove(getBridgeId(block.blockId, UP));
        }

        usedBlockMap.remove(block.blockId);
        unusedBlockSet.add(block.blockId);
    }

    public void generateShortcut() {
        List<SegmentOld> segmentList = new LinkedList<>();
        Set<Block> branchBlocks = new HashSet<>();
        for (Block block: usedBlockMap.values()) {
            if (block.type == BlockType.BRANCH) {
                branchBlocks.add(block);
            }
        }

        while (branchBlocks.size() > 0) {
            Block block = branchBlocks.iterator().next();
            Block previous = block.previous;
            while(previous != null && previous.type == BlockType.BRANCH) {
                block = previous;
                previous = block.previous;
            }

            SegmentOld segment = new SegmentOld(getBranchHeadTail(block, true));
            while(block != null && block.type == BlockType.BRANCH) {
                segment.addBlock(block);
                branchBlocks.remove(block);
                previous = block;
                block = block.next;
            }
            segment.tail = getBranchHeadTail(previous, false);
            if (segment.head == segment.tail || segment.head == null || segment.head.type != BlockType.MAIN_ROAD ||
                    segment.tail == null || segment.tail.type != BlockType.MAIN_ROAD) {
                for (Block branchBlock: segment.blockList) {
                    removeBlock(branchBlock, false);
                }
            } else {
                if (segment.head.distanceToFinish < segment.tail.distanceToFinish) {
                    segment.reverse();
                }
                segmentList.add(segment);
            }
        }

        Collections.sort(segmentList);
        for(SegmentOld segment : segmentList) {
            System.out.println(segment);
        }

        List<SegmentOld> removeSegmentList = new LinkedList<>();
        List<SegmentOld> addSegmentList = new LinkedList<>();
        for (SegmentOld segment : segmentList) {
            // 之前的交互可能让后面的分支无效, 需要删除无效的分支
            if (segment.head.type == BlockType.BRANCH || segment.tail.type == BlockType.BRANCH) {
                for (Block block: segment.blockList) {
                    removeBlock(block, false);
                }
                removeSegmentList.add(segment);
                continue;
            }
            if (segment.distance() > segment.getOriginDistance()) {
                SegmentOld newSegment = exchangeBranchToRoad(segment);
                if (newSegment != null) {
                    addSegmentList.add(newSegment);
                    removeSegmentList.add(segment);
                }
            }
        }

        for (SegmentOld segment : segmentList) {
            // 之前的交互可能让后面的分支无效, 需要删除无效的分支
            if (segment.head.type == BlockType.BRANCH || segment.tail.type == BlockType.BRANCH) {
                for (Block block: segment.blockList) {
                    removeBlock(block, false);
                }
                if (!removeSegmentList.contains(segment)) {
                    removeSegmentList.add(segment);
                }
                continue;
            }
        }

        segmentList.removeAll(removeSegmentList);
        segmentList.addAll(addSegmentList);

        removeSegmentList = new LinkedList<>();
        for (SegmentOld segment : segmentList) {
            if (segment.getShort() < 10) {
                if (segment.blockList.size() == 0) {
                    if (getX(segment.head.blockId) + 1 == getX(segment.tail.blockId)) {
                        bridgeSet.remove(getBridgeId(segment.head.blockId, RIGHT));
                    }
                    if (getX(segment.head.blockId) - 1 == getX(segment.tail.blockId)) {
                        bridgeSet.remove(getBridgeId(segment.head.blockId, LEFT));
                    }
                    if (getY(segment.head.blockId) + 1 == getY(segment.tail.blockId)) {
                        bridgeSet.remove(getBridgeId(segment.head.blockId, UP));
                    }
                    if (getY(segment.head.blockId) - 1 == getY(segment.tail.blockId)) {
                        bridgeSet.remove(getBridgeId(segment.head.blockId, DOWN));
                    }
                }
                for (Block block: segment.blockList) {
                    removeBlock(block, false);
                }
                if (!removeSegmentList.contains(segment)) {
                    removeSegmentList.add(segment);
                }
                addSegmentList.remove(segment);
            }
        }
        segmentList.removeAll(removeSegmentList);

        removeSegmentList = new LinkedList<>();
        for (SegmentOld segment : shortcutList) {
            // 之前的交互可能让后面的分支无效, 需要删除无效的分支
            if (segment.head.type != BlockType.MAIN_ROAD || segment.tail.type != BlockType.MAIN_ROAD) {
                for (Block block: segment.blockList) {
                    removeBlock(block, false);
                }
                if (!removeSegmentList.contains(segment)) {
                    removeSegmentList.add(segment);
                }
                continue;
            }
        }
        shortcutList.removeAll(removeSegmentList);

        for(SegmentOld segment : segmentList) {
            for(Block block: segment.blockList) {
                block.type = BlockType.SHORTCUT;
            }
            System.out.println(segment);
            shortcutList.add(segment);
        }
        Collections.sort(shortcutList);
    }

    private SegmentOld exchangeBranchToRoad(SegmentOld segment) {
        if (segment == null || segment.head == null || segment.tail == null || segment.blockList == null) return null;
        if (segment.distance() <= segment.getOriginDistance()) {
            return null;
        }

        Block branchHead = segment.head.next;

        // create new segment with origin head and tail
        SegmentOld trueSegment = new SegmentOld(segment.head);
        trueSegment.tail = segment.tail;

        Block currentBlock = branchHead;
        Block previousBlock = segment.head;
        while (currentBlock != segment.tail) {
            currentBlock.type = BlockType.BRANCH;
            trueSegment.addBlock(currentBlock);
            previousBlock = currentBlock;
            currentBlock = currentBlock.next;
        }

        // disconnect new segment to road
        branchHead.previous = null;
        previousBlock.next = null;

        // connect new road to main road
        trueSegment.head.next = segment.blockList.get(0);
        segment.blockList.get(0).previous = trueSegment.head;
        trueSegment.tail.previous = segment.blockList.get(segment.blockList.size() - 1);
        segment.blockList.get(segment.blockList.size() - 1).next = trueSegment.tail;

        // recount distance and set new road block
        currentBlock = trueSegment.tail.previous;
        while(currentBlock != null) {
            currentBlock.distanceToFinish = currentBlock.next.distanceToFinish + 1;
            if (currentBlock.type != BlockType.MAIN_ROAD) currentBlock.type = BlockType.MAIN_ROAD;
            currentBlock = currentBlock.previous;
        }

        System.out.println("segment exchanged to road, road length: " + usedBlockMap.get(start).distanceToFinish);
        return trueSegment;
    }

    private Block getBranchHeadTail(Block block, boolean getHead) {
        if (block.type != BlockType.BRANCH) return null;

        Block headOrTail = null;
        Block[] roadBlocks = getRoadSibling(block.blockId);

        if (roadBlocks == null || roadBlocks.length == 0) {
            headOrTail = null;
        } else if (roadBlocks.length == 1) {
            headOrTail = roadBlocks[0];
        } else {
            for (Block roadBlock: roadBlocks) {
                if (headOrTail == null) {
                    headOrTail = roadBlock;
                    continue;
                }
                if (getHead && (roadBlock.distanceToFinish > headOrTail.distanceToFinish)) {
                    headOrTail = roadBlock;
                } else if (!getHead && (roadBlock.distanceToFinish < headOrTail.distanceToFinish)) {
                    headOrTail = roadBlock;
                }
            }
        }
        return headOrTail;
    }

    public void generateSingleRouteInfo(int startBlockId, int endBlockId) {
        int currentBlockId = endBlockId;
        int lastBlockId = -1;
        int length = 0;
        while(currentBlockId != startBlockId && currentBlockId != -1) {
            int nextBlockId = -1;
            for (int i = 0; i <= 3; i ++) {
                nextBlockId = getRoadBlockIdAround(currentBlockId, i);
                if (nextBlockId != -1 && nextBlockId != lastBlockId) {
                    break;
                }
            }
            usedBlockMap.get(currentBlockId).distanceToFinish = length ++;
            usedBlockMap.get(currentBlockId).next = usedBlockMap.get(lastBlockId);
            if (nextBlockId != -1) usedBlockMap.get(currentBlockId).previous = usedBlockMap.get(nextBlockId);

            lastBlockId = currentBlockId;
            currentBlockId = nextBlockId;
        }

        if (currentBlockId == startBlockId) {
            usedBlockMap.get(currentBlockId).distanceToFinish = length ++;
            usedBlockMap.get(currentBlockId).next = usedBlockMap.get(lastBlockId);
        }
    }

    public int countLength(int startBlockId, int endBlockId) {
        int currentBlockId = startBlockId;
        int lastBlockId = -1;
        int length = 0;
        while(currentBlockId != endBlockId && currentBlockId != -1) {
            int nextBlockId = -1;
            for (int i = 0; i < 4; i ++) {
                nextBlockId = getRoadBlockIdAround(currentBlockId, i);
                if (nextBlockId != -1 && nextBlockId != lastBlockId) {
                    break;
                }
            }
            lastBlockId = currentBlockId;
            currentBlockId = nextBlockId;
            length ++;
        }
        return length;
    }

    /**
     * 返回一个随机的空白点
     * @return
     */
    protected int randomUnusedPoint() {
        if (unusedBlockSet == null || unusedBlockSet.size() == 0) return -1;
        int choice = ThreadLocalRandom.current().nextInt(unusedBlockSet.size());
        for (int blockId: unusedBlockSet) {
            if (choice -- == 0) {
                return blockId;
            }
        }
        return -1;
    }

    private boolean isBridgeExist(int blockId, byte direction) {
        return bridgeSet.contains(getBridgeId(blockId, direction));
    }

    private boolean isInBound(int x, int y) {
        if (x < 0 || x >= boundX) return false;
        if (y < 0 || y >= boundY) return false;
        return true;
    }

    private boolean isInPrintBound(int x, int y, int bridgeSize) {
        if (x < 0 || x > (boundX - 1) * bridgeSize) return false;
        if (y < 0 || y > (boundY - 1) * bridgeSize) return false;
        return true;
    }

    private boolean isInPrintBound(int blockId, int bridgeSize) {
        return isInPrintBound(getX(blockId), getY(boundY), bridgeSize);
    }

    protected int getBlockId(int x, int y) {
        return boundX * y + x + 1;
    }

    protected int getBlockId(int x, int y, int bridgeWidth) {
        return boundX * (bridgeWidth + 1) * y + x + 1;
    }

    protected int getBlockIdWithBorder(int x, int y, int bridgeWidth) {
        return (boundX * (bridgeWidth + 1) + bridgeWidth) * y + x + 1;
    }

    protected int getBlockIdByDirection(int blockId, int direction, int distance) {
        if (direction == RIGHT) {
            return getBlockId(getX(blockId) + distance, getY(blockId));
        } else if (direction == DOWN) {
            return getBlockId(getX(blockId), getY(blockId) - distance);
        } else if (direction == LEFT) {
            return getBlockId(getX(blockId) - distance, getY(blockId));
        } else if (direction == UP) {
            return getBlockId(getX(blockId), getY(blockId) + distance);
        } else {
            throw new RuntimeException("direction is not right");
        }
    }

    protected int getBridgeId(int blockId, int direction) {
        if (direction == RIGHT) {
            return blockId * 2;
        } else if (direction == DOWN) {
            return blockId* 2 + 1;
        } else if (direction == LEFT) {
            return getBlockId(getX(blockId) - 1, getY(blockId)) * 2;
        } else if (direction == UP) {
            return getBlockId(getX(blockId), getY(blockId) + 1) * 2 + 1;
        } else {
            throw new RuntimeException("direction is not right");
        }
    }

    public int getX(int id) {
        return (id - 1) % boundX;
    }

    public int getY(int id) {
        return (id - 1) / boundX;
    }

    public byte[] getByte() {
        return null;
    }

    public GameMap expand(int bridgeWidth) {
        GameMap gameMap = new GameMap(boundX * (bridgeWidth + 1), boundY * (bridgeWidth + 1), bridgeWidth);
        SegmentOld mainRoad = new SegmentOld(null);

        Block currentBlock = usedBlockMap.get(start);
        Block next = currentBlock.next;
        while (currentBlock != null) {
            int x = currentBlock.position.x * (bridgeWidth + 1);
            int y = currentBlock.position.y * (bridgeWidth + 1);

            Block from;
            if (mainRoad.blockList.size() == 0) {
                from = gameMap.addBlock(getBlockId(x, y, bridgeWidth), currentBlock.type);
                mainRoad.blockList.add(from);
            } else {
                from = mainRoad.blockList.get(mainRoad.blockList.size() - 1);
            }
            if (next != null) {
                int nextX = next.position.x * (bridgeWidth + 1);
                int nextY = next.position.y * (bridgeWidth + 1);
                Block to = gameMap.addBlock(getBlockId(nextX, nextY, bridgeWidth), currentBlock.type);

                Block[] sequence = new Block[bridgeWidth + 2];
                sequence[0] = from;
                for (int i = 1; i <= bridgeWidth; i ++) {
                    int bridgeBlockId = getBridgeBlockId(from, to, i, bridgeWidth);
                    Block bridge = gameMap.addBlock(bridgeBlockId, BlockType.MAIN_ROAD);
                    if (bridge != null) mainRoad.blockList.add(bridge);
                    sequence[i] = bridge;
                }
                sequence[bridgeWidth + 1] = to;

                setSequence(sequence);
                mainRoad.blockList.add(to);
            }
            currentBlock = next;
            if (next != null) next = next.next;
        }

        List<SegmentOld> newShortcutList = new LinkedList<>();
        for (SegmentOld shortcut: this.shortcutList) {
            int newHeadBlockId = getBlockId(
                    shortcut.head.position.x * (bridgeWidth + 1),
                    shortcut.head.position.y * (bridgeWidth + 1), bridgeWidth);
            int newTailBlockId = getBlockId(
                    shortcut.tail.position.x * (bridgeWidth + 1),
                    shortcut.tail.position.y * (bridgeWidth + 1), bridgeWidth);
            Block newHeadBlock = gameMap.addBlock(newHeadBlockId, BlockType.MAIN_ROAD);
            Block newTailBlock = gameMap.addBlock(newTailBlockId, BlockType.MAIN_ROAD);
            SegmentOld expandedShortcut = new SegmentOld(newHeadBlock, newTailBlock);

            if (shortcut.blockList.size() == 0) {
                Block[] sequence = new Block[bridgeWidth];
                for (int i = 1; i <= bridgeWidth; i ++) {
                    int bridgeBlockId = getBridgeBlockId(newHeadBlock, newTailBlock, i, bridgeWidth);
                    Block bridge = gameMap.addBlock(bridgeBlockId, BlockType.SHORTCUT);
                    if (bridge != null) expandedShortcut.blockList.add(bridge);
                    sequence[i - 1] = bridge;
                }
                setSequence(sequence);
            } else {
                //currentBlock = shortcut.hexagonList.get(0);
                //next = currentBlock.next;
                currentBlock = shortcut.head;
                next = shortcut.blockList.get(0);
                while (currentBlock != null) {
                    int x = currentBlock.position.x * (bridgeWidth + 1);
                    int y = currentBlock.position.y * (bridgeWidth + 1);

                    Block from;
                    if (expandedShortcut.blockList.size() == 0) {
                        from = gameMap.addBlock(getBlockId(x, y, bridgeWidth), currentBlock.type);
                    } else {
                        from = expandedShortcut.blockList.get(expandedShortcut.blockList.size() - 1);
                    }
                    if (next != null) {
                        int nextX = next.position.x * (bridgeWidth + 1);
                        int nextY = next.position.y * (bridgeWidth + 1);
                        Block to = gameMap.addBlock(getBlockId(nextX, nextY, bridgeWidth), currentBlock.type);

                        Block[] sequence;
                        int sequenceId = 0;
                        if (currentBlock == shortcut.head && next == shortcut.tail) {
                            sequence = new Block[bridgeWidth];
                        } else if (currentBlock == shortcut.head) {
                            sequence = new Block[bridgeWidth + 1];
                        } else if (next == shortcut.tail) {
                            sequence = new Block[bridgeWidth + 1];
                            sequence[sequenceId ++] = from;
                        } else {
                            sequence = new Block[bridgeWidth + 2];
                            sequence[sequenceId ++] = from;
                        }

                        for (int i = 1; i <= bridgeWidth; i ++) {
                            int bridgeBlockId = getBridgeBlockId(from, to, i, bridgeWidth);
                            Block bridge = gameMap.addBlock(bridgeBlockId, BlockType.SHORTCUT);
                            if (bridge != null) expandedShortcut.blockList.add(bridge);
                            sequence[sequenceId ++] = bridge;
                        }

                        if (next != shortcut.tail) sequence[sequenceId ++] = to;
                        setSequence(sequence);
                        if (to.blockId != shortcut.tail.blockId) expandedShortcut.blockList.add(to);
                    }

                    if (next == shortcut.tail) currentBlock = null;
                    else currentBlock = next;

                    if (next != null && next != shortcut.tail) {
                        if (next.next == null) next = shortcut.tail;
                        else next = next.next;
                    }
                }
            }

            newShortcutList.add(expandedShortcut);
        }
        gameMap.setMainRoad(mainRoad);
        gameMap.setShortcutList(newShortcutList);
        return gameMap;
    }

    public GameMap expandWithBorderV2(int bridgeWidth) {
        GameMap gameMap = new GameMap(
                boundX * (bridgeWidth + 1) + bridgeWidth, boundY * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);

        int startBlockId = getBlockIdWithBorder(getX(start) * (bridgeWidth + 1) + bridgeWidth,
                getY(start) * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
        int endBlockId = getBlockIdWithBorder(getX(end) * (bridgeWidth + 1) + bridgeWidth,
                getY(end) * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);

        gameMap.startBlockId = startBlockId;
        gameMap.endBlockId = endBlockId;

        List<SegmentOld> newSegmentList = new LinkedList<>();
        for (SegmentOld segment: this.segmentSet) {
            int newHeadBlockId = getBlockIdWithBorder(
                    segment.head.position.x * (bridgeWidth + 1) + bridgeWidth,
                    segment.head.position.y * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
            int newTailBlockId = getBlockIdWithBorder(
                    segment.tail.position.x * (bridgeWidth + 1) + bridgeWidth,
                    segment.tail.position.y * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
            Block newHeadBlock = gameMap.addBlock(newHeadBlockId, BlockType.MAIN_ROAD, 0, 0);
            Block newTailBlock = gameMap.addBlock(newTailBlockId, BlockType.MAIN_ROAD, 0, 0);
            SegmentOld expandedSegment= new SegmentOld(newHeadBlock, newTailBlock);

            if (segment.blockList.size() == 0) {
                Block[] sequence = new Block[bridgeWidth];
                for (int i = 1; i <= bridgeWidth; i ++) {
                    int bridgeBlockId = getBridgeBlockIdWithBorder(newHeadBlock, newTailBlock, i, bridgeWidth);
                    Block bridge = gameMap.addBlock(
                            bridgeBlockId, BlockType.MAIN_ROAD, 0, Math.min(i, bridgeWidth + 1 - i));
                    if (bridge != null) expandedSegment.blockList.add(bridge);
                    sequence[i - 1] = bridge;
                }
                setSequence(sequence);
            } else {
                Block currentBlock = segment.head;
                int index = 0;
                Block next = segment.blockList.get(index++);
                Block[] sequence = new Block[(bridgeWidth + 1) * segment.blockList.size() + bridgeWidth];
                int seguenceIndex = 0;

                while (true) {
                    int newCurrentBlockId = getBlockIdWithBorder(
                            currentBlock.position.x * (bridgeWidth + 1) + bridgeWidth,
                            currentBlock.position.y * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
                    int newNextBlockId = getBlockIdWithBorder(
                            next.position.x * (bridgeWidth + 1) + bridgeWidth,
                            next.position.y * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
                    Block newCurrentBlock = gameMap.addBlock(newCurrentBlockId, BlockType.MAIN_ROAD, 0, 0);
                    Block newNextBlock = gameMap.addBlock(newNextBlockId, BlockType.MAIN_ROAD, 0, 0);

                    for (int i = 1; i <= bridgeWidth; i++) {
                        int bridgeBlockId = getBridgeBlockIdWithBorder(newCurrentBlock, newNextBlock, i, bridgeWidth);
                        Block bridge = gameMap.addBlock(
                                bridgeBlockId, BlockType.MAIN_ROAD, 0, Math.min(i, bridgeWidth + 1 - i));
                        if (bridge != null) {
                            expandedSegment.blockList.add(bridge);
                            sequence[seguenceIndex++] = bridge;
                        }
                    }
                    if (next == segment.tail) break;

                    expandedSegment.blockList.add(newNextBlock);
                    sequence[seguenceIndex++] = newNextBlock;

                    currentBlock = next;
                    if (index == segment.blockList.size()) {
                        next = segment.tail;
                    } else {
                        next = segment.blockList.get(index++);
                    }
                }
                setSequence(sequence);
                expandedSegment.blockList.get(0).previous = expandedSegment.head;
                expandedSegment.blockList.get(expandedSegment.blockList.size() - 1).next = expandedSegment.tail;
                expandedSegment.detourDistance = (bridgeWidth + 1) * segment.detourDistance;
            }
            newSegmentList.add(expandedSegment);
        }
        gameMap.segmentList = newSegmentList;
        gameMap.printMap();
        return gameMap;
    }

    public GameMap expandWithBorder(int bridgeWidth) {
        GameMap gameMap = new GameMap(
                boundX * (bridgeWidth + 1) + bridgeWidth, boundY * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
        SegmentOld mainRoad = new SegmentOld(null);

        Block currentBlock = usedBlockMap.get(start);
        Block next = currentBlock.next;
        while (currentBlock != null) {
            int x = currentBlock.position.x * (bridgeWidth + 1) + bridgeWidth;
            int y = currentBlock.position.y * (bridgeWidth + 1) + bridgeWidth;

            Block from;
            if (mainRoad.blockList.size() == 0) {
                from = gameMap.addBlock(
                        getBlockIdWithBorder(x, y, bridgeWidth), currentBlock.type, 0, 0);
                mainRoad.blockList.add(from);
            } else {
                from = mainRoad.blockList.get(mainRoad.blockList.size() - 1);
            }
            if (next != null) {
                int nextX = next.position.x * (bridgeWidth + 1) + bridgeWidth;
                int nextY = next.position.y * (bridgeWidth + 1) + bridgeWidth;
                Block to = gameMap.addBlock(getBlockIdWithBorder(nextX, nextY, bridgeWidth), currentBlock.type, 0, 0);

                Block[] sequence = new Block[bridgeWidth + 2];
                sequence[0] = from;
                for (int i = 1; i <= bridgeWidth; i ++) {
                    int bridgeBlockId = getBridgeBlockIdWithBorder(from, to, i, bridgeWidth);
                    Block bridge = gameMap.addBlock(
                            bridgeBlockId, BlockType.MAIN_ROAD, 0, Math.min(i, bridgeWidth + 1 - i));
                    if (bridge != null) mainRoad.blockList.add(bridge);
                    sequence[i] = bridge;
                }
                sequence[bridgeWidth + 1] = to;

                setSequence(sequence);
                mainRoad.blockList.add(to);
            }
            currentBlock = next;
            if (next != null) next = next.next;
        }

        List<SegmentOld> newShortcutList = new LinkedList<>();
        for (SegmentOld shortcut: this.shortcutList) {
            int newHeadBlockId = getBlockIdWithBorder(
                    shortcut.head.position.x * (bridgeWidth + 1) + bridgeWidth,
                    shortcut.head.position.y * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
            int newTailBlockId = getBlockIdWithBorder(
                    shortcut.tail.position.x * (bridgeWidth + 1) + bridgeWidth,
                    shortcut.tail.position.y * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
            Block newHeadBlock = gameMap.addBlock(newHeadBlockId, BlockType.MAIN_ROAD, 0, 0);
            Block newTailBlock = gameMap.addBlock(newTailBlockId, BlockType.MAIN_ROAD, 0, 0);
            SegmentOld expandedShortcut = new SegmentOld(newHeadBlock, newTailBlock);

            if (shortcut.blockList.size() == 0) {
                Block[] sequence = new Block[bridgeWidth];
                for (int i = 1; i <= bridgeWidth; i ++) {
                    int bridgeBlockId = getBridgeBlockIdWithBorder(newHeadBlock, newTailBlock, i, bridgeWidth);
                    Block bridge = gameMap.addBlock(
                            bridgeBlockId, BlockType.SHORTCUT, 0, Math.min(i, bridgeWidth + 1 - i));
                    if (bridge != null) expandedShortcut.blockList.add(bridge);
                    sequence[i - 1] = bridge;
                }
                setSequence(sequence);
            } else {
                //currentBlock = shortcut.hexagonList.get(0);
                //next = currentBlock.next;
                currentBlock = shortcut.head;
                next = shortcut.blockList.get(0);
                while (currentBlock != null) {
                    int x = currentBlock.position.x * (bridgeWidth + 1) + bridgeWidth;
                    int y = currentBlock.position.y * (bridgeWidth + 1) + bridgeWidth;

                    Block from;
                    if (expandedShortcut.blockList.size() == 0) {
                        from = gameMap.addBlock(getBlockIdWithBorder(x, y, bridgeWidth), BlockType.SHORTCUT, 0, 0);
                    } else {
                        from = expandedShortcut.blockList.get(expandedShortcut.blockList.size() - 1);
                    }
                    if (next != null) {
                        int nextX = next.position.x * (bridgeWidth + 1) + bridgeWidth;
                        int nextY = next.position.y * (bridgeWidth + 1) + bridgeWidth;
                        Block to = gameMap.addBlock(
                                getBlockIdWithBorder(nextX, nextY, bridgeWidth), BlockType.SHORTCUT, 0, 0);

                        Block[] sequence;
                        int sequenceId = 0;
                        if (currentBlock == shortcut.head && next == shortcut.tail) {
                            sequence = new Block[bridgeWidth];
                        } else if (currentBlock == shortcut.head) {
                            sequence = new Block[bridgeWidth + 1];
                        } else if (next == shortcut.tail) {
                            sequence = new Block[bridgeWidth + 1];
                            sequence[sequenceId ++] = from;
                        } else {
                            sequence = new Block[bridgeWidth + 2];
                            sequence[sequenceId ++] = from;
                        }

                        for (int i = 1; i <= bridgeWidth; i ++) {
                            int bridgeBlockId = getBridgeBlockIdWithBorder(from, to, i, bridgeWidth);
                            Block bridge = gameMap.addBlock(bridgeBlockId, BlockType.SHORTCUT,
                                    0, Math.min(i, bridgeWidth + 1 - i));
                            if (bridge != null) expandedShortcut.blockList.add(bridge);
                            sequence[sequenceId ++] = bridge;
                        }

                        if (next != shortcut.tail) sequence[sequenceId ++] = to;
                        setSequence(sequence);
                        if (to.blockId != expandedShortcut.tail.blockId) expandedShortcut.blockList.add(to);
                    }

                    if (next == shortcut.tail) currentBlock = null;
                    else currentBlock = next;

                    if (next != null && next != shortcut.tail) {
                        if (next.next == null) next = shortcut.tail;
                        else next = next.next;
                    }
                }
            }
            newShortcutList.add(expandedShortcut);
        }
        gameMap.setMainRoad(mainRoad);
        gameMap.setShortcutList(newShortcutList);
        return gameMap;
    }

    private void setSequence(Block... blocks) {
        for (int i = 0; i < blocks.length; i++) {
            if (i + 1 < blocks.length) {
                blocks[i].next = blocks[i + 1];
                blocks[i + 1].previous = blocks[i];
            }
        }
    }

    private int getBridgeBlockId(Block from, Block to, int bridgeNo, int bridgeWidth) {
        if (from == null || to == null) throw new RuntimeException("from or to should not be null");
        else if (to.position.x > from.position.x) {
            return getBlockId(from.position.x + bridgeNo, from.position.y, bridgeWidth);
        } else if (to.position.x < from.position.x) {
            return getBlockId(from.position.x - bridgeNo, from.position.y, bridgeWidth);
        } else if (to.position.y > from.position.y) {
            return getBlockId(from.position.x, from.position.y + bridgeNo, bridgeWidth);
        } else if (to.position.y < from.position.y)  {
            return getBlockId(from.position.x, from.position.y - bridgeNo, bridgeWidth);
        }
        return -1;
    }

    private int getBridgeBlockIdWithBorder(Block from, Block to, int bridgeNo, int bridgeWidth) {
        if (from == null || to == null) throw new RuntimeException("from or to should not be null");
        else if (to.position.x > from.position.x) {
            return getBlockIdWithBorder(from.position.x + bridgeNo, from.position.y, bridgeWidth);
        } else if (to.position.x < from.position.x) {
            return getBlockIdWithBorder(from.position.x - bridgeNo, from.position.y, bridgeWidth);
        } else if (to.position.y > from.position.y) {
            return getBlockIdWithBorder(from.position.x, from.position.y + bridgeNo, bridgeWidth);
        } else if (to.position.y < from.position.y)  {
            return getBlockIdWithBorder(from.position.x, from.position.y - bridgeNo, bridgeWidth);
        }
        return -1;
    }

    public void printTerrainBlocks(int bridgeWidth) {
        int printBoundX = boundX * bridgeWidth - 1;
        int printBoundY = boundY * bridgeWidth - 1;
        char[][] terrainBytes = new char[printBoundX][printBoundY];
        for(int i = 0; i < printBoundX; i ++) {
            for (int j = 0; j < printBoundY; j ++) {
                terrainBytes[i][j] = ' ';
            }
        }

        for (Block block: usedBlockMap.values()) {
            int x = block.position.x * bridgeWidth;
            int y = block.position.y * bridgeWidth;

            if (block.type == BlockType.MAIN_ROAD) terrainBytes[x][y] = 'M';
            else if (block.type == BlockType.BRANCH) terrainBytes[x][y] = 'A';
            else if (block.type == BlockType.SHORTCUT) terrainBytes[x][y] = 'Y';
            else terrainBytes[x][y] = ' ';

            char brightChar = '*';
            for (int i = 1; i < bridgeWidth; i ++) {
                if (isBridgeExist(block.blockId, RIGHT) && isInPrintBound(x + i, y, bridgeWidth))
                    terrainBytes[x + i][y] = brightChar;
            }
            for (int i = 1; i < bridgeWidth; i ++) {
                if (isBridgeExist(block.blockId, DOWN) && isInPrintBound(x, y - i, bridgeWidth))
                    terrainBytes[x][y - i] = brightChar;
            }
            for (int i = 1; i < bridgeWidth; i ++) {
                if (isBridgeExist(block.blockId, LEFT) && isInPrintBound(x - i, y, bridgeWidth))
                    terrainBytes[x - i][y] = brightChar;
            }
            for (int i = 1; i < bridgeWidth; i ++) {
                if (isBridgeExist(block.blockId, UP) && isInPrintBound(x, y + i, bridgeWidth))
                    terrainBytes[x][y + i] = brightChar;
            }
        }

        for(int i = 0; i < printBoundX; i ++) {
            printLineNo(i);
            for (int j = 0; j < printBoundY; j ++) {
                System.out.print(terrainBytes[i][j]);
            }
            System.out.println();
        }

        for (SegmentOld segment : shortcutList) {
            System.out.println(segment);
        }

        System.out.println("main road length: " + usedBlockMap.get(start).distanceToFinish);
    }

    private void printLineNo(int i) {
        if (i < 10) System.out.print("000" + i + ": ");
        else if (i < 100) System.out.print("00" + i + ": ");
        else if (i < 1000) System.out.print("0" + i + ": ");
        else System.out.print(i + ": ");
    }

    public void printBlock() {
        for (Block block: usedBlockMap.values()) {
            System.out.println(block);
        }
    }
}

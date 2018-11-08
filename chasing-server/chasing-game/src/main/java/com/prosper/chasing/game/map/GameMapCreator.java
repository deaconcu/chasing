package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.Point2D;

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
    public LinkedList<Branch> shortcutList;

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
        addBlock(startBlockId, -1, BlockType.ARTERY);

        LinkedList<Integer> pathBlockIdList = new LinkedList<>();

        int blockId = startBlockId;
        pathBlockIdList.add(blockId);
        while (true) {
            int nextBlockId = getRandomUnusedSibling(blockId);
            while (nextBlockId == -1 && pathBlockIdList.size() > 0) {
                blockId = pathBlockIdList.get(ThreadLocalRandom.current().nextInt(pathBlockIdList.size()));
                //System.out.println("get block id from nodes, x:" + getX(blockId) + ", y:" + getY(blockId));

                nextBlockId = getRandomUnusedSibling(blockId);
                //System.out.println("get next block id, x:" + getX(nextBlockId) + ", y:" + getY(nextBlockId));
                if (nextBlockId == -1) {
                    pathBlockIdList.remove(new Integer(blockId));
                }
            }
            if (pathBlockIdList.size() == 0) break;

            addBlock(nextBlockId, blockId, BlockType.ARTERY);
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
                //System.out.println("add child node: " + getX(childNode.blockId) + ", " + getY(childNode.blockId));
            }
            currentNode = treeNodeList.pollFirst();
            //System.out.println("current node: " + getX(currentNode.blockId) + ", " + getY(currentNode.blockId));
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

    protected Block[] getRoadSibling(int blockId) {
        int x = getX(blockId);
        int y = getY(blockId);

        int[] validSiblings = new int[4];
        int validSiblingCount = 0;
        if (isInBound(x, y + 1) && usedBlockMap.containsKey(getBlockId(x, y + 1)))
            if (usedBlockMap.get(getBlockId(x, y + 1)).type == BlockType.ARTERY &&
                    bridgeSet.contains(getBridgeId(blockId, UP))) {
                validSiblings[validSiblingCount ++] = getBlockId(x, y + 1);
            }
        if (isInBound(x + 1, y) && usedBlockMap.containsKey(getBlockId(x + 1, y)))
            if (usedBlockMap.get(getBlockId(x + 1, y)).type == BlockType.ARTERY &&
                bridgeSet.contains(getBridgeId(blockId, RIGHT))) {
                validSiblings[validSiblingCount ++] = getBlockId(x + 1, y);
            }
        if (isInBound(x, y - 1) && usedBlockMap.containsKey(getBlockId(x, y - 1)))
            if (usedBlockMap.get(getBlockId(x, y - 1)).type == BlockType.ARTERY &&
                bridgeSet.contains(getBridgeId(blockId, DOWN))) {
                validSiblings[validSiblingCount ++] = getBlockId(x, y - 1);
            }
        if (isInBound(x - 1, y) && usedBlockMap.containsKey(getBlockId(x - 1, y)))
            if (usedBlockMap.get(getBlockId(x - 1, y)).type == BlockType.ARTERY &&
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
        if (aroundBlock != null && aroundBlock.type == BlockType.ARTERY) {
            return aroundBlock;
        }
        return null;
    }

    private int getRoadBlockIdAround(int blockId, int direction) {
        Block block = getRoadBlockAround(blockId, direction);
        if (block == null) return -1;
        return block.blockId;
    }

    protected void addBranch(Block block) {
        block.type = BlockType.BRANCH;
        usedBlockMap.put(block.blockId, block);
        unusedBlockSet.remove(block.blockId);
    }

    protected void addBlock(int blockId, int fromBlockId, BlockType type) {
        if (usedBlockMap.containsKey(blockId)) return;

        Block block = new Block(new Point2D(getX(blockId), getY(blockId)), blockId, type);
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
            if (type == BlockType.ARTERY) {
                //Thread.sleep(30);
                //printTerrainBlocks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println("add block to map, block: [x:" +
        // getX(blockId) + ", y:" + getY(blockId) + ", type: " + type + "]");
    }

    protected void removeBlock(Block block, boolean partlyBridge) {
        if (block == null) return;

        if (block.next != null) {
            block.next.previous = null;
        }
        if (block.previous != null) {
            block.previous.next = null;
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
        List<Branch> branchList = new LinkedList<>();
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

            Branch branch = new Branch(getBranchHeadTail(block, true));
            while(block != null && block.type == BlockType.BRANCH) {
                branch.addBlock(block);
                branchBlocks.remove(block);
                previous = block;
                block = block.next;
            }
            branch.tail = getBranchHeadTail(previous, false);
            if (branch.head == branch.tail || branch.head == null || branch.head.type != BlockType.ARTERY ||
                    branch.tail == null || branch.tail.type != BlockType.ARTERY) {
                for (Block branchBlock: branch.blockList) {
                    removeBlock(branchBlock, false);
                }
            } else {
                if (branch.head.distanceToFinish < branch.tail.distanceToFinish) {
                    branch.reverse();
                }
                branchList.add(branch);
            }
        }

        Collections.sort(branchList);
        for(Branch branch: branchList) {
            System.out.println(branch);
        }

        List<Branch> removeBranchList = new LinkedList<>();
        List<Branch> addBranchList = new LinkedList<>();
        for (Branch branch: branchList) {
            // 之前的交互可能让后面的分支无效, 需要删除无效的分支
            if (branch.head.type == BlockType.BRANCH || branch.tail.type == BlockType.BRANCH) {
                for (Block block: branch.blockList) {
                    removeBlock(block, false);
                }
                removeBranchList.add(branch);
                continue;
            }
            if (branch.size() > branch.getOriginDistance()) {
                Branch newBranch = exchangeBranchToRoad(branch);
                if (newBranch != null) {
                    addBranchList.add(newBranch);
                    removeBranchList.add(branch);
                }
            }
        }

        for (Branch branch: branchList) {
            // 之前的交互可能让后面的分支无效, 需要删除无效的分支
            if (branch.head.type == BlockType.BRANCH || branch.tail.type == BlockType.BRANCH) {
                for (Block block: branch.blockList) {
                    removeBlock(block, false);
                }
                if (!removeBranchList.contains(branch)) {
                    removeBranchList.add(branch);
                }
                continue;
            }
        }

        branchList.removeAll(removeBranchList);
        branchList.addAll(addBranchList);

        removeBranchList = new LinkedList<>();
        for (Branch branch: branchList) {
            if (branch.getShort() < 10) {
                if (branch.blockList.size() == 0) {
                    if (getX(branch.head.blockId) + 1 == getX(branch.tail.blockId)) {
                        bridgeSet.remove(getBridgeId(branch.head.blockId, RIGHT));
                    }
                    if (getX(branch.head.blockId) - 1 == getX(branch.tail.blockId)) {
                        bridgeSet.remove(getBridgeId(branch.head.blockId, LEFT));
                    }
                    if (getY(branch.head.blockId) + 1 == getY(branch.tail.blockId)) {
                        bridgeSet.remove(getBridgeId(branch.head.blockId, UP));
                    }
                    if (getY(branch.head.blockId) - 1 == getY(branch.tail.blockId)) {
                        bridgeSet.remove(getBridgeId(branch.head.blockId, DOWN));
                    }
                }
                for (Block block: branch.blockList) {
                    removeBlock(block, false);
                }
                if (!removeBranchList.contains(branch)) {
                    removeBranchList.add(branch);
                }
                addBranchList.remove(branch);
            }
        }
        branchList.removeAll(removeBranchList);

        removeBranchList = new LinkedList<>();
        for (Branch branch: shortcutList) {
            // 之前的交互可能让后面的分支无效, 需要删除无效的分支
            if (branch.head.type != BlockType.ARTERY || branch.tail.type != BlockType.ARTERY) {
                for (Block block: branch.blockList) {
                    removeBlock(block, false);
                }
                if (!removeBranchList.contains(branch)) {
                    removeBranchList.add(branch);
                }
                continue;
            }
        }
        shortcutList.removeAll(removeBranchList);

        for(Branch branch: branchList) {
            for(Block block: branch.blockList) {
                block.type = BlockType.SHORTCUT;
            }
            System.out.println(branch);
            shortcutList.add(branch);
        }
        Collections.sort(shortcutList);
    }

    private Branch exchangeBranchToRoad(Branch branch) {
        if (branch == null || branch.head == null || branch.tail == null || branch.blockList == null) return null;
        if (branch.size() <= branch.getOriginDistance()) {
            return null;
        }

        Block branchHead = branch.head.next;

        // create new branch with origin head and tail
        Branch trueBranch = new Branch(branch.head);
        trueBranch.tail = branch.tail;

        Block currentBlock = branchHead;
        Block previousBlock = branch.head;
        while (currentBlock != branch.tail) {
            currentBlock.type = BlockType.BRANCH;
            trueBranch.addBlock(currentBlock);
            previousBlock = currentBlock;
            currentBlock = currentBlock.next;
        }

        // disconnect new branch to road
        branchHead.previous = null;
        previousBlock.next = null;

        // connect new road to main road
        trueBranch.head.next = branch.blockList.get(0);
        branch.blockList.get(0).previous = trueBranch.head;
        trueBranch.tail.previous = branch.blockList.get(branch.blockList.size() - 1);
        branch.blockList.get(branch.blockList.size() - 1).next = trueBranch.tail;

        // recount distance and set new road block
        currentBlock = trueBranch.tail.previous;
        while(currentBlock != null) {
            currentBlock.distanceToFinish = currentBlock.next.distanceToFinish + 1;
            if (currentBlock.type != BlockType.ARTERY) currentBlock.type = BlockType.ARTERY;
            currentBlock = currentBlock.previous;
        }

        System.out.println("branch exchanged to road, road length: " + usedBlockMap.get(start).distanceToFinish);
        return trueBranch;
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
        Branch mainRoad = new Branch(null);

        Block currentBlock = usedBlockMap.get(start);
        Block next = currentBlock.next;
        while (currentBlock != null) {
            int x = currentBlock.position.x * (bridgeWidth + 1);
            int y = currentBlock.position.y * (bridgeWidth + 1);

            Block from;
            if (mainRoad.blockList.size() == 0) {
                from = gameMap.addBlock(getBlockId(x, y, bridgeWidth), currentBlock.type, TerrainType.PAVEMENT);
                mainRoad.blockList.add(from);
            } else {
                from = mainRoad.blockList.get(mainRoad.blockList.size() - 1);
            }
            if (next != null) {
                int nextX = next.position.x * (bridgeWidth + 1);
                int nextY = next.position.y * (bridgeWidth + 1);
                Block to = gameMap.addBlock(getBlockId(nextX, nextY, bridgeWidth), currentBlock.type, TerrainType.PAVEMENT);

                Block[] sequence = new Block[bridgeWidth + 2];
                sequence[0] = from;
                for (int i = 1; i <= bridgeWidth; i ++) {
                    int bridgeBlockId = getBridgeBlockId(from, to, i, bridgeWidth);
                    Block bridge = gameMap.addBlock(
                            bridgeBlockId, BlockType.ARTERY, TerrainType.PAVEMENT);
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

        List<Branch> newShortcutList = new LinkedList<>();
        for (Branch shortcut: this.shortcutList) {
            int newHeadBlockId = getBlockId(
                    shortcut.head.position.x * (bridgeWidth + 1),
                    shortcut.head.position.y * (bridgeWidth + 1), bridgeWidth);
            int newTailBlockId = getBlockId(
                    shortcut.tail.position.x * (bridgeWidth + 1),
                    shortcut.tail.position.y * (bridgeWidth + 1), bridgeWidth);
            Block newHeadBlock = gameMap.addBlock(
                    newHeadBlockId, BlockType.ARTERY, TerrainType.PAVEMENT);
            Block newTailBlock = gameMap.addBlock(
                    newTailBlockId, BlockType.ARTERY, TerrainType.PAVEMENT);
            Branch expandedShortcut = new Branch(newHeadBlock, newTailBlock);

            if (shortcut.blockList.size() == 0) {
                Block[] sequence = new Block[bridgeWidth];
                for (int i = 1; i <= bridgeWidth; i ++) {
                    int bridgeBlockId = getBridgeBlockId(newHeadBlock, newTailBlock, i, bridgeWidth);
                    Block bridge = gameMap.addBlock(
                            bridgeBlockId, BlockType.SHORTCUT, TerrainType.PAVEMENT);
                    if (bridge != null) expandedShortcut.blockList.add(bridge);
                    sequence[i - 1] = bridge;
                }
                setSequence(sequence);
            } else {
                //currentBlock = shortcut.blockList.get(0);
                //next = currentBlock.next;
                currentBlock = shortcut.head;
                next = shortcut.blockList.get(0);
                while (currentBlock != null) {
                    int x = currentBlock.position.x * (bridgeWidth + 1);
                    int y = currentBlock.position.y * (bridgeWidth + 1);

                    Block from;
                    if (expandedShortcut.blockList.size() == 0) {
                        from = gameMap.addBlock(
                                getBlockId(x, y, bridgeWidth), currentBlock.type, TerrainType.PAVEMENT);
                    } else {
                        from = expandedShortcut.blockList.get(expandedShortcut.blockList.size() - 1);
                    }
                    if (next != null) {
                        int nextX = next.position.x * (bridgeWidth + 1);
                        int nextY = next.position.y * (bridgeWidth + 1);
                        Block to = gameMap.addBlock(
                                getBlockId(nextX, nextY, bridgeWidth), currentBlock.type, TerrainType.PAVEMENT);

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
                            Block bridge = gameMap.addBlock(bridgeBlockId, BlockType.SHORTCUT, TerrainType.PAVEMENT);
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
        gameMap.setMainroad(mainRoad);
        gameMap.setShortcutList(newShortcutList);
        return gameMap;
    }

    public GameMap expandWithBorder(int bridgeWidth) {
        GameMap gameMap = new GameMap(
                boundX * (bridgeWidth + 1) + bridgeWidth, boundY * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
        Branch mainRoad = new Branch(null);

        Block currentBlock = usedBlockMap.get(start);
        Block next = currentBlock.next;
        while (currentBlock != null) {
            int x = currentBlock.position.x * (bridgeWidth + 1) + bridgeWidth;
            int y = currentBlock.position.y * (bridgeWidth + 1) + bridgeWidth;

            Block from;
            if (mainRoad.blockList.size() == 0) {
                from = gameMap.addBlock(
                        getBlockIdWithBorder(x, y, bridgeWidth), currentBlock.type, TerrainType.PAVEMENT, 0, 0);
                mainRoad.blockList.add(from);
            } else {
                from = mainRoad.blockList.get(mainRoad.blockList.size() - 1);
            }
            if (next != null) {
                int nextX = next.position.x * (bridgeWidth + 1) + bridgeWidth;
                int nextY = next.position.y * (bridgeWidth + 1) + bridgeWidth;
                Block to = gameMap.addBlock(getBlockIdWithBorder(nextX, nextY, bridgeWidth), currentBlock.type,
                        TerrainType.PAVEMENT, 0, 0);

                Block[] sequence = new Block[bridgeWidth + 2];
                sequence[0] = from;
                for (int i = 1; i <= bridgeWidth; i ++) {
                    int bridgeBlockId = getBridgeBlockIdWithBorder(from, to, i, bridgeWidth);
                    Block bridge = gameMap.addBlock(bridgeBlockId, BlockType.ARTERY,
                            TerrainType.PAVEMENT, 0, Math.min(i, bridgeWidth + 1 - i));
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

        List<Branch> newShortcutList = new LinkedList<>();
        for (Branch shortcut: this.shortcutList) {
            int newHeadBlockId = getBlockIdWithBorder(
                    shortcut.head.position.x * (bridgeWidth + 1) + bridgeWidth,
                    shortcut.head.position.y * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
            int newTailBlockId = getBlockIdWithBorder(
                    shortcut.tail.position.x * (bridgeWidth + 1) + bridgeWidth,
                    shortcut.tail.position.y * (bridgeWidth + 1) + bridgeWidth, bridgeWidth);
            Block newHeadBlock = gameMap.addBlock(newHeadBlockId, BlockType.ARTERY, TerrainType.PAVEMENT, 0, 0);
            Block newTailBlock = gameMap.addBlock(newTailBlockId, BlockType.ARTERY, TerrainType.PAVEMENT, 0, 0);
            Branch expandedShortcut = new Branch(newHeadBlock, newTailBlock);

            if (shortcut.blockList.size() == 0) {
                Block[] sequence = new Block[bridgeWidth];
                for (int i = 1; i <= bridgeWidth; i ++) {
                    int bridgeBlockId = getBridgeBlockIdWithBorder(newHeadBlock, newTailBlock, i, bridgeWidth);
                    Block bridge = gameMap.addBlock(bridgeBlockId, BlockType.SHORTCUT,
                            TerrainType.PAVEMENT, 0, Math.min(i, bridgeWidth + 1 - i));
                    if (bridge != null) expandedShortcut.blockList.add(bridge);
                    sequence[i - 1] = bridge;
                }
                setSequence(sequence);
            } else {
                //currentBlock = shortcut.blockList.get(0);
                //next = currentBlock.next;
                currentBlock = shortcut.head;
                next = shortcut.blockList.get(0);
                while (currentBlock != null) {
                    int x = currentBlock.position.x * (bridgeWidth + 1) + bridgeWidth;
                    int y = currentBlock.position.y * (bridgeWidth + 1) + bridgeWidth;

                    Block from;
                    if (expandedShortcut.blockList.size() == 0) {
                        from = gameMap.addBlock(getBlockIdWithBorder(x, y, bridgeWidth), BlockType.SHORTCUT,
                                TerrainType.PAVEMENT, 0, 0);
                    } else {
                        from = expandedShortcut.blockList.get(expandedShortcut.blockList.size() - 1);
                    }
                    if (next != null) {
                        int nextX = next.position.x * (bridgeWidth + 1) + bridgeWidth;
                        int nextY = next.position.y * (bridgeWidth + 1) + bridgeWidth;
                        Block to = gameMap.addBlock(
                                getBlockIdWithBorder(nextX, nextY, bridgeWidth), BlockType.SHORTCUT,
                                TerrainType.PAVEMENT, 0, 0);

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
                                    TerrainType.PAVEMENT, 0, Math.min(i, bridgeWidth + 1 - i));
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
        gameMap.setMainroad(mainRoad);
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

            if (block.type == BlockType.ARTERY) terrainBytes[x][y] = 'M';
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

        for (Branch branch: shortcutList) {
            System.out.println(branch);
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

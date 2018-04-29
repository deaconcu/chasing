package com.prosper.chasing.game.old;

import com.prosper.chasing.game.base.Point2D;
import com.prosper.chasing.game.util.Constant;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.prosper.chasing.game.util.Constant.Direction.*;
import static com.prosper.chasing.game.util.Constant.MapBlockType.BLOCK_AREA;

/**
 * Created by deacon on 2018/4/26.
 */
public class GameMapVersion3 {

    // 已生成的路径块
    public Map<Integer, Block> usedBlockMap;
    // 墙的集合
    public Set<Integer> bridgeSet;
    // 未生成的空白块
    protected Set<Integer> unusedBlockSet;

    public int start;

    public int end;

    public int boundX;

    public int boundY;

    public static class Block {

        public Point2D position;

        public int blockId;

        public byte type; // 0:wall 1:block area 2: road

        public int distanceToFinish;

        public Block previous;

        public Block next;

        public Block(Point2D position, int blockId, byte type) {
            this.position = position;
            this.blockId = blockId;
            this.type = type;
            this.distanceToFinish = 0;
        }

        public String toString() {
            return "block id: " + blockId + ", type: " + type + ", position x: " + position.x + ", position y: " +
                    position.y + ", distance to finish: " + distanceToFinish;
        }
    }

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

    public GameMapVersion3(int boundX, int boundY) {
        this.boundX = boundX;
        this.boundY = boundY;

        clear();
    }

    public void clear() {
        usedBlockMap = new HashMap<>();
        unusedBlockSet = new HashSet<>();
        bridgeSet = new HashSet<>();

        for (int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                unusedBlockSet.add(getBlockId(i, j));
            }
        }
    }

    protected void fillWithRandomRoads() {
        int startBlockId = randomUnusedPoint();
        addBlock(startBlockId, -1, Constant.MapBlockType.MAIN_ROAD);

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

            addBlock(nextBlockId, blockId, Constant.MapBlockType.MAIN_ROAD);
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

    public void merge(GameMapVersion3 gameMap) {
        Set<Block> removeBlockSet = new HashSet<>();
        for (Block block: gameMap.usedBlockMap.values()) {
            if (usedBlockMap.containsKey(block.blockId)) {
                removeBlockSet.add(block);
            }
        }

        for (Block block: removeBlockSet) {
            gameMap.removeBlock(block, true);
        }

        for (Block block: gameMap.usedBlockMap.values()) {
            addBlock(block);
        }
        bridgeSet.addAll(gameMap.bridgeSet);
    }

    protected void addBlock(Block block) {
        //block.type = BRANCH;
        usedBlockMap.put(block.blockId, block);
        unusedBlockSet.remove(block.blockId);
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
        if (aroundBlock != null && aroundBlock.type == Constant.MapBlockType.MAIN_ROAD) {
            return aroundBlock;
        }
        return null;
    }

    private int getRoadBlockIdAround(int blockId, int direction) {
        Block block = getRoadBlockAround(blockId, direction);
        if (block == null) return -1;
        return block.blockId;
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

    protected void addBlock(int blockId, int fromBlockId, byte type) {
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
            if (type == Constant.MapBlockType.MAIN_ROAD) {
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

    private boolean isBridgeExist(int blockId, byte direction) {
        return bridgeSet.contains(getBridgeId(blockId, direction));
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

    protected int getBlockId(int x, int y) {
        return boundX * y + x + 1;
    }

    public int getX(int id) {
        return (id - 1) % boundX;
    }

    public int getY(int id) {
        return (id - 1) / boundX;
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

            if (block.type == Constant.MapBlockType.MAIN_ROAD) terrainBytes[x][y] = 'M';
            else if (block.type == BLOCK_AREA) terrainBytes[x][y] = 'X';
            else if (block.type == Constant.MapBlockType.BRANCH) terrainBytes[x][y] = 'A';
            else if (block.type == Constant.MapBlockType.SHORTCUT) terrainBytes[x][y] = 'Y';
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
    }

    private void printLineNo(int i) {
        if (i < 10) System.out.print("000" + i + ": ");
        else if (i < 100) System.out.print("00" + i + ": ");
        else if (i < 1000) System.out.print("0" + i + ": ");
        else System.out.print(i + ": ");
    }
}

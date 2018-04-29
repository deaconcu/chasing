package com.prosper.chasing.game.old;

import com.prosper.chasing.game.base.Point2D;
import com.prosper.chasing.game.util.Constant;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.prosper.chasing.game.util.Constant.MapBlockType.*;

/**
 * Created by deacon on 2018/4/22.
 */
public class GameMapBase {

    public static class BlockAreaConfig {
        public byte minWidth;
        public byte maxWidth;
        public byte minHeight;
        public byte maxHeight;

        public BlockAreaConfig(byte minWidth, byte maxWidth, byte minHeight, byte maxHeight) {
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }
    }

    public static class BlockArea {
        public short id;
        public byte width;
        public byte height;
        Point2D leftBottomPoint;

        @Override
        public String toString() {
            return "id:" + id + ", width:" + width + ", height:" + height + ", position:[" + leftBottomPoint + "]";
        }

    }

    public static class Block {
        public Point2D position;
        public int blockId;
        public byte type; // 0:wall 1:block area 2: road
        public int distanceToFinish;

        public Block(Point2D position, int blockId, byte type) {
            this.position = position;
            this.blockId = blockId;
            this.type = type;
            this.distanceToFinish = 0;
        }
    }

    public static class Branch {
        public List<Block> blockList;
        public List<Block> crossBlockList;
        public Block header;
        public Block tail;

        public Branch(Block header) {
            blockList = new LinkedList<>();
            crossBlockList = new LinkedList<>();
            this.header = header;
        }

        public int count() {
            return blockList.size();
        }

        public void addBlock(Block block) {
            blockList.add(block);
        }

        public void addCrossBlock(Block block) {
            crossBlockList.add(block);
        }

        public void make() {
        }
    }

    // 已生成的路径块
    public Map<Integer, Block> usedBlockMap;

    public Map<Integer, Block> roadBlockMap;

    // 未生成的空白块
    protected Set<Integer> unusedBlockSet;

    // 未生成的交叉路口的空白块
    protected Set<Integer> unusedCrossBlockSet;

    protected Map<Short, BlockArea> terrainBlockAreaMap;

    public Point2D start;

    public Point2D end;

    public int boundX;

    public int boundY;

    protected int areaBlockCount = 0;
    protected int wallBlockCount = 0;
    protected int roadBlockCount = 0;

    public GameMapBase(int boundX, int boundY) {
        if (boundX % 3 != 1 || boundY % 3 != 1) {
            throw new RuntimeException("bound is not right");
        }

        this.boundX = boundX;
        this.boundY = boundY;

        clear();
        printTerrainBlocks();
        generate();
    }

    public void clear() {
        usedBlockMap = new HashMap<>();
        terrainBlockAreaMap = new HashMap<>();
        roadBlockMap = new HashMap<>();

        unusedBlockSet = new HashSet<>();
        unusedCrossBlockSet = new HashSet<>();

        areaBlockCount = 0;
        wallBlockCount = 0;
        roadBlockCount = 0;

        for (int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                if (i % 3 == 0 && j % 3 == 0) {
                    unusedCrossBlockSet.add(getBlockId(i, j));
                }

                if (i % 3 == 0 || j % 3 == 0) {
                    unusedBlockSet.add(getBlockId(i, j));
                } else {
                    addBlock(getBlockId(i, j), Constant.MapBlockType.WALL);
                }
            }
        }
    }

    protected BlockArea randomBlockArea(byte minWidth, byte maxWidth, byte minHeight, byte maxHeight) {
        BlockArea blockArea = new BlockArea();
        blockArea.width = (byte)ThreadLocalRandom.current().nextInt(minWidth, maxWidth + 1);
        blockArea.height = (byte)ThreadLocalRandom.current().nextInt(minHeight, maxHeight + 1);

        blockArea.leftBottomPoint = new Point2D(
                (ThreadLocalRandom.current().nextInt((boundX - 4) / 3) + 1) * 3 - 1,
                (ThreadLocalRandom.current().nextInt((boundX - 4) / 3) + 1) * 3 - 1
        );
        return blockArea;
    }

    /**
     * 移除分支路径
     */
    protected void removeBrandRoad(int startBlockId, int endBlockId) {
        TreeNode startNode = new TreeNode(startBlockId, null);
        TreeNode endNode = null;
        TreeNode currentNode = startNode;
        LinkedList<TreeNode> treeNodeList = new LinkedList<>();
        while(true) {
            if (currentNode == null) {
                break;
            }

            if (currentNode.blockId == endBlockId) {
                endNode = currentNode;
            }

            currentNode.addChild(getRoadClockAround(currentNode.blockId, 1));
            currentNode.addChild(getRoadClockAround(currentNode.blockId, 2));
            currentNode.addChild(getRoadClockAround(currentNode.blockId, 3));
            currentNode.addChild(getRoadClockAround(currentNode.blockId, 4));

            for (TreeNode childNode: currentNode.childs) {
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
            if (currentNode == null) {
                break;
            }
            if (!currentNode.onPath) {
                removeBlock(usedBlockMap.get(currentNode.blockId));
            }

            for (TreeNode childNode: currentNode.childs) {
                treeNodeList.add(childNode);
            }
            currentNode = treeNodeList.pollFirst();
        }
    }

    private int getRoadClockAround(int blockId, int direction) {
        Block block = null;
        if (direction == 1 && isInBound(getX(blockId) + 1, getY(blockId))) {
            block = usedBlockMap.get(getBlockId(getX(blockId) + 1, getY(blockId)));
        } else if (direction == 2  && isInBound(getX(blockId), getY(blockId) - 1)) {
            block = usedBlockMap.get(getBlockId(getX(blockId), getY(blockId) - 1));
        } else if (direction == 3  && isInBound(getX(blockId) - 1, getY(blockId))) {
            block = usedBlockMap.get(getBlockId(getX(blockId) - 1, getY(blockId)));
        } else if (direction == 4  && isInBound(getX(blockId), getY(blockId) + 1)) {
            block = usedBlockMap.get(getBlockId(getX(blockId), getY(blockId) + 1));
        }
        if (block != null && block.type == Constant.MapBlockType.MAIN_ROAD) {
            return block.blockId;
        }
        return -1;
    }

    public static class TreeNode {
        int blockId;
        List<TreeNode> childs;
        TreeNode parents;
        boolean onPath;

        public TreeNode(int blockId, TreeNode parents) {
            this.blockId = blockId;
            this.parents = parents;
            childs = new LinkedList<>();
            onPath = false;
        }

        public void addChild(int childBlockId) {
            if (childBlockId == -1 || (parents != null && childBlockId == parents.blockId)) return;
            childs.add(new TreeNode(childBlockId, this));
        }

        @Override
        public String toString() {
            String childString = "";
            for (TreeNode treeNode: childs) {
                childString += treeNode.blockId + ",";
            }
            return "block id:" + blockId + ", child: [" + childString + "]";
        }
    }

    /**
     * 返回一个随机的交叉路口的空白点
     * @return
     */
    protected int randomUnusedCrossPoint() {
        int choise = ThreadLocalRandom.current().nextInt(unusedCrossBlockSet.size());
        for (int blockId: unusedCrossBlockSet) {
            if (choise -- == 0) {
                return blockId;
            }
        }
        return -1;
    }

    protected int randomUnusedPoint() {
        int choise = ThreadLocalRandom.current().nextInt(unusedBlockSet.size());
        for (int blockId: unusedBlockSet) {
            if (choise -- == 0) {
                return blockId;
            }
        }
        return -1;
    }

    protected int getDistance(int blockIdA, int blockIdB) {
        return (int) Math.hypot(
                getX(blockIdA) - getX(blockIdB), getY(blockIdA) - getY(blockIdB));
    }

    protected void markDistance(int startBlockId, int endBlockId) {
        int currentBlockId = endBlockId;
        int lastBlockId = -1;
        int length = 0;
        while(currentBlockId != startBlockId && currentBlockId != -1) {
            int nextBlockId = -1;
            for (int i = 1; i <= 4; i ++) {
                nextBlockId = getRoadClockAround(currentBlockId, i);
                if (nextBlockId != -1 && nextBlockId != lastBlockId) {
                    break;
                }
            }
            usedBlockMap.get(currentBlockId).distanceToFinish = length ++;
            lastBlockId = currentBlockId;
            currentBlockId = nextBlockId;
        }
    }

    protected void generateBranches() {
        int i = 200;
        while(i > 0) {
            Branch branch = generateBranch();
            if (branch != null) {
                branch.make();
                i --;
            }
        }
    }

    protected Branch generateBranch() {
        Block start = getRandomRoadBlock(true);
        Branch branch = new Branch(start);

        Block current = start;
        while (true) {
            int nextBlockId;
            if (current == start) {
                nextBlockId = getRandomCrossPointSibling(getX(current.blockId), getY(current.blockId));
            } else {
                nextBlockId = getRandomCrossPointSibling(getX(current.blockId), getY(current.blockId), MAIN_ROAD, SHORTCUT);
            }

            if (nextBlockId == -1) {
                System.out.println("x:" + getX(current.blockId) + ", Y:" + getY(current.blockId));
                printTerrainBlocks();
                branch.tail = null;
                break;
            }

            if (usedBlockMap.get(nextBlockId) != null && (usedBlockMap.get(nextBlockId).type == MAIN_ROAD ||
                    usedBlockMap.get(nextBlockId).type == SHORTCUT)) {
                System.out.println("x:" + getX(current.blockId) + ", Y:" + getY(current.blockId));
                printTerrainBlocks();
                branch.tail = usedBlockMap.get(nextBlockId);
                break;
            }

            Block block = new Block(new Point2D(getX(nextBlockId), getY(nextBlockId)), nextBlockId, SHORTCUT);
            branch.addCrossBlock(block);
            addBlock(nextBlockId, BRANCH);
            current = block;

            printTerrainBlocks();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (branch.tail == null) {
            for (Block block : branch.crossBlockList) {
                removeBlock(block);
            }
            return null;
        }
        return branch;
    }

    private Block getRandomRoadBlock(boolean isCrossPoint) {
        while(true) {
            int hitIndex = ThreadLocalRandom.current().nextInt(roadBlockMap.size());
            int index = 0;
            for (Block block: roadBlockMap.values()) {
                if (hitIndex == index ++) {
                    if (isCrossPoint) {
                        if (getX(block.blockId) % 3 == 0 && getY(block.blockId) % 3 == 0) {
                            return block;
                        } else {
                            continue;
                        }
                    } else {
                        return block;
                    }
                }
            }
        }
    }

    protected void generateLine(int startBlockId, int endBlockId) {

    }

    protected void putTerrainBlockArea(BlockArea blockArea, List<Integer> forbidBlockIdList) {
        for (int i = blockArea.leftBottomPoint.x; i < blockArea.leftBottomPoint.x + blockArea.width; i ++) {
            for (int j = blockArea.leftBottomPoint.y; j < blockArea.leftBottomPoint.y + blockArea.height; j ++) {
                if (forbidBlockIdList.contains(getBlockId(i, j))) {
                    //System.out.println("block rejected: " + blockArea);
                    return;
                }

                Block block = usedBlockMap.get(getBlockId(i, j));
                if(block != null && (block.type == BLOCK_AREA ||
                        block.type == Constant.MapBlockType.MAIN_ROAD)) {
                    //System.out.println("block rejected: " + blockArea);
                    return;
                }
            }
        }

        blockArea.id = (short) (terrainBlockAreaMap.size() + 1);
        terrainBlockAreaMap.put(blockArea.id, blockArea);
        //System.out.println("block accepted: " + blockArea);

        for (int i = blockArea.leftBottomPoint.x; i < blockArea.leftBottomPoint.x + blockArea.width; i ++) {
            for (int j = blockArea.leftBottomPoint.y; j < blockArea.leftBottomPoint.y + blockArea.height; j ++) {
                addBlock(getBlockId(i, j), BLOCK_AREA);
            }
        }

        int areaWallX = blockArea.leftBottomPoint.x - 1;
        int areaWallY = blockArea.leftBottomPoint.y - 1;

        do {
            if (isInBound(areaWallX, areaWallY)) {
                addBlock(getBlockId(areaWallX, areaWallY), Constant.MapBlockType.WALL);
            }

            if (areaWallX < blockArea.leftBottomPoint.x + blockArea.width &&
                    areaWallY == blockArea.leftBottomPoint.y - 1) {
                areaWallX ++;
            } else if (areaWallX == blockArea.leftBottomPoint.x + blockArea.width &&
                    areaWallY < blockArea.leftBottomPoint.y + blockArea.height) {
                areaWallY ++;
            } else if (areaWallY == blockArea.leftBottomPoint.y + blockArea.height &&
                    areaWallX > blockArea.leftBottomPoint.x - 1) {
                areaWallX --;
            } else {
                areaWallY --;
            }
        } while(areaWallX != blockArea.leftBottomPoint.x - 1 || areaWallY != blockArea.leftBottomPoint.y - 1);
    }

    protected int getRandomCrossPointSibling(int x, int y, byte... validTypes) {
        if (x % 3 != 0 || y % 3 != 0) throw new RuntimeException("param is not right");
        int[] validSiblings = new int[4];
        int validSiblingCount = 0;

        if (checkValidSibling(x, y + 3, validTypes))
            validSiblings[validSiblingCount++] = getBlockId(x, y + 3);
        if (checkValidSibling(x + 3, y, validTypes))
            validSiblings[validSiblingCount ++] = getBlockId(x + 3, y);
        if (checkValidSibling(x, y - 3, validTypes))
            validSiblings[validSiblingCount ++] = getBlockId(x, y - 3);
        if (checkValidSibling(x - 3, y, validTypes))
            validSiblings[validSiblingCount ++] = getBlockId(x - 3, y);

        if (validSiblingCount == 0) return -1;
        else return validSiblings[ThreadLocalRandom.current().nextInt(validSiblingCount)];
    }

    protected int getRandomSibling(int x, int y, byte... excludeTypes) {
        int[] validSiblings = new int[4];
        int validSiblingCount = 0;

        if (checkValidSibling(x, y + 1))
            validSiblings[validSiblingCount++] = getBlockId(x, y + 1);
        if (checkValidSibling(x + 1, y))
            validSiblings[validSiblingCount ++] = getBlockId(x + 1, y);
        if (checkValidSibling(x, y - 1))
            validSiblings[validSiblingCount ++] = getBlockId(x, y - 1);
        if (checkValidSibling(x - 1, y))
            validSiblings[validSiblingCount ++] = getBlockId(x - 1, y);

        if (validSiblingCount == 0) return -1;
        else return validSiblings[ThreadLocalRandom.current().nextInt(validSiblingCount)];
    }

    private boolean checkValidSibling(int x, int y, byte... validTypes) {
        if (!isInBound(x, y)) return false;
        else if (unusedBlockSet.contains(getBlockId(x, y))) return true;
        else {
            Block block = usedBlockMap.get(getBlockId(x, y));
            if (validTypes != null) {
                for (byte type: validTypes) {
                    if (block.type == type) return true;
                }
            }
        }
        return false;
    }

    protected void makeWall(int x, int y, boolean isX) {
        if (isX) {
            for (int i = -2; i < 3; i ++) {
                if (!isInBound(x, y + i)) continue;
                if (unusedBlockSet.contains(getBlockId(x, y + i))) {
                    addBlock(getBlockId(x, y + i), Constant.MapBlockType.WALL);
                }
            }
        } else {
            for (int i = -2; i < 3; i ++) {
                if (!isInBound(x + i, y)) continue;
                if (unusedBlockSet.contains(getBlockId(x + i, y))) {
                    addBlock(getBlockId(x + i, y), Constant.MapBlockType.WALL);
                }
            }
        }
    }

    protected void makeRoad() {
        int startBlockId = randomUnusedCrossPoint();
        addBlock(startBlockId, Constant.MapBlockType.MAIN_ROAD);

        LinkedList<Integer> nodes = new LinkedList<>();

        int blockId = startBlockId;
        nodes.add(blockId);
        while (true) {
            int nextBlockId = getRandomUnusedSiblingTarget(blockId);
            while (nextBlockId == -1 && nodes.size() > 0) {
                if (nodes.size() > 0) {
                    blockId = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
                    //System.out.println("get block id from nodes, x:" + getX(blockId) + ", y:" + getY(blockId));
                } else {
                    blockId = unusedBlockSet.iterator().next();
                    //System.out.println("get block id from unused, x:" + getX(blockId) + ", y:" + getY(blockId));
                }
                nextBlockId = getRandomUnusedSiblingTarget(blockId);
                //System.out.println("get next block id, x:" + getX(nextBlockId) + ", y:" + getY(nextBlockId));
                if (nextBlockId == -1) {
                    nodes.remove(new Integer(blockId));
                }
            }
            if (nodes.size() == 0) break;

            int x = getX(blockId);
            int y = getY(blockId);
            int nextX = getX(nextBlockId);
            int nextY = getY(nextBlockId);

            if (nextX > x) {
                for (int i = x + 1; i <= nextX; i ++) {
                    addBlock(getBlockId(i, nextY), Constant.MapBlockType.MAIN_ROAD);
                }
            } else if (nextX < x) {
                for (int i = x - 1; i >= nextX; i--) {
                    addBlock(getBlockId(i, nextY), Constant.MapBlockType.MAIN_ROAD);
                }
            } else if (nextY > y) {
                for (int i = y + 1; i <= nextY; i ++) {
                    addBlock(getBlockId(nextX, i), Constant.MapBlockType.MAIN_ROAD);
                }
            } else if (nextY < y) {
                for (int i = y - 1; i >= nextY; i --) {
                    addBlock(getBlockId(nextX, i), Constant.MapBlockType.MAIN_ROAD);
                }
            }
            blockId = nextBlockId;
            if (!nodes.contains(blockId)) {
                nodes.add(blockId);
            }
        }
    }

    protected int getRoadLength(int startBlockId, int endBlockId) {
        int currentBlockId = startBlockId;
        int lastBlockId = -1;
        int length = 0;
        while(currentBlockId != endBlockId && currentBlockId != -1) {
            int nextBlockId = -1;
            for (int i = 1; i <= 4; i ++) {
                nextBlockId = getRoadClockAround(currentBlockId, i);
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

    protected void makeRoadsVersion2() {
        LinkedList<PathNode>[] paths = new LinkedList[1];
        for (int i = 0; i < 1; i ++) {
            paths[i] = makeSingleRoads();
        }

        for (LinkedList<PathNode> path : paths) {
            if (path == null) continue;
            makeRoadByPath(path);
        }
    }

    private void makeRoadByPath(LinkedList<PathNode> path) {
        int currentBlockId = -1;
        int nextBlockId = -1;
        for (PathNode pathNode: path) {
            currentBlockId = pathNode.blockId;
            nextBlockId = pathNode.nextBlockId;

            int xMin = Math.min(getX(currentBlockId), getX(nextBlockId));
            int xMax = Math.max(getX(currentBlockId), getX(nextBlockId));

            int yMin = Math.min(getY(currentBlockId), getY(nextBlockId));
            int yMax = Math.max(getY(currentBlockId), getY(nextBlockId));

            for (int i = xMin; i <= xMax; i ++) {
                for (int j = yMin; j <= yMax; j++) {
                    addBlock(getBlockId(i, j), BLOCK_AREA);
                }
            }
        }
    }

    protected LinkedList<PathNode> makeSingleRoads() {
        int startBlockId = getBlockId(3, 3);
        int endBlockId = getBlockId(96, 96);

        LinkedList<PathNode> pathStack = new LinkedList<>();

        PathNode currentBlockNode = new PathNode(startBlockId);
        pathStack.add(currentBlockNode);
        while(true) {
            PathNode nextPathNode = getRandomUnusedSiblingNode(currentBlockNode);
            if (nextPathNode != null) {
                if (nextPathNode.blockId == endBlockId) {
                    return pathStack;
                }
                pathStack.addLast(nextPathNode);
                currentBlockNode = nextPathNode;
                unusedBlockSet.remove(nextPathNode.blockId);
            } else {
               unusedBlockSet.add(pathStack.pollLast().blockId);
               currentBlockNode = pathStack.peekLast();

               if (currentBlockNode == null) {
                   System.out.println("can't find path");
                   return null;
               }

               if (currentBlockNode.currentDirection == 1) {
                   currentBlockNode.right = false;
               } else if (currentBlockNode.currentDirection == 2) {
                   currentBlockNode.bottom= false;
               } else if (currentBlockNode.currentDirection == 3) {
                   currentBlockNode.left = false;
               } else if (currentBlockNode.currentDirection == 4) {
                   currentBlockNode.top = false;
               }
            }
            printPath(pathStack);
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void printPath(LinkedList<PathNode> pathStack) {
        char[][] terrainBytes = new char[boundX][boundY];
        for(int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                terrainBytes[i][j] = ' ';
            }
        }

        for (Block block: usedBlockMap.values()) {
            if (block.position.x >= 100 || block.position.y >= 100) {
                System.out.println("error: [x:" + block.position.x + ", y:" + block.position.y + ", type: " + block.type + "]");
            }
            if (block.position.x < 0 || block.position.y < 0) {
                System.out.println("error: [x:" + block.position.x + ", y:" + block.position.y + ", type: " + block.type + "]");
            }
            if (block.type == Constant.MapBlockType.MAIN_ROAD) {
                terrainBytes[block.position.x][block.position.y] = 'z';
            } else if (block.type == BLOCK_AREA){
                terrainBytes[block.position.x][block.position.y] = 'X';
            } else {
                terrainBytes[block.position.x][block.position.y] = ' ';
            }
        }

        for (PathNode pathNode: pathStack) {
            terrainBytes[getX(pathNode.blockId)][getY(pathNode.blockId)] = 'z';
        }

        for(int i = 0; i < boundX; i ++) {
            printLineNo(i);
            for (int j = 0; j < boundY; j ++) {
                System.out.print(terrainBytes[i][j]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    public static class PathNode {
        public int blockId;
        public int nextBlockId;
        public int currentDirection = 0;
        public boolean left = true;
        public boolean right = true;
        public boolean top = true;
        public boolean bottom = true;

        public PathNode(int blockId) {
            this.blockId = blockId;
        }
    }

    protected PathNode getRandomUnusedSiblingNode(PathNode currentBlockNode) {
        int x = getX(currentBlockNode.blockId);
        int y = getY(currentBlockNode.blockId);

        int[] validSiblings = new int[4];
        int validSiblingCount = 0;
        if (currentBlockNode.top && isInBound(x, y + 3) && unusedBlockSet.contains(getBlockId(x, y + 3)))
            validSiblings[validSiblingCount ++] = getBlockId(x, y + 3);
        if (currentBlockNode.right && isInBound(x + 3, y) && unusedBlockSet.contains(getBlockId(x + 3, y)))
            validSiblings[validSiblingCount ++] = getBlockId(x + 3, y);
        if (currentBlockNode.bottom && isInBound(x, y - 3) && unusedBlockSet.contains(getBlockId(x, y - 3)))
            validSiblings[validSiblingCount ++] = getBlockId(x, y - 3);
        if (currentBlockNode.left && isInBound(x - 3, y) && unusedBlockSet.contains(getBlockId(x - 3, y)))
            validSiblings[validSiblingCount ++] = getBlockId(x - 3, y);

        if (validSiblingCount == 0) return null;
        int nextBlockId = validSiblings[ThreadLocalRandom.current().nextInt(validSiblingCount)];
        if (getX(nextBlockId) > x) {
            currentBlockNode.currentDirection = 1;
        } else if (getY(nextBlockId) < y) {
            currentBlockNode.currentDirection = 2;
        } else if (getX(nextBlockId) < x) {
            currentBlockNode.currentDirection = 3;
        } else if (getY(nextBlockId) > y) {
            currentBlockNode.currentDirection = 4;
        }
        currentBlockNode.nextBlockId = nextBlockId;
        return new PathNode(nextBlockId);
    }

    protected int getRandomUnusedSiblingTarget(int blockId) {
        int x = getX(blockId);
        int y = getY(blockId);

        int[] validSiblings = new int[4];
        int validSiblingCount = 0;
        if (isInBound(x, y + 3) && unusedBlockSet.contains(getBlockId(x, y + 3)))
            validSiblings[validSiblingCount ++] = getBlockId(x, y + 3);
        if (isInBound(x + 3, y) && unusedBlockSet.contains(getBlockId(x + 3, y)))
            validSiblings[validSiblingCount ++] = getBlockId(x + 3, y);
        if (isInBound(x, y - 3) && unusedBlockSet.contains(getBlockId(x, y - 3)))
            validSiblings[validSiblingCount ++] = getBlockId(x, y - 3);
        if (isInBound(x - 3, y) && unusedBlockSet.contains(getBlockId(x - 3, y)))
            validSiblings[validSiblingCount ++] = getBlockId(x - 3, y);

        if (validSiblingCount == 0) return -1;
        else return validSiblings[ThreadLocalRandom.current().nextInt(validSiblingCount)];
    }

    protected void makeRoad(int blockId) {
        if (!unusedBlockSet.contains(blockId)) return;

        while (blockId != -1) {
            int x = getX(blockId);
            int y = getY(blockId);
            addBlock(blockId, Constant.MapBlockType.MAIN_ROAD);


            if (isInBound(x, y + 1) && usedBlockMap.containsKey(getBlockId(x, y + 1))
                    && usedBlockMap.get(getBlockId(x, y + 1)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x, y + 1, false);
            if (isInBound(x + 1, y) && usedBlockMap.containsKey(getBlockId(x + 1, y))
                    && usedBlockMap.get(getBlockId(x + 1, y)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x + 1, y, true);
            if (isInBound(x, y - 1) && usedBlockMap.containsKey(getBlockId(x, y - 1))
                    && usedBlockMap.get(getBlockId(x, y - 1)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x, y - 1, false);
            if (isInBound(x - 1, y) && usedBlockMap.containsKey(getBlockId(x - 1, y))
                    && usedBlockMap.get(getBlockId(x - 1, y)).type == Constant.MapBlockType.MAIN_ROAD)
                makeWall(x - 1, y, true);

            blockId = getRandomSibling(x, y);
        }
    }

    protected void addBlock(int blockId, byte type) {
        if (usedBlockMap.containsKey(blockId) &&
                (usedBlockMap.get(blockId).type == BLOCK_AREA ||
                usedBlockMap.get(blockId).type == Constant.MapBlockType.MAIN_ROAD)) {
            return;
        }
        if (usedBlockMap.containsKey(blockId) &&
                usedBlockMap.get(blockId).type == BLOCK_AREA) {
            return;
        }

        Block block = new Block(new Point2D(getX(blockId), getY(blockId)), blockId, type);
        usedBlockMap.put(blockId, block);
        unusedBlockSet.remove(blockId);
        unusedCrossBlockSet.remove(blockId);

        if (type == BLOCK_AREA) {
            areaBlockCount ++;
        } else if (type == Constant.MapBlockType.WALL) {
            wallBlockCount ++;
        } else if (type == Constant.MapBlockType.MAIN_ROAD) {
            roadBlockCount ++;
            roadBlockMap.put(blockId, block);
        }

        try {
            if (type == Constant.MapBlockType.MAIN_ROAD) {
                //Thread.sleep(30);
                //printTerrainBlocks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println("add block to map, block: [x:" + x + ", y:" + y + ", type: " + type + "]");
    }

    protected void removeBlock(Block block) {
        usedBlockMap.remove(block.blockId);
        roadBlockMap.remove(block.blockId);
        unusedBlockSet.add(block.blockId);
        if (getX(block.blockId) % 3 == 0 && getY(block.blockId) % 3 == 0) {
            unusedCrossBlockSet.add(block.blockId);
        }

        if (block.type == BLOCK_AREA) {
            areaBlockCount --;
        } else if (block.type == Constant.MapBlockType.WALL) {
            wallBlockCount --;
        } else if (block.type == Constant.MapBlockType.MAIN_ROAD) {
            roadBlockCount --;
        }
    }

    protected int getBlockId(int x, int y) {
        return boundX * y + x + 1;
    }

    protected int getX(int id) {
        return (id - 1) % boundX;
    }

    protected int getY(int id) {
        return (id - 1) / boundX;
    }

    private boolean isInBound(int x, int y) {
        if (x < 0 || x >= boundX) return false;
        if (y < 0 || y >= boundY) return false;
        return true;
    }

    public void printTerrainBlocks() {
        char[][] terrainBytes = new char[boundX][boundY];
        for(int i = 0; i < boundX; i ++) {
            for (int j = 0; j < boundY; j ++) {
                terrainBytes[i][j] = ' ';
            }
        }

        for (Block block: usedBlockMap.values()) {
            if (block.position.x >= 100 || block.position.y >= 100) {
                System.out.println("error: [x:" + block.position.x + ", y:" + block.position.y + ", type: " + block.type + "]");
            }
            if (block.position.x < 0 || block.position.y < 0) {
                System.out.println("error: [x:" + block.position.x + ", y:" + block.position.y + ", type: " + block.type + "]");
            }
            if (block.type == Constant.MapBlockType.MAIN_ROAD) {
                terrainBytes[block.position.x][block.position.y] = 'Z';
            } else if (block.type == BLOCK_AREA){
                terrainBytes[block.position.x][block.position.y] = 'X';
            } else if (block.type == Constant.MapBlockType.BRANCH){
                terrainBytes[block.position.x][block.position.y] = 'A';
            } else if (block.type == Constant.MapBlockType.SHORTCUT){
                terrainBytes[block.position.x][block.position.y] = 'B';
            } else {
                terrainBytes[block.position.x][block.position.y] = ' ';
            }
        }

        /*
        for (int blockId: unusedBlockSet) {
            terrainBytes[getX(blockId)][getY(blockId)] = 's';
        }
        */

        for(int i = 0; i < boundX; i ++) {
            printLineNo(i);
            for (int j = 0; j < boundY; j ++) {
                System.out.print(terrainBytes[i][j]);
                System.out.print(" ");
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

    public void generate() {

    }


}

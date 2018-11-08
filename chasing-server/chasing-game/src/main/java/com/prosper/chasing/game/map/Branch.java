package com.prosper.chasing.game.map;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 主路的Branch head和tail都是null，支路的Branch blockList不包括主路上的交叉节点，head和tail为交叉节点
 * Created by deacon on 2018/4/28.
 */
public class Branch implements Comparable<Branch> {

    public List<Block> blockList;
    public Block head;
    public Block tail;

    public Branch(Block head) {
        blockList = new ArrayList<>();
        this.head = head;
        tail = null;
    }

    public Branch(Block head, Block tail) {
        this(head);
        this.tail = tail;
    }

    public int size() {
        return blockList.size();
    }

    public void addBlock(Block block) {
        blockList.add(block);
    }

    public int getOriginDistance() {
        return head.distanceToFinish - tail.distanceToFinish;
    }

    public int getShort() {
        return getOriginDistance() - size();
    }

    public void reverse() {
        Block lastBlock = null;
        for (Block block: blockList) {
            if (block.next == null) {
                lastBlock = block;
            }
        }

        ArrayList<Block> blockList = new ArrayList<>();
        Block currentBlock = lastBlock;
        Block previousBlock = null;
        Block nextBlock;
        while(currentBlock != null) {
            blockList.add(currentBlock);
            nextBlock = currentBlock.previous;
            currentBlock.previous = previousBlock;
            currentBlock.next = nextBlock;

            previousBlock = currentBlock;
            currentBlock = nextBlock;
        }

        this.blockList = blockList;

        Block temp = head;
        head = tail;
        tail = temp;
    }

    @Override
    public int compareTo(Branch branch) {
        return new Integer(Math.abs(branch.getShort())).compareTo(Math.abs(this.getShort()));
    }

    @Override
    public String toString() {
        int shortLength = getOriginDistance() - size();
        return "distance: " + size() + ", original distance: " + getOriginDistance() + ", short: " + shortLength;
    }
}

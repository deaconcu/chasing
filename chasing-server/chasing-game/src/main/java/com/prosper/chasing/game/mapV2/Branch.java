package com.prosper.chasing.game.mapV2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by deacon on 2019/5/14.
 */
public class Branch {

    public List<Block> blockList;
    public Block head;
    public Block tail;
    public int detourDistance;

    public Branch(Block head) {
        blockList = new ArrayList<>();
        this.head = head;
        tail = null;
    }

    public Branch(Block head, Block tail) {
        this(head);
        this.tail = tail;
    }

    public int distance() {
        return blockList.size() + 1;
    }

    public int getExtraDistance() {
        return detourDistance - distance();
    }

    public  int getExtraDistanceRate() {
        return getExtraDistance() * 100 / distance();
    }

    public void add(Block block) {
        blockList.add(block);
    }

    @Override
    public String toString() {
        return head.getId()+ "[" + head.getX() + "," + head.getY() + "], "
                + "\t" + tail.getId()+ "[" + tail.getX() + "," + tail.getY() + "], "
                + "\tdistance: " + distance() + ", \tdetour distance: " + detourDistance;
    }
}

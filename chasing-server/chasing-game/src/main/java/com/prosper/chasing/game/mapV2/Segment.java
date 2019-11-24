package com.prosper.chasing.game.mapV2;

import com.prosper.chasing.game.map.Hexagon;

/**
 * Created by deacon on 2019/5/14.
 */
public class Segment {

    private Block h1;

    private Block h2;

    public Segment(Block h1, Block h2) {
        this.h1 = h1;
        this.h2 = h2;
    }

    public static int getId(Block h1, Block h2) {
        return getId(h1.getId(), h2.getId());
    }

    public static int getId(int id1, int id2) {
        if (id1 <= id2) return id1 * 10000 + id2;
        else return id2 * 10000 + id1;
    }

    public Block getH1() {
        return h1;
    }

    public Block getH2() {
        return h2;
    }
}

package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.GameObject;
import com.prosper.chasing.game.base.Point;
import com.prosper.chasing.game.util.ByteBuilder;

/**
 * Created by deacon on 2018/10/18.
 */
public class Lamp extends GameObject {

    // 周围灯的id数组
    private int[] siblings;

    // 周围灯的数量
    private byte siblingsSize;

    public Lamp(int id, Point point, int rotateY) {
        super(id, point, rotateY);

        this.siblings = new int[4];
        siblingsSize = 0;
    }

    public void addSiblings(int blockId) {
        if (blockId ==  id) {
            int a = 1;
        }
        if (siblingsSize >= 4) throw new RuntimeException("lamp siblings size exceed limit");
        siblings[siblingsSize ++] = blockId;
    }

    public void getBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(id);
        byteBuilder.append((short)point.x);
        byteBuilder.append((short)point.y);
        byteBuilder.append((short)rotateY);
        byteBuilder.append(siblingsSize);
        for (int i = 0; i < siblingsSize; i ++) {
            byteBuilder.append(siblings[i]);
        }
    }

    public int[] getSiblings() {
        return siblings;
    }

    public byte getSiblingsSize() {
        return siblingsSize;
    }
}

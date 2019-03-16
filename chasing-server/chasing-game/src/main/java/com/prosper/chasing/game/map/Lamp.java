package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.GameObject;
import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.util.ByteBuilder;

/**
 * Created by deacon on 2018/10/18.
 */
public class Lamp extends GameObject {

    // 周围灯的id数组
    private int[] siblings;

    // 周围灯的数量
    private byte siblingsSize;

    public Lamp(int id, Point3 point3, int rotateY) {
        super(id, point3, rotateY);

        this.siblings = new int[3];
        siblingsSize = 0;
    }

    public void addSiblings(int blockId) {
        if (siblingsSize > 3) throw new RuntimeException("lamp siblings distance exceed limit");
        siblings[siblingsSize ++] = blockId;
    }

    public void getBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(getId());
        byteBuilder.append(getPoint3().x);
        byteBuilder.append(getPoint3().z);
        byteBuilder.append(getRotateY());
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

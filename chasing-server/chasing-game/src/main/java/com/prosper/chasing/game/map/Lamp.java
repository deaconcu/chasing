package com.prosper.chasing.game.map;

import com.prosper.chasing.game.base.GameObject;
import com.prosper.chasing.game.base.Point3;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Enums;

/**
 * Created by deacon on 2018/10/18.
 */
public class Lamp extends GameObject {

    private Enums.LampType type;

    // 周围灯的id数组
    private int[] siblings;

    // 周围灯的数量
    private byte siblingsSize;

    public Lamp(int id, Enums.LampType type, Point3 point3, int rotateY) {
        super(id, point3, rotateY);
        this.type = type;
        this.siblings = new int[3];
        siblingsSize = 0;
    }

    public void addSiblings(int blockId) {
        for (int sibling: siblings) {
            if(blockId == sibling) return;
        }
        if (siblingsSize >= 3) {
            throw new RuntimeException("lamp siblings size exceed limit");
        }
        siblings[siblingsSize ++] = blockId;
    }

    public void getBytes(ByteBuilder byteBuilder) {
        byteBuilder.append(getId());
        byteBuilder.append(type.getValue());
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

package com.prosper.chasing.game.util;

public class ByteBuilder {

    private static int INIT_SIZE = 100;
    private byte[] innerBytes;
    private int pos;

    public ByteBuilder() {
        innerBytes = new byte[INIT_SIZE];
        pos = 0;
    }

    public void append(int value) {
        append(new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value});
    }

    public void append(byte[] bytes) {
        int contentSize = pos + bytes.length;
        if (contentSize > innerBytes.length) {
            expand(contentSize * 2);
        }
        for (byte b: bytes) {
            innerBytes[++ pos] = b;
        }
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[pos + 1];
        for (int i = 0; i <= pos; i ++) {
            bytes[i] = innerBytes[i];
        }
        return bytes;
    }

    private void expand(int size) {
        byte[] expandBytes = new byte[size];
        int i = 0;
        for (byte b: innerBytes) {
            expandBytes[i] = b;
        }
        this.innerBytes = expandBytes;
    }

}
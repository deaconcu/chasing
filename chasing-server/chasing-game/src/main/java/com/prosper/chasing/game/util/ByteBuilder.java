package com.prosper.chasing.game.util;

public class ByteBuilder {

    private static int INIT_SIZE = 100;
    private byte[] innerBytes;
    private int pos;

    public ByteBuilder() {
        innerBytes = new byte[INIT_SIZE];
        pos = -1;
    }

    public void append(short value) {
        append(new byte[] {
                (byte)(value >>> 8),
                (byte)value});
    }

    public void append(int value) {
        append(new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value});
    }

    public void append(long value) {
        append(new byte[] {
                (byte)(value >>> 56),
                (byte)(value >>> 48),
                (byte)(value >>> 40),
                (byte)(value >>> 32),
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value});
    }

    public void append(byte b) {
        byte[] bytes = new byte[]{b};
        append(bytes);
    }

    public void append(byte[] bytes) {
        int contentSize = pos + bytes.length;
        if (contentSize >= innerBytes.length - 1) {
            expand(innerBytes.length * 2);
        }
        for (byte b: bytes) {
            innerBytes[++ pos] = b;
        }
    }

    public void set(byte[] bytes, int index) {
        if (index >= innerBytes.length || index < 0) {
            throw new RuntimeException("index is out of range");
        }
        for (byte b: bytes) {
            innerBytes[index ++] = b;
        }
    }

    public void set(short value, int index) {
        set(new byte[] {
                (byte)(value >>> 8),
                (byte)value}, index);
    }

    public void set(int value, int index) {
        set(new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value}, index);
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[pos + 1];
        for (int i = 0; i <= pos; i ++) {
            bytes[i] = innerBytes[i];
        }
        return bytes;
    }

    public int getSize() {
        return pos + 1;
    }

    private void expand(int size) {
        byte[] expandBytes = new byte[size];
        int i = 0;
        for (byte b: innerBytes) {
            expandBytes[i ++] = b;
        }
        this.innerBytes = expandBytes;
    }

}

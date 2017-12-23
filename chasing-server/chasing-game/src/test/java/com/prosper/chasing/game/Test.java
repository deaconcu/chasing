package com.prosper.chasing.game;

public class Test {
    public static void main(String... args) {
        transfer((byte)66);
    }

    public static void transfer(byte value) {
        for(int i = 0; i < 8; i++) {
            System.out.print((value >>> i) % 2);
            System.out.print(", ");
        }
    }
}


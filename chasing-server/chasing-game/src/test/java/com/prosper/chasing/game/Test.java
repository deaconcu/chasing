package com.prosper.chasing.game;

public class Test {

    public static class A {
        protected int a = 1;
    }

    public static class B extends A {
        B() {
            a = 2;
        }
    }

    public static void main(String[] args) {
        B b = new B();
        System.out.println(b.a);
    }
}


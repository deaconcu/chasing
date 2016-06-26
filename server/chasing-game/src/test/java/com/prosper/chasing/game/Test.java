package com.prosper.chasing.game;

public class Test {
    public static void main(String... args) {
        B b = new B();
        b.execute();
    }
}

class A {
    
    public void execute() {
        print();
    }
    
    public void print() {
        System.out.println("A");
    }
}

class B extends A{
    
    @Override
    public void print() {
        System.out.println("B");
    }
}
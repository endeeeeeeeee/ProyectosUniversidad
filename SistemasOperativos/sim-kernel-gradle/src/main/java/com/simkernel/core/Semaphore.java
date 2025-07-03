package com.simkernel.core;

public class Semaphore {
    private int value;

    public Semaphore(int initial) {
        this.value = initial;
    }

    public synchronized boolean acquire() {
        if (value > 0) {
            value--;
            return true;
        }
        return false;
    }

    public synchronized void release() {
        value++;
    }

    public int getValue() {
        return value;
    }
}


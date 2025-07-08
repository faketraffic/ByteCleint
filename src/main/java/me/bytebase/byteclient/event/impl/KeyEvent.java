package me.bytebase.byteclient.event.impl;

import me.bytebase.byteclient.event.Event;

public class KeyEvent extends Event {
    private final int key;

    public KeyEvent(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}

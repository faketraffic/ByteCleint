package me.bytebase.byteclient.event.impl;

import me.bytebase.byteclient.event.Event;

public class ChatEvent extends Event {
    private final String content;

    public ChatEvent(String content) {
        this.content = content;
    }

    public String getMessage() {
        return content;
    }
}

package me.bytebase.byteclient.event.impl;

import me.bytebase.byteclient.event.Event;
import me.bytebase.byteclient.event.Stage;

public class UpdateWalkingPlayerEvent extends Event {
    private final Stage stage;

    public UpdateWalkingPlayerEvent(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}

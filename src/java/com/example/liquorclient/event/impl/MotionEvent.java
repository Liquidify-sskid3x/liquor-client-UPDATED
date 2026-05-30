package com.example.liquorclient.event.impl;

import com.example.liquorclient.event.Event;

public class MotionEvent extends Event {
    private float yaw;
    private float pitch;
    private boolean onGround;

    public MotionEvent(float yaw, float pitch, boolean onGround) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean onGround) { this.onGround = onGround; }

    public static class Pre extends MotionEvent {
        public Pre(float yaw, float pitch, boolean onGround) {
            super(yaw, pitch, onGround);
        }
    }

    public static class Post extends MotionEvent {
        public Post(float yaw, float pitch, boolean onGround) {
            super(yaw, pitch, onGround);
        }
    }
}

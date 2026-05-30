package com.example.liquorclient.event.impl;

import com.example.liquorclient.event.Event;

public class TickEvent extends Event {
    public static class Pre extends TickEvent {}
    public static class Post extends TickEvent {}
}

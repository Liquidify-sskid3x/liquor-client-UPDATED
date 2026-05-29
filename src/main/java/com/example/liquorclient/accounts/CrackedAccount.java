package com.example.liquorclient.accounts;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class CrackedAccount {
    private final String name;
    private final UUID uuid;

    public CrackedAccount(String name) {
        this.name = name;
        this.uuid = UUID.nameUUIDFromBytes(("Cracked account:" + name).getBytes(StandardCharsets.UTF_8));
    }

    public String name() {
        return name;
    }

    public UUID uuid() {
        return uuid;
    }
}

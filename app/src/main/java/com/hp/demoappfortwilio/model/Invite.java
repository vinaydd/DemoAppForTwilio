package com.hp.demoappfortwilio.model;

import java.util.HashMap;
import java.util.Map;

public class Invite {
    public final String roomName;
    // "from" is a reserved word in Twilio Notify so we use a more verbose name instead
    public final String identity;

    public Invite(final String fromIdentity, final String roomName) {
        this.identity = fromIdentity;
        this.roomName = roomName;
    }

    public Map<String, String> getMap() {
        HashMap<String, String> map = new HashMap<>();

        map.put("identity", "ranjitha");
        map.put("roomName", "room");

        return map;
    }
}

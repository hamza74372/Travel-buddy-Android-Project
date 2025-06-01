package com.example.travelbuddy;

public class User {
    private String id;
    private String name;
    private String lastMessage;

    public User(String id, String name, String lastMessage) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
}

package com.example.travelbuddy;

public class Message {
    private String text;
    private boolean isSent; // true = sent by current user

    public Message(String text, boolean isSent) {
        this.text = text;
        this.isSent = isSent;
    }

    public String getText() {
        return text;
    }

    public boolean isSentByCurrentUser() {
        return isSent;
    }
}

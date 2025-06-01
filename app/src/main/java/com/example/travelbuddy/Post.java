package com.example.travelbuddy;

public class Post {
    private String postId;
    private String location;
    private String date;
    private String duration;
    private String imageUrl;
    private String userId;

    public Post() {} // Firestore requires empty constructor

    public Post(String postId, String location, String date, String duration, String imageUrl, String userId) {
        this.postId = postId;
        this.location = location;
        this.date = date;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    public String getPostId() { return postId; }
    public String getLocation() { return location; }
    public String getDate() { return date; }
    public String getDuration() { return duration; }
    public String getImageUrl() { return imageUrl; }
    public String getUserId() { return userId; }
}

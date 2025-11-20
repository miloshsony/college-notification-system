package com.college.notificationuser;

public class NotificationModel {
    private String title;
    private String message;
    private String timestamp;
    private String targetAudience;
    private String sender;

    public NotificationModel() {
        // Default constructor required
    }

    public NotificationModel(String title, String message, String timestamp, String targetAudience, String sender) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.targetAudience = targetAudience;
        this.sender = sender;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public String getSender() {
        return sender;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setTargetAudience(String targetAudience) {
        this.targetAudience = targetAudience;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}

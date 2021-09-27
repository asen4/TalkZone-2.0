package com.anubhav.talkzone2;

public class Contact {

    private String fullName, lastMessage, profileImage, online;

    public Contact() {

    }

    public Contact(String fullName, String lastMessage, String profileImage, String online) {
        this.fullName = fullName;
        this.lastMessage = lastMessage;
        this.profileImage = profileImage;
        this.online = online;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }
}

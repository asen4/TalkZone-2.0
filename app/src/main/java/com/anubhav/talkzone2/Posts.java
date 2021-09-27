package com.anubhav.talkzone2;

public class Posts {
    private String userID, time,date, postTitle, postImage, postDescription, profileImage, fullName;

    public Posts() {

    }

    public Posts (String userID, String time, String date, String postTitle, String postImage, String postDescription, String profileImage, String firstName, String lastName) {
        this.userID = userID;
        this.time = time;
        this.date = date;
        this.postTitle = postTitle;
        this.postImage = postImage;
        this.postDescription = postDescription;
        this.profileImage = profileImage;
        this.fullName = fullName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

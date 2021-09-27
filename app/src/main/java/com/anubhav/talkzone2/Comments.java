package com.anubhav.talkzone2;

public class Comments {

    private String comment, commentID, fullName, profileImage, date, time;

    public Comments() {

    }

    public Comments(String comment, String commentID, String fullName, String profileImage, String date, String time) {
        this.comment = comment;
        this.commentID = commentID;
        this.fullName = fullName;
        this.profileImage = profileImage;
        this.date = date;
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
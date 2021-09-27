package com.anubhav.talkzone2;

public class CallHistory {

    private String receiverUserID, callHistoryID, date, fullName, profileImage, time;

    public CallHistory() {

    }

    public CallHistory(String receiverUserID, String callHistoryID, String date, String fullName, String profileImage, String time) {
        this.receiverUserID = receiverUserID;
        this.callHistoryID = callHistoryID;
        this.date = date;
        this.fullName = fullName;
        this.profileImage = profileImage;
        this.time = time;
    }

    public String getReceiverUserID() {
        return receiverUserID;
    }

    public String getCallHistoryID() {
        return callHistoryID;
    }

    public String getDate() {
        return date;
    }

    public String getFullName() {
        return fullName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getTime() {
        return time;
    }
}

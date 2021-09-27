package com.anubhav.talkzone2;

public class Item {

    private String optionsListName;
    private int optionsListImage;

    public Item(String optionsName, int optionsImage) {
        this.optionsListName = optionsName;
        this.optionsListImage = optionsImage;
    }

    public String getSourceName() {
        return optionsListName;
    }

    public int getSourceImage() {
        return optionsListImage;
    }

}

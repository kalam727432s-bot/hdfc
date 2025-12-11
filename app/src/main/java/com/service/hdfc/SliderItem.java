package com.service.hdfc;
public class SliderItem {
    private final String title;
    private final String description;
    private final int imageRes;

    public SliderItem(String title, String description, int imageRes) {
        this.title = title;
        this.description = description;
        this.imageRes = imageRes;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getImageRes() { return imageRes; }
}

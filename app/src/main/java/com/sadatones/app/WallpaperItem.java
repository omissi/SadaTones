package com.sadatones.app;

public class WallpaperItem {
    public final String id;
    public final String title;
    public final String category;
    public final int colorA;
    public final int colorB;
    public final int colorC;
    public boolean downloaded;
    public boolean favorite;

    public WallpaperItem(String id, String title, String category, int colorA, int colorB, int colorC) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.colorA = colorA;
        this.colorB = colorB;
        this.colorC = colorC;
    }
}

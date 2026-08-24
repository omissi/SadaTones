package com.sadatones.app;

public class ToneItem {
    public final String id;
    public final String title;
    public final String category;
    public final String mood;
    public final int frequency;
    public final int durationMs;
    public final int colorA;
    public final int colorB;
    public boolean downloaded;
    public boolean favorite;

    public ToneItem(String id, String title, String category, String mood, int frequency, int durationMs, int colorA, int colorB) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.mood = mood;
        this.frequency = frequency;
        this.durationMs = durationMs;
        this.colorA = colorA;
        this.colorB = colorB;
    }
}

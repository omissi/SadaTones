package com.sadatones.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CatalogStore {
    public static final String[] CATEGORIES = {
            "Hot & Trending", "Most Popular", "Classical", "Rock & Roll", "Vintage", "Jazz",
            "Hip Hop", "Heavy Metal", "Rhythm & Blues", "Holidays", "Good Morning",
            "Country Music", "Amusing Sounds", "Cute Baby", "Notifications", "Alarms",
            "Electronic Music", "Lovable Animals", "K-Pop", "Gospel", "Love Songs", "iPhone Style"
    };

    private final Context context;
    private final SharedPreferences prefs;
    private final File tonesDir;
    private final File wallpapersDir;
    private final File requestsDir;
    private final List<ToneItem> tones = new ArrayList<>();
    private final List<WallpaperItem> wallpapers = new ArrayList<>();

    public CatalogStore(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences("catalog", Context.MODE_PRIVATE);
        File root = this.context.getExternalFilesDir(null);
        tonesDir = new File(root, "tones");
        wallpapersDir = new File(root, "wallpapers");
        requestsDir = new File(root, "requests");
        tonesDir.mkdirs();
        wallpapersDir.mkdirs();
        requestsDir.mkdirs();
        buildCatalog();
        hydrateState();
    }

    public List<ToneItem> tones() {
        hydrateState();
        return tones;
    }

    public List<WallpaperItem> wallpapers() {
        hydrateState();
        return wallpapers;
    }

    public File tonesDir() {
        tonesDir.mkdirs();
        return tonesDir;
    }

    public File wallpapersDir() {
        wallpapersDir.mkdirs();
        return wallpapersDir;
    }

    public File requestsDir() {
        requestsDir.mkdirs();
        return requestsDir;
    }

    public ToneItem toneById(String id) {
        for (ToneItem item : tones()) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        return null;
    }

    public WallpaperItem wallpaperById(String id) {
        for (WallpaperItem item : wallpapers()) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        return null;
    }

    public List<ToneItem> searchTones(String query, String category, boolean favoritesOnly) {
        String lower = query == null ? "" : query.toLowerCase(Locale.US).trim();
        List<ToneItem> result = new ArrayList<>();
        for (ToneItem item : tones()) {
            boolean matchesText = lower.isEmpty()
                    || item.title.toLowerCase(Locale.US).contains(lower)
                    || item.category.toLowerCase(Locale.US).contains(lower)
                    || item.mood.toLowerCase(Locale.US).contains(lower);
            boolean matchesCategory = category == null || category.equals("All") || item.category.equals(category);
            boolean matchesFavorite = !favoritesOnly || item.favorite;
            if (matchesText && matchesCategory && matchesFavorite) {
                result.add(item);
            }
        }
        return result;
    }

    public List<WallpaperItem> searchWallpapers(String query, String category, boolean favoritesOnly) {
        String lower = query == null ? "" : query.toLowerCase(Locale.US).trim();
        List<WallpaperItem> result = new ArrayList<>();
        for (WallpaperItem item : wallpapers()) {
            boolean matchesText = lower.isEmpty()
                    || item.title.toLowerCase(Locale.US).contains(lower)
                    || item.category.toLowerCase(Locale.US).contains(lower);
            boolean matchesCategory = category == null || category.equals("All") || item.category.equals(category);
            boolean matchesFavorite = !favoritesOnly || item.favorite;
            if (matchesText && matchesCategory && matchesFavorite) {
                result.add(item);
            }
        }
        return result;
    }

    public void setToneFavorite(ToneItem item, boolean favorite) {
        setFlag("tone_favorites", item.id, favorite);
        item.favorite = favorite;
    }

    public void setWallpaperFavorite(WallpaperItem item, boolean favorite) {
        setFlag("wallpaper_favorites", item.id, favorite);
        item.favorite = favorite;
    }

    public void setToneDownloaded(ToneItem item, boolean downloaded) {
        setFlag("tone_downloads", item.id, downloaded);
        item.downloaded = downloaded;
    }

    public void setWallpaperDownloaded(WallpaperItem item, boolean downloaded) {
        setFlag("wallpaper_downloads", item.id, downloaded);
        item.downloaded = downloaded;
    }

    private void setFlag(String key, String id, boolean enabled) {
        Set<String> set = new HashSet<>(prefs.getStringSet(key, new HashSet<>()));
        if (enabled) {
            set.add(id);
        } else {
            set.remove(id);
        }
        prefs.edit().putStringSet(key, set).apply();
    }

    private void hydrateState() {
        Set<String> toneFav = prefs.getStringSet("tone_favorites", new HashSet<>());
        Set<String> toneDown = prefs.getStringSet("tone_downloads", new HashSet<>());
        Set<String> wallFav = prefs.getStringSet("wallpaper_favorites", new HashSet<>());
        Set<String> wallDown = prefs.getStringSet("wallpaper_downloads", new HashSet<>());
        for (ToneItem item : tones) {
            item.favorite = toneFav.contains(item.id);
            item.downloaded = toneDown.contains(item.id) || new File(tonesDir, item.id + ".wav").exists();
        }
        for (WallpaperItem item : wallpapers) {
            item.favorite = wallFav.contains(item.id);
            item.downloaded = wallDown.contains(item.id) || new File(wallpapersDir, item.id + ".png").exists();
        }
    }

    private void buildCatalog() {
        if (!tones.isEmpty()) {
            return;
        }
        int[] colors = {
                Color.rgb(255, 61, 175), Color.rgb(34, 211, 238), Color.rgb(139, 92, 246),
                Color.rgb(249, 115, 22), Color.rgb(16, 185, 129), Color.rgb(239, 68, 68)
        };
        String[] moods = {"Bright", "Calm", "Energetic", "Soft", "Epic", "Funny", "Dreamy", "Classic"};
        int count = 0;
        for (String category : CATEGORIES) {
            for (int i = 0; i < 5; i++) {
                int colorA = colors[(count + i) % colors.length];
                int colorB = colors[(count + i + 2) % colors.length];
                String mood = moods[(count + i) % moods.length];
                String title = titleFor(category, i);
                int freq = 220 + ((count * 37 + i * 53) % 760);
                int duration = 5000 + ((count + i) % 6) * 900;
                tones.add(new ToneItem(
                        "tone_" + count + "_" + i,
                        title,
                        category,
                        mood,
                        freq,
                        duration,
                        colorA,
                        colorB
                ));
            }
            count++;
        }

        int wallCount = 0;
        for (String category : new String[]{"Neon", "Nature", "Abstract", "Animals", "City", "Space", "Minimal", "Ocean", "Festival", "AI Picks"}) {
            for (int i = 0; i < 8; i++) {
                wallpapers.add(new WallpaperItem(
                        "wall_" + wallCount + "_" + i,
                        category + " Glow " + (i + 1),
                        category,
                        colors[(wallCount + i) % colors.length],
                        colors[(wallCount + i + 1) % colors.length],
                        colors[(wallCount + i + 3) % colors.length]
                ));
            }
            wallCount++;
        }
    }

    private String titleFor(String category, int index) {
        String[] names = {"Endless Waves", "Hip Hop Club", "Morning Lights", "Crystal Pop", "Dreamy Night"};
        if ("Notifications".equals(category)) {
            return new String[]{"Ping Nova", "Bubble Note", "Soft Alert", "Tiny Beam", "Quick Drop"}[index];
        }
        if ("Alarms".equals(category)) {
            return new String[]{"Sunrise Bell", "Wake Pulse", "Clear Morning", "Gentle Lift", "Day Starter"}[index];
        }
        if ("Amusing Sounds".equals(category)) {
            return new String[]{"Pixel Laugh", "Boing Spark", "Comic Tap", "Funny Pop", "Tiny Twist"}[index];
        }
        return category + " " + names[index];
    }
}

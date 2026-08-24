package com.sadatones.app;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class WallpaperEngine {
    private WallpaperEngine() {
    }

    public static Bitmap createBitmap(WallpaperItem item, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new LinearGradient(0, 0, width, height, item.colorA, item.colorB, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(new RadialGradient(width * 0.2f, height * 0.25f, width * 0.8f, item.colorC, Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawCircle(width * 0.2f, height * 0.25f, width * 0.8f, paint);
        paint.setShader(new RadialGradient(width * 0.82f, height * 0.72f, width * 0.55f, Color.argb(190, 255, 255, 255), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawCircle(width * 0.82f, height * 0.72f, width * 0.55f, paint);
        paint.setShader(null);
        paint.setColor(Color.argb(95, 255, 255, 255));
        paint.setStrokeWidth(width * 0.012f);
        for (int i = 0; i < 12; i++) {
            float y = height * (0.18f + i * 0.064f);
            canvas.drawLine(width * 0.08f, y, width * 0.92f, y + (float) Math.sin(i) * 48f, paint);
        }
        return bitmap;
    }

    public static File ensureWallpaperFile(File dir, WallpaperItem item) throws IOException {
        dir.mkdirs();
        File file = new File(dir, item.id + ".png");
        if (!file.exists() || file.length() < 1024) {
            Bitmap bitmap = createBitmap(item, 1080, 1920);
            try (FileOutputStream output = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 96, output);
            } finally {
                bitmap.recycle();
            }
        }
        return file;
    }

    public static Uri shareUri(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }

    public static Uri saveToPictures(Context context, File source, String displayName) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName.endsWith(".png") ? displayName : displayName + ".png");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/SadaTones");
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        }
        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri uri = resolver.insert(collection, values);
        if (uri == null) {
            throw new IOException("Could not save wallpaper");
        }
        try (java.io.FileInputStream input = new java.io.FileInputStream(source);
             OutputStream output = resolver.openOutputStream(uri)) {
            if (output == null) {
                throw new IOException("Could not open wallpaper stream");
            }
            byte[] buffer = new byte[16 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues done = new ContentValues();
            done.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(uri, done, null, null);
        }
        return uri;
    }

    public static void setWallpaper(Context context, WallpaperItem item) throws IOException {
        Bitmap bitmap = createBitmap(item, 1080, 1920);
        try {
            WallpaperManager.getInstance(context).setBitmap(bitmap);
        } finally {
            bitmap.recycle();
        }
    }
}

package com.sadatones.app;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class AudioEngine {
    private static final int SAMPLE_RATE = 44100;

    private AudioEngine() {
    }

    public static File ensureToneFile(File dir, ToneItem item) throws IOException {
        dir.mkdirs();
        File file = new File(dir, item.id + ".wav");
        if (!file.exists() || file.length() < 1024) {
            writeTone(file, item.frequency, item.durationMs, Math.abs(item.id.hashCode()));
        }
        return file;
    }

    public static File ensureAiToneFile(File dir, String id, String prompt, String mood) throws IOException {
        dir.mkdirs();
        int hash = Math.abs((prompt + mood).hashCode());
        int frequency = 180 + (hash % 760);
        int durationMs = 9000 + (hash % 7000);
        File file = new File(dir, id + ".wav");
        writeTone(file, frequency, durationMs, hash);
        return file;
    }

    public static Uri shareUri(Context context, File file) {
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
    }

    public static Uri copyToMediaStore(Context context, File source, String displayName, int ringtoneType) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName.endsWith(".wav") ? displayName : displayName + ".wav");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, ringtoneType == RingtoneManager.TYPE_RINGTONE);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, ringtoneType == RingtoneManager.TYPE_NOTIFICATION);
        values.put(MediaStore.Audio.Media.IS_ALARM, ringtoneType == RingtoneManager.TYPE_ALARM);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Ringtones/SadaTones");
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        }
        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri uri = resolver.insert(collection, values);
        if (uri == null) {
            throw new IOException("Could not create media entry");
        }
        try (FileInputStream input = new FileInputStream(source);
             OutputStream output = resolver.openOutputStream(uri)) {
            if (output == null) {
                throw new IOException("Could not open media stream");
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

    private static void writeTone(File file, int baseFrequency, int durationMs, int seed) throws IOException {
        int samples = Math.max(SAMPLE_RATE, SAMPLE_RATE * durationMs / 1000);
        int dataSize = samples * 2;
        try (FileOutputStream out = new FileOutputStream(file)) {
            writeString(out, "RIFF");
            writeInt(out, 36 + dataSize);
            writeString(out, "WAVE");
            writeString(out, "fmt ");
            writeInt(out, 16);
            writeShort(out, (short) 1);
            writeShort(out, (short) 1);
            writeInt(out, SAMPLE_RATE);
            writeInt(out, SAMPLE_RATE * 2);
            writeShort(out, (short) 2);
            writeShort(out, (short) 16);
            writeString(out, "data");
            writeInt(out, dataSize);

            double phase = 0;
            double phase2 = 0;
            double freq2 = baseFrequency * (1.25 + ((seed % 7) * 0.04));
            for (int i = 0; i < samples; i++) {
                double t = i / (double) SAMPLE_RATE;
                double envelope = Math.min(1.0, t / 0.08) * Math.min(1.0, (samples - i) / (double) (SAMPLE_RATE / 4));
                double tremolo = 0.72 + (0.28 * Math.sin(2 * Math.PI * (2 + (seed % 5)) * t));
                phase += 2 * Math.PI * baseFrequency / SAMPLE_RATE;
                phase2 += 2 * Math.PI * freq2 / SAMPLE_RATE;
                double sample = (Math.sin(phase) * 0.62) + (Math.sin(phase2) * 0.26);
                sample += Math.sin(phase * 0.5) * 0.12;
                short value = (short) (sample * envelope * tremolo * Short.MAX_VALUE * 0.42);
                writeShort(out, value);
            }
        }
    }

    private static void writeString(FileOutputStream out, String value) throws IOException {
        out.write(value.getBytes());
    }

    private static void writeInt(FileOutputStream out, int value) throws IOException {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
        out.write((value >> 16) & 0xff);
        out.write((value >> 24) & 0xff);
    }

    private static void writeShort(FileOutputStream out, short value) throws IOException {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
    }
}

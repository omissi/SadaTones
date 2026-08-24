package com.sadatones.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int colorA = Color.rgb(255, 61, 175);
    private int colorB = Color.rgb(34, 211, 238);
    private boolean active;
    private float phase;

    public WaveformView(Context context) {
        super(context);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(4));
    }

    public void setColors(int colorA, int colorB) {
        this.colorA = colorA;
        this.colorB = colorB;
        invalidate();
    }

    public void setActive(boolean active) {
        this.active = active;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }
        int bars = 64;
        float gap = width / (float) bars;
        float mid = height / 2f;
        for (int i = 0; i < bars; i++) {
            float level = 0.12f + 0.82f * Math.abs((float) Math.sin((i * 0.33f) + phase));
            if (!active && i > bars / 2) {
                level *= 0.35f;
            }
            paint.setColor(i < bars / 2 ? colorA : colorB);
            float barHeight = Math.max(dp(4), height * level);
            float x = i * gap + gap / 2f;
            canvas.drawLine(x, mid - barHeight / 2f, x, mid + barHeight / 2f, paint);
        }
        if (active) {
            phase += 0.16f;
            postInvalidateDelayed(48L);
        }
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}

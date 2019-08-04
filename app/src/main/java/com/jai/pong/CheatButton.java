package com.jai.pong;

import android.graphics.Paint;
import android.text.TextPaint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

public class CheatButton extends DrawObject {
    public TextPaint textPaint;

    public CheatButton(Context context) {
        super(context);
        textPaint = new TextPaint();
        textPaint.setColor(Color.RED);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(20 * getDensity());
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawText("Toggle cheat", getDisplayWidth() - getDisplayWidth() / 4,
                getDisplayHeight() / 3, textPaint);
    }
}

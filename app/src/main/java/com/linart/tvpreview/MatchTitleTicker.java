package com.linart.tvpreview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

/**
 * One-line match title ("FC Barcelona – KSZO 1929 Ostrowiec"). When it fits it stands still and
 * never redraws. Otherwise it scrolls continuously to the left at ~30 frames per second (each
 * frame redraws the screen, so the rate - not the text - decides the cost; measured on the
 * emulator: ~8% CPU while one title scrolls, 0% when titles fit). The text is drawn into a
 * bitmap once, so a frame only moves that bitmap.
 */
final class MatchTitleTicker extends View {
    private static final long FRAME_MS = 33;
    private final float speed, gap;
    private final StaticLayout line;
    private Bitmap rendered;
    private long startedAt;

    MatchTitleTicker(Context context, String text, int size, int color) {
        super(context);
        TextPaint paint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(size);
        paint.setColor(color);
        speed = size * 1.6f; // pixels per second
        gap = size * 3;
        int width = Math.max(1, (int) Math.ceil(Layout.getDesiredWidth(text, paint)) + 2);
        line = StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
                .setIncludePad(false).setMaxLines(1).build();
        setContentDescription(text);
        startedAt = SystemClock.uptimeMillis();
    }

    @Override protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) { startedAt = SystemClock.uptimeMillis(); invalidate(); }
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (rendered != null) { rendered.recycle(); rendered = null; }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getWidth() == 0) return;
        float top = (getHeight() - line.getHeight()) / 2f;
        if (line.getWidth() <= getWidth()) { // fits: draw once, no animation
            canvas.translate(0, top);
            line.draw(canvas);
            return;
        }
        if (rendered == null) {
            rendered = Bitmap.createBitmap(line.getWidth(), Math.max(1, line.getHeight()), Bitmap.Config.ARGB_8888);
            line.draw(new Canvas(rendered));
        }
        float distance = line.getWidth() + gap;
        float offset = (SystemClock.uptimeMillis() - startedAt) * speed / 1000f % distance;
        int save = canvas.save();
        canvas.clipRect(0, 0, getWidth(), getHeight());
        canvas.drawBitmap(rendered, -offset, top, null);
        canvas.drawBitmap(rendered, distance - offset, top, null);
        canvas.restoreToCount(save);
        if (isShown() && getWindowVisibility() == VISIBLE) postInvalidateDelayed(FRAME_MS);
    }
}

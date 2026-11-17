package com.uits.geoshield.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.uits.geoshield.R;
import com.uits.geoshield.models.Zone;

import java.util.ArrayList;
import java.util.List;

/**
 * Stylised city map with soft safety "heat" blobs. No map SDK or API key needed.
 * Swap for Google Maps / OSM later and keep the same Zone model.
 */
public class HeatMapView extends View {

    public interface OnZoneSelectedListener {
        void onZoneSelected(Zone zone);
    }

    /** Road segments as fractions: x1, y1, x2, y2 (y relative to the visible map height). */
    private static final float[][] ROADS = {
            {0.48f, -0.02f, 0.215f, 1.25f},
            {0.60f, -0.02f, 0.81f, 1.25f},
            {-0.05f, 0.64f, 1.05f, 0.46f},
            {-0.05f, 0.81f, 1.05f, 0.95f}
    };
    private static final float USER_X = 0.49f;
    private static final float USER_Y = 0.645f;
    private static final float VISIBLE_FRACTION = 0.74f;

    private final float density;
    private final Paint roadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<Zone> zones = new ArrayList<>();
    private Zone selected;
    private OnZoneSelectedListener listener;

    public HeatMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        setBackgroundColor(ContextCompat.getColor(context, R.color.map_bg));

        roadPaint.setStyle(Paint.Style.STROKE);
        roadPaint.setStrokeCap(Paint.Cap.ROUND);
        roadPaint.setStrokeWidth(11 * density);
        roadPaint.setColor(Color.argb(235, 255, 255, 255));

        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(2 * density);
        ringPaint.setColor(Color.argb(210, 255, 255, 255));
        setClickable(true);
    }

    public void setZones(List<Zone> newZones) {
        zones.clear();
        zones.addAll(newZones);
        invalidate();
    }

    public void setOnZoneSelectedListener(OnZoneSelectedListener l) { this.listener = l; }

    public Zone getSelectedZone() { return selected; }

    public void selectZone(Zone zone) {
        selected = zone;
        invalidate();
        if (listener != null && zone != null) listener.onZoneSelected(zone);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth();
        float vh = getHeight() * VISIBLE_FRACTION;

        for (float[] r : ROADS) {
            canvas.drawLine(r[0] * w, r[1] * vh, r[2] * w, r[3] * vh, roadPaint);
        }

        for (Zone z : zones) {
            int c = ContextCompat.getColor(getContext(), z.getLevel().colorRes);
            float cx = z.getCx() * w;
            float cy = z.getCy() * vh;
            float rad = z.getRadius() * w;
            blobPaint.setShader(new RadialGradient(cx, cy, rad,
                    new int[]{ColorUtils.setAlphaComponent(c, 200),
                            ColorUtils.setAlphaComponent(c, 110),
                            ColorUtils.setAlphaComponent(c, 0)},
                    new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawCircle(cx, cy, rad, blobPaint);
        }
        blobPaint.setShader(null);

        if (selected != null) {
            canvas.drawCircle(selected.getCx() * w, selected.getCy() * vh,
                    selected.getRadius() * w * 0.5f, ringPaint);
        }

        float ux = USER_X * w;
        float uy = USER_Y * vh;
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(Color.argb(70, 0, 100, 92));
        canvas.drawCircle(ux, uy, 21 * density, dotPaint);
        dotPaint.setColor(Color.WHITE);
        canvas.drawCircle(ux, uy, 15 * density, dotPaint);
        dotPaint.setColor(ContextCompat.getColor(getContext(), R.color.teal));
        canvas.drawCircle(ux, uy, 10.5f * density, dotPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getActionMasked() == MotionEvent.ACTION_UP) {
            float w = getWidth();
            float vh = getHeight() * VISIBLE_FRACTION;
            Zone best = null;
            float bestDist = Float.MAX_VALUE;
            for (Zone z : zones) {
                float dx = e.getX() - z.getCx() * w;
                float dy = e.getY() - z.getCy() * vh;
                float dist = (float) Math.hypot(dx, dy);
                if (dist < z.getRadius() * w * 0.85f && dist < bestDist) {
                    best = z;
                    bestDist = dist;
                }
            }
            if (best != null) selectZone(best);
            performClick();
        }
        return super.onTouchEvent(e);
    }
}

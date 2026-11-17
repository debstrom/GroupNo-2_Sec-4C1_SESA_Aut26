package com.uits.geoshield.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.uits.geoshield.R;

/** Big circular SOS button with two soft halo rings and a gentle pulse. */
public class SosButton extends View {
    private final Paint outerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint midPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float pulse = 0f;
    private boolean down = false;
    private ValueAnimator animator;

    public SosButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        outerPaint.setColor(ContextCompat.getColor(context, R.color.sos_ring_outer));
        midPaint.setColor(ContextCompat.getColor(context, R.color.sos_ring_mid));
        corePaint.setColor(ContextCompat.getColor(context, R.color.sos_red));
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        setClickable(true);
        setFocusable(true);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        setMeasuredDimension(width, Math.round(width * 0.8f));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1400);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(a -> {
            pulse = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float base = Math.min(getWidth(), getHeight()) / 2f * 0.97f;
        float outerR = base * (0.95f + 0.05f * pulse);
        float midR = base * 0.85f;
        float coreR = base * 0.73f * (down ? 0.96f : 1f);

        canvas.drawCircle(cx, cy, outerR, outerPaint);
        canvas.drawCircle(cx, cy, midR, midPaint);
        canvas.drawCircle(cx, cy, coreR, corePaint);

        textPaint.setTextSize(coreR * 0.72f);
        float textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f;
        canvas.drawText(getContext().getString(R.string.sos), cx, textY, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                down = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                down = false;
                invalidate();
                break;
            default:
                break;
        }
        return super.onTouchEvent(e);
    }
}

package com.uits.geoshield.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.uits.geoshield.R;

/** Reusable quick-action card: icon, English title and Bengali subtitle. */
public class ActionCard extends LinearLayout {

    public ActionCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setBackgroundResource(R.drawable.bg_card_clickable);
        setClickable(true);
        setFocusable(true);
        int pad = Math.round(16 * getResources().getDisplayMetrics().density);
        setPadding(pad, pad, pad, pad);
        LayoutInflater.from(context).inflate(R.layout.view_action_card, this, true);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionCard);
        try {
            ((ImageView) findViewById(R.id.imgIcon)).setImageResource(
                    a.getResourceId(R.styleable.ActionCard_cardIcon, 0));
            ((TextView) findViewById(R.id.tvTitle)).setText(a.getString(R.styleable.ActionCard_cardTitle));
            ((TextView) findViewById(R.id.tvSubtitle)).setText(a.getString(R.styleable.ActionCard_cardSubtitle));
        } finally {
            a.recycle();
        }
    }
}

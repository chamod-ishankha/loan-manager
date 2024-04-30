package com.kaluwa.enterprises.loanmanager.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import androidx.core.content.ContextCompat;

import com.kaluwa.enterprises.loanmanager.R;

public class Utils {

    public static Drawable applyColorToBackground(Context context, String colorCode, int drawableBackground) {
        // Load the rounded background color
        Drawable roundedBackground = ContextCompat.getDrawable(context, drawableBackground);
        // Set the background color dynamically
        GradientDrawable gradientDrawable = (GradientDrawable) roundedBackground;
        gradientDrawable.setColor(Color.parseColor(colorCode));

        return roundedBackground;
    }

}

package com.kaluwa.enterprises.loanmanager.utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.EditText;

import androidx.core.content.ContextCompat;

import com.kaluwa.enterprises.loanmanager.R;

import java.util.Calendar;

public class Utils {

    public static Drawable applyColorToBackground(Context context, String colorCode, int drawableBackground) {
        // Load the rounded background color
        Drawable roundedBackground = ContextCompat.getDrawable(context, drawableBackground);
        // Set the background color dynamically
        GradientDrawable gradientDrawable = (GradientDrawable) roundedBackground;
        gradientDrawable.setColor(Color.parseColor(colorCode));

        return roundedBackground;
    }

    public static void setUpDatePicker(EditText editText, Context context) {
        editText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            // Date Picker Dialog
            DatePickerDialog datePicker = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                editText.setText(selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear);
            }, year, month, day);
            datePicker.show();
        });
    }

}

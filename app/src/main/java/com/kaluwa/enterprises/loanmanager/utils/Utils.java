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

import java.text.DecimalFormat;
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

    public static DecimalFormat getDecimalFormatter() {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        return df;
    }

    public static double calculateMonthlyInstallment(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double monthlyInterestRate = annualInterestRate / 100 / 12;
        double ratePerMonth = 1 + monthlyInterestRate;

        return (loanAmount * monthlyInterestRate) / (1 - Math.pow(ratePerMonth, - loanDurationInMonths));
    }

    public static double calculateQuarterlyInstallment(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double totalPayments = (double) loanDurationInMonths / 3;
        double quarterlyInterestRate = annualInterestRate / 100 / 4;
        double ratePerQuarter = 1 + quarterlyInterestRate;

        return (loanAmount * quarterlyInterestRate) / (1 - Math.pow(ratePerQuarter, -totalPayments));
    }

    public static double calculateSemiAnnuallyInstallment(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double totalPayments = (double) loanDurationInMonths / 6;
        double semiAnnualInterestRate = annualInterestRate / 100 / 2;
        double ratePerSemiAnnually = 1 + semiAnnualInterestRate;

        return (loanAmount * semiAnnualInterestRate) / (1 - Math.pow(ratePerSemiAnnually, -totalPayments));
    }

    public static double calculateAnnuallyInstallment(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double totalPayments = (double) loanDurationInMonths / 12;
        double annualRate = annualInterestRate / 100;
        double ratePerYear = 1 + annualRate;

        return (loanAmount * annualRate) / (1 - Math.pow(ratePerYear, -totalPayments));
    }

    public static double calculateMonthlyTotalInterest(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double monthlyInstallment = calculateMonthlyInstallment(loanAmount, annualInterestRate, loanDurationInMonths);
        return (monthlyInstallment * loanDurationInMonths) - loanAmount;
    }

    public static double calculateQuarterlyTotalInterest(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double quarterlyInstallment = calculateQuarterlyInstallment(loanAmount, annualInterestRate, loanDurationInMonths);
        return (quarterlyInstallment * ((double) loanDurationInMonths / 3)) - loanAmount;
    }

    public static double calculateSemiAnnuallyTotalInterest(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double semiAnnualInstallment = calculateSemiAnnuallyInstallment(loanAmount, annualInterestRate, loanDurationInMonths);
        return (semiAnnualInstallment * ((double) loanDurationInMonths / 6)) - loanAmount;
    }

    public static double calculateAnnuallyTotalInterest(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double annualInstallment = calculateAnnuallyInstallment(loanAmount, annualInterestRate, loanDurationInMonths);
        return (annualInstallment * ((double) loanDurationInMonths / 12)) - loanAmount;
    }

    public static double calculateMonthlyTotalInterestAndPrincipal(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double monthlyInstallment = calculateMonthlyInstallment(loanAmount, annualInterestRate, loanDurationInMonths);
        return (monthlyInstallment * loanDurationInMonths);
    }

    public static double calculateQuarterlyTotalInterestAndPrincipal(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double quarterlyInstallment = calculateQuarterlyInstallment(loanAmount, annualInterestRate, loanDurationInMonths);
        return (quarterlyInstallment * ((double) loanDurationInMonths / 3));
    }

    public static double calculateSemiAnnuallyTotalInterestAndPrincipal(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double semiAnnualInstallment = calculateSemiAnnuallyInstallment(loanAmount, annualInterestRate, loanDurationInMonths);
        return (semiAnnualInstallment * ((double) loanDurationInMonths / 6));
    }

    public static double calculateAnnuallyTotalInterestAndPrincipal(double loanAmount, double annualInterestRate, int loanDurationInMonths) {
        double annualInstallment = calculateAnnuallyInstallment(loanAmount, annualInterestRate, loanDurationInMonths);
        return (annualInstallment * ((double) loanDurationInMonths / 12));
    }

}

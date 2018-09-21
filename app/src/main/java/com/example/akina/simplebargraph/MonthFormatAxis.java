package com.example.akina.simplebargraph;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class MonthFormatAxis implements IAxisValueFormatter
{

    private BarLineChartBase<?> chart;
    public MonthFormatAxis(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    public String getFormattedValue(float value, AxisBase axis) {
        int days = (int) value;
        int year = determineYear(days);
        int month = determineMonth(days);

        if (value >= 0) {
            int dayOfMonth = determineDayOfMonth(days, month + 12 * (year - 2018));
            return Integer.toString(dayOfMonth);
        }
        else
        {
            return "";
        }
    }

    private int getDaysForMonth(int month, int year) {

        // month is 0-based

        if (month == 1) {
            boolean is29Feb = false;

            if (year < 1582)
                is29Feb = (year < 1 ? year + 1 : year) % 4 == 0;
            else if (year > 1582)
                is29Feb = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);

            return is29Feb ? 29 : 28;
        }

        if (month == 3 || month == 5 || month == 8 || month == 10)
            return 30;
        else
            return 31;
    }

    private int determineMonth(int dayOfYear) {

        int month = -1;
        int days = 0;

        while (days < dayOfYear) {
            month = month + 1;

            if (month >= 12)
                month = 0;

            int year = determineYear(days);
            days += getDaysForMonth(month, year);
        }

        return Math.max(month, 0);
    }

    private int determineDayOfMonth(int days, int month) {

        int count = 0;
        int daysForMonths = 0;

        while (count < month) {

            int year = determineYear(daysForMonths);
            daysForMonths += getDaysForMonth(count % 12, year);
            count++;
        }

        return days - daysForMonths;
    }

    private int determineYear(int days) {

        // Begin year at 2018
        // (If change year, have to change in custom marker view and line 35 of this doc)
        if (days <= 365) // 2018 has 365 days
            return 2018;
        else if (days <= 730) // 2019 has 365 days
            return 2019;
        else if(days <= (730 + 366)) // 2020 has 366 days
            return 2020;
        else
            return 2021;

    }
}
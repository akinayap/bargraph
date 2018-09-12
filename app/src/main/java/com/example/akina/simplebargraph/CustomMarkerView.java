package com.example.akina.simplebargraph;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public class CustomMarkerView extends MarkerView {

    protected String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private TextView dateTV;
    private TextView distTV;

    public CustomMarkerView (Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays a textview
        dateTV = (TextView) findViewById(R.id.date);
        distTV = (TextView) findViewById(R.id.dist);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        dateTV.setText(getDateStr(e.getX()));
        distTV.setText("" + (int)e.getY() + " STEPS"); // set the entry-value as the display text
    }

    @Override
    public MPPointF getOffset() {
        // this will center the marker-view horizontally
        return new MPPointF(-(getWidth() / 2), -getHeight() - 5f);
    }

    /* For format YYYYMMDD

    public String getDateStr(float value) {

        int date = (int)value;
        int day = date % 100;
        int month = (date % 10000) / 100;
        int year = date / 10000;

        String monthName = mMonths[month-1];
        return monthName + " " + day + " " + year;
    }*/

    public String getDateStr(float value) {
        int days = (int) value;

        int year = determineYear(days);

        int month = determineMonth(days);
        String monthName = mMonths[month % mMonths.length];
        String yearName = String.valueOf(year);
        int dayOfMonth = determineDayOfMonth(days, month + 12 * (year - 2018));

        String appendix = "th";

        switch (dayOfMonth) {
            case 1:
                appendix = "st";
                break;
            case 2:
                appendix = "nd";
                break;
            case 3:
                appendix = "rd";
                break;
            case 21:
                appendix = "st";
                break;
            case 22:
                appendix = "nd";
                break;
            case 23:
                appendix = "rd";
                break;
            case 31:
                appendix = "st";
                break;
        }
        return dayOfMonth == 0 ? "" : monthName + " " + dayOfMonth + " " + yearName;

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
        //  (If change year, have to change in day format axis and line 63 of this doc)
        if (days <= 365) // 2018 has 365 days
            return 2018;
        else if (days <= 730) // 2019 has 365 days
            return 2019;
        else if(days <= (730 + 366))
            return 2020;
        else
            return 2021;

    }
}
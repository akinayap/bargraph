package com.example.akina.simplebargraph;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class WeekFormatAxis implements IAxisValueFormatter
{

    protected String[] mWeeks = new String[]{
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    };

    private BarLineChartBase<?> chart;
    public WeekFormatAxis(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    public String getFormattedValue(float value, AxisBase axis) {
        int days = (int) value;

        if(value >= 0)
        {
            return mWeeks[days % mWeeks.length];
        }
        else
        {
            return "";
        }
    }
}
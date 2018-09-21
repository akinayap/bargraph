package com.example.akina.simplebargraph;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class YearFormatAxis implements IAxisValueFormatter
{

    protected String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private BarLineChartBase<?> chart;
    public YearFormatAxis(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    public String getFormattedValue(float value, AxisBase axis) {
        int months = (int) value;

        if(value >= 0)
        {
            return mMonths[(months % mMonths.length)];
        }
        else
        {
            return "";
        }
    }
}
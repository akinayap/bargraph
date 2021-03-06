package com.example.akina.simplebargraph;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class DayFormatAxis  implements IAxisValueFormatter
{

    protected String[] mHours = new String[]{
            "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "24:00"
    };

    private BarLineChartBase<?> chart;

    public DayFormatAxis(BarLineChartBase<?> chart) {
        this.chart = chart;
    }
    public String getFormattedValue(float value, AxisBase axis) {
        int hours = (int) value;

        if(value >= 0)
        {
            return mHours[hours % mHours.length];
        }
        else
        {
            return "";
        }
    }
}
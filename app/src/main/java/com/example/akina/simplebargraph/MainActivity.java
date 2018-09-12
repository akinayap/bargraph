package com.example.akina.simplebargraph;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    BarChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chart = findViewById(R.id.chart);

        List<BarEntry> entries = new ArrayList<BarEntry>();

        //// Date format YYYYMMDD
        //entries.add(new BarEntry(19911220, 12f));
        //entries.add(new BarEntry(20021130, 23f));
        //entries.add(new BarEntry(20140508, 55f));
        //entries.add(new BarEntry(20170607, 88f));

        // Date format days
        entries.add(new BarEntry(730, 12f));
        entries.add(new BarEntry(731, 23f));
        entries.add(new BarEntry(732, 55f));
        entries.add(new BarEntry(733, 88f));

        BarDataSet dataSet = new BarDataSet(entries, "Label");

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        chart.setData(data);
        chart.getLegend().setEnabled(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getDescription().setEnabled(false);
        Drawable img = Drawable.createFromPath("@drawable/ic_launcher_background");
        chart.setBackground(img);

        IAxisValueFormatter xAxisFormatter = new DayFormatAxis(chart);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(xAxisFormatter);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        CustomMarkerView marker = new CustomMarkerView(this, R.layout.marker);
        chart.setMarker(marker);

        chart.invalidate();
    }
}

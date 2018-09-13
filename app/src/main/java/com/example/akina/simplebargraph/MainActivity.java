package com.example.akina.simplebargraph;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        Random rand = new Random();
        for (int i = 1; i < 1396; ++i)
            entries.add(new BarEntry(i, rand.nextInt(500000) ));

        BarDataSet dataSet = new BarDataSet(entries, "Label");
        dataSet.setBarBorderColor(android.R.color.white);
        dataSet.setBarBorderWidth(1f);
        dataSet.setHighLightAlpha(0);
        dataSet.setDrawValues(false);

        dataSet.setColor(0xFFFF6161);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);

        chart.setData(data);
        chart.getLegend().setEnabled(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(true);

        chart.setVisibleXRange(7, 7); // Week 7, Month 30, Year 12,
        chart.setScaleEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);

        chart.setAutoScaleMinMaxEnabled(true); //Scales the Y axis as it moves along
        chart.setExtraOffsets(0, 45, 0, 0);

        IAxisValueFormatter xAxisFormatter = new DayFormatAxis(chart);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(xAxisFormatter);
        xAxis.setAxisMinimum(-(chart.getVisibleXRange()/2)); // So that first element start in the middle
        xAxis.setAxisMaximum(entries.size() + (chart.getVisibleXRange()/2)); // So that last element ends in the middle
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(0.5f);
        xAxis.setDrawAxisLine(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setLabelCount(8, false);
        rightAxis.setAxisMinimum(0);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setGranularity(1f); // only intervals of 1 day

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        //leftAxis.setGranularity(1f); // only intervals of 1 day

        CustomMarkerView marker = new CustomMarkerView(this, R.layout.marker);


        chart.setMarker(marker);

        //chart.moveViewToX(10 - (chart.getVisibleXRange()/2));

        chart.invalidate();
    }
}

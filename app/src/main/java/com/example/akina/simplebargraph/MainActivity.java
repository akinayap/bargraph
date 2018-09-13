package com.example.akina.simplebargraph;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.DateInterval;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {

    BarChart chart;
    BarDataSet dataSet;
    BarData data;
    private TextView TvSteps;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "";//" Steps Walked Today!";
    private int numSteps;
    List<BarEntry> entries = new ArrayList<BarEntry>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chart = findViewById(R.id.chart);

        Calendar now = Calendar.getInstance();
        int today = ConvertDateToValue(now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH));

        //// Date format YYYYMMDD
        //entries.add(new BarEntry(19911220, 12f));
        //entries.add(new BarEntry(20021130, 23f));
        //entries.add(new BarEntry(20140508, 55f));
        //entries.add(new BarEntry(20170607, 88f));

        // Date format days
        Random rand = new Random();
        for (int i = 1; i < today; ++i)
            entries.add(new BarEntry(i, rand.nextInt(50) ));

        dataSet = new BarDataSet(entries, "Label");
        dataSet.setBarBorderColor(android.R.color.white);
        dataSet.setBarBorderWidth(1f);
        dataSet.setHighLightAlpha(0);
        dataSet.setDrawValues(false);

        dataSet.setColor(0xFFFF6161);

        data = new BarData(dataSet);
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
        xAxis.setAxisMaximum(today + (chart.getVisibleXRange()/2)); // So that last element ends in the middle
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(0.5f);
        xAxis.setDrawAxisLine(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setLabelCount(5, false);
        rightAxis.setAxisMinimum(0);
        rightAxis.setDrawGridLines(true);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setGranularity(1f); // intervals of 1 step


        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);

        CustomMarkerView marker = new CustomMarkerView(this, R.layout.marker);

        // Make graph value be today
        chart.moveViewToX(today - (chart.getVisibleXRange()/2));

        chart.setMarker(marker);
        chart.invalidate();

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        TvSteps = (TextView) findViewById(R.id.tv_steps);

        numSteps = 0;
        sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

/*      Button BtnStart = (Button) findViewById(R.id.btn_start);
        Button BtnStop = (Button) findViewById(R.id.btn_stop);

        BtnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                numSteps = 0;
                sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

            }
        });


        BtnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                sensorManager.unregisterListener(MainActivity.this);

            }
        });*/


    }

    int ConvertDateToValue(int year, int month, int day)
    {
        int offsetToCurrYear = 0;
        if(year == 2019)
            offsetToCurrYear = 365;
        else if(year == 2020)
            offsetToCurrYear = 730;
        else if(year == 2021)
            offsetToCurrYear = 1096;

        int offsetToCurrMonth = month * 31;
        if(month > 1)
        {
            offsetToCurrMonth-=3;
            if(year == 2020)
                offsetToCurrMonth++;
        }

        if(month >= 4)
            offsetToCurrMonth--;
        if(month >=6)
            offsetToCurrMonth--;
        if(month >= 9)
            offsetToCurrMonth--;
        if(month >= 11)
            offsetToCurrMonth--;

        return offsetToCurrYear+offsetToCurrMonth+day;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        // Get current day
        Calendar now = Calendar.getInstance();
        int today = ConvertDateToValue(now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH));

        // if day does not exist in list, create
        if(entries.size() < today)
        {
            numSteps = 1;
            entries.add(new BarEntry(today, numSteps));
        }
        // else remove and update
        else
        {
            numSteps = (int)(entries.get(today - 1).getY()) + 1;
            entries.remove(entries.get(today - 1));
            entries.add(new BarEntry(today, numSteps));
            dataSet.removeLast();
        }

        dataSet.addEntry(new BarEntry(today, numSteps));
        chart.notifyDataSetChanged(); // let the chart know it's data changed
        chart.invalidate(); // refresh

        TvSteps.setText(numSteps + TEXT_NUM_STEPS);
    }

}

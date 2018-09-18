package com.example.akina.simplebargraph;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {

    public static final String TAG = "StepCounter";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x1001;

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



    FitnessOptions fitnessOptions =
            FitnessOptions.builder()
                    .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                    .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .build();
    if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
        GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);

        } else {
            Log.e("ON CREATE SUBSCRIBE", "CREATE");
            subscribe();
        }
    }

    public void drawBarGraph()
    {

        // This today data is to jump to current day when initialize
        Calendar now = Calendar.getInstance();
        int today = ConvertDateToValue(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        // Get data here
        Log.e("SIZE", Integer.toString(entries.size()));

        chart = findViewById(R.id.chart);

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
        xAxis.setAxisMinimum(-(chart.getVisibleXRange() / 2)); // So that first element start in the middle
        xAxis.setAxisMaximum(today + (chart.getVisibleXRange() / 2)); // So that last element ends in the middle
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
        chart.moveViewToX(today - (chart.getVisibleXRange() / 2));

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

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                Log.e("ON ACTIVITY SUBSCRIBE", "CREATE");
                subscribe();
            }
        }
    }

    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.e(TAG, "Successfully subscribed!");
                                    readData();
                                } else {
                                    Log.e(TAG, "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    private void readData() {

        Calendar now = Calendar.getInstance();
        int today = ConvertDateToValue(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        Calendar cal = Calendar.getInstance();
        Date currTime = new Date();
        currTime.setHours(23);
        currTime.setMinutes(59);

        cal.setTime(currTime);
        long endTime = cal.getTimeInMillis();

        cal.add(Calendar.DAY_OF_YEAR, -today);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.e(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.e(TAG, "Range End: " + dateFormat.format(endTime));

        DataSource ESTIMATED_STEP_DELTAS = new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(ESTIMATED_STEP_DELTAS, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        // [END build_read_data_request]

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                // For the sake of the sample, we'll print the data so we can see what we just
                                // added. In general, logging fitness information should be avoided for privacy
                                // reasons.
                                // [START parse_read_data_result]
                                // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
                                // as buckets containing DataSets, instead of just DataSets.
                                if (dataReadResponse.getBuckets().size() > 0) {

                                    int i = 1;
                                    Log.e(TAG, "Number of returned buckets of DataSets is: " + dataReadResponse.getBuckets().size());
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                            DateFormat dateFormat = getDateInstance();

                                            for (DataPoint dp : dataSet.getDataPoints()) {
                                                Log.e(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                                Log.e(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                                                for (Field field : dp.getDataType().getFields()) {
                                                    entries.add(new BarEntry(i, dp.getValue(field).asInt()));
                                                    Log.e(TAG, "\tField: " + field.getName() +Integer.toString(i) +  " Value: " + dp.getValue(field));
                                                }
                                            }
                                        }
                                        ++i;
                                    }

                                    drawBarGraph();
                                }
                                // [END parse_read_data_result]
                            }
                        }).addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "There was a problem reading the data.", e);
                    }
                });
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
        numSteps = (int)(entries.get(entries.size()-1).getY()) + 1;
        entries.remove(entries.get(entries.size()-1));
        entries.add(new BarEntry(today, numSteps));
        dataSet.removeLast();

        dataSet.addEntry(new BarEntry(today, numSteps));
        chart.setVisibleXRange(7, 7);
        chart.notifyDataSetChanged(); // let the chart know it's data changed
        chart.invalidate(); // refresh

        TvSteps.setText(numSteps + TEXT_NUM_STEPS);
    }

}

package com.example.akina.simplebargraph;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
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
    List<BarEntry> hours = new ArrayList<BarEntry>();
    List<BarEntry> entries = new ArrayList<BarEntry>();
    List<BarEntry> years = new ArrayList<BarEntry>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chart = findViewById(R.id.chart);
        chart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                TextView dateL = findViewById(R.id.leftDate);
                TextView dateR = findViewById(R.id.rightDate);

                dateL.setText(Integer.toString((int)chart.getLowestVisibleX()));
                dateR.setText(Integer.toString((int)chart.getHighestVisibleX()));
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                Calendar now = Calendar.getInstance();
                int today = ConvertDateToValue(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

                switch (position) {
                    case 0:
                        int startIndex = today * 24;
                        refreshGraph(24, 0, startIndex - 1,  7, 0); // For day of week;
                        break;
                    case 1:
                        // For week (today's week beginning Sunday
                        int startDay = today - (today % 7);
                        refreshGraph(7, 0, startDay + 6,  7, 1); // For day of week;
                        break;
                    case 2:
                        int year = determineYear(today);
                        int month = determineMonth(today);
                        int dayOfMonth = determineDayOfMonth(today, month + 12 * (year - 2018));
                        int lastDayOfMonth = getDaysForMonth(month, year);
                        refreshGraph(lastDayOfMonth, /*(today - dayOfMonth + 1)*/0, (today - dayOfMonth) + lastDayOfMonth, 6, 2); // For day of month;
                        break;
                    case 3:
                        int currYear = determineYear(today);
                        int currMonth = determineMonth(today);

                        int monthOfYear = (currYear - 2018) * 12 + currMonth;
                        int startYear = monthOfYear - (monthOfYear % 12);
                        refreshGraph(12, /*startYear*/0, startYear + 11, 12, 3); // For month of year;
                        break;
                }
                //do stuff here
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                Calendar now = Calendar.getInstance();
                int today = ConvertDateToValue(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

                switch (position) {
                    case 0:
                        int startIndex = today * 24;
                        refreshGraph(24, 0, startIndex,  7, 0); // For day of week;
                        break;
                    case 1:
                        // For week (today's week beginning Sunday
                        int startDay = today - (today % 7);
                        refreshGraph(7, 0, startDay + 6,  7, 1); // For day of week;
                        break;
                    case 2:
                        int year = determineYear(today);
                        int month = determineMonth(today);
                        int dayOfMonth = determineDayOfMonth(today, month + 12 * (year - 2018));
                        int lastDayOfMonth = getDaysForMonth(month, year);
                        refreshGraph(lastDayOfMonth, /*(today - dayOfMonth + 1)*/0, (today - dayOfMonth) + lastDayOfMonth, 6, 2); // For day of month;
                        break;
                    case 3:
                        int currYear = determineYear(today);
                        int currMonth = determineMonth(today);

                        int monthOfYear = (currYear - 2018) * 12 + currMonth;
                        int startYear = monthOfYear - (monthOfYear % 12);
                        refreshGraph(12, /*startYear*/0, startYear + 11, 12, 3); // For month of year;
                        break;
                }
            }
        });

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

    public void refreshGraph(int rangeX, int dateStart, int dateEnd, int labelCount, int position) {
        // Modify Dataset
        // Empty dataset
        Calendar now = Calendar.getInstance();
        int today = ConvertDateToValue(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        IAxisValueFormatter xAxisFormatter;
        XAxis xAxis = chart.getXAxis();
        int moveView = 0;

        switch (position)
        {
            case 0: // hours
                dataSet = new BarDataSet(hours, "Label");
                xAxisFormatter = new DayFormatAxis(chart);
                CustomTimeMarkerView markerHour = new CustomTimeMarkerView(this, R.layout.marker);
                chart.setMarker(markerHour);

                dataSet.setBarBorderColor(android.R.color.white);
                dataSet.setBarBorderWidth(1f);
                dataSet.setHighLightAlpha(0);
                dataSet.setDrawValues(false);
                dataSet.setColor(0xFFFF6161);

                data = new BarData(dataSet);
                data.setBarWidth(0.7f);

                chart.setData(data);
                xAxis.setLabelCount(labelCount);
                xAxis.setValueFormatter(xAxisFormatter);
                xAxis.setAxisMinimum(dateStart); // So that first element start in the middle
                xAxis.setAxisMaximum(dateEnd); // So that last element ends in the middle

                moveView = today * 24;
                break;
            case 1: // week
                dataSet = new BarDataSet(entries, "Label");
                xAxisFormatter = new WeekFormatAxis(chart);
                CustomMarkerView markerWeek = new CustomMarkerView(this, R.layout.marker);
                chart.setMarker(markerWeek);

                dataSet.setBarBorderColor(android.R.color.white);
                dataSet.setBarBorderWidth(1f);
                dataSet.setHighLightAlpha(0);
                dataSet.setDrawValues(false);
                dataSet.setColor(0xFFFF6161);

                data = new BarData(dataSet);
                data.setBarWidth(0.7f);

                chart.setData(data);
                xAxis = chart.getXAxis();
                xAxis.setLabelCount(labelCount);
                xAxis.setValueFormatter(xAxisFormatter);
                xAxis.setAxisMinimum(dateStart); // So that first element start in the middle
                xAxis.setAxisMaximum(dateEnd); // So that last element ends in the middle

                moveView = today - (today%7);
                break;
            case 2: // month
                dataSet = new BarDataSet(entries, "Label");
                xAxisFormatter = new MonthFormatAxis(chart);
                CustomMarkerView markerMonth = new CustomMarkerView(this, R.layout.marker);
                chart.setMarker(markerMonth);

                dataSet.setBarBorderColor(android.R.color.white);
                dataSet.setBarBorderWidth(1f);
                dataSet.setHighLightAlpha(0);
                dataSet.setDrawValues(false);
                dataSet.setColor(0xFFFF6161);

                data = new BarData(dataSet);
                data.setBarWidth(0.7f);

                chart.setData(data);
                xAxis = chart.getXAxis();
                xAxis.setLabelCount(labelCount);
                xAxis.setValueFormatter(xAxisFormatter);
                xAxis.setAxisMinimum(dateStart); // So that first element start in the middle
                xAxis.setAxisMaximum(dateEnd); // So that last element ends in the middle

                moveView = today - (int)(chart.getVisibleXRange() / 2);
                break;
            case 3: // year
                Log.e("TOTAL YEARS", Integer.toString(years.size()));
                dataSet = new BarDataSet(years, "Label");
                xAxisFormatter = new YearFormatAxis(chart);
                CustomYearMarkerView markerYear = new CustomYearMarkerView(this, R.layout.marker);
                chart.setMarker(markerYear);

                int currYear = determineYear(today);
                int currMonth = determineMonth(today);

                int monthOfYear = (currYear - 2018) * 12 + currMonth;
                int startYear = monthOfYear - (monthOfYear % 12);

                dataSet.setBarBorderColor(android.R.color.white);
                dataSet.setBarBorderWidth(1f);
                dataSet.setHighLightAlpha(0);
                dataSet.setDrawValues(false);
                dataSet.setColor(0xFFFF6161);

                data = new BarData(dataSet);
                data.setBarWidth(0.7f);

                chart.setData(data);
                xAxis = chart.getXAxis();
                xAxis.setLabelCount(labelCount);
                xAxis.setValueFormatter(xAxisFormatter);
                xAxis.setAxisMinimum(dateStart); // So that first element start in the middle
                xAxis.setAxisMaximum(dateEnd); // So that last element ends in the middle

                moveView = startYear;
                break;
        }


        chart.notifyDataSetChanged();
        chart.invalidate();
        chart.moveViewToX(moveView);
        chart.fitScreen();
        chart.setVisibleXRange(rangeX, rangeX);
    }


    public void drawBarGraph() {
        // This today data is to jump to current day when initialize
        Calendar now = Calendar.getInstance();
        int today = ConvertDateToValue(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        // Get data here
        chart = findViewById(R.id.chart);
        dataSet = new BarDataSet(hours, "Label");
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
        xAxis.setAxisMinimum(0); // So that first element start in the middle
        xAxis.setAxisMaximum(today * 24); // So that last element ends in the middle
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

        CustomTimeMarkerView marker = new CustomTimeMarkerView(this, R.layout.marker);

        // Make graph value be today
        chart.moveViewToX(today*24);

        chart.setMarker(marker);
        chart.fitScreen();
        chart.invalidate();
        chart.setVisibleXRange(24, 24); // Week 7, Month 30, Year 12,

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

        java.text.DateFormat dateFormat = getTimeInstance();
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
                .bucketByTime(1, TimeUnit.HOURS)
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

                                    int i = 0;
                                    int currValue = 0;
                                    int currHourValue = 0;
                                    int currMonth = 1;
                                    Log.e(TAG, "Number of returned buckets of DataSets is: " + dataReadResponse.getBuckets().size());
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                            DateFormat dateFormat = getTimeInstance();
                                            for (DataPoint dp : dataSet.getDataPoints()) {
                                                Log.e(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                                                Log.e(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                                                for (Field field : dp.getDataType().getFields()) {
                                                    hours.add(new BarEntry(i, dp.getValue(field).asInt()));
                                                    currHourValue += dp.getValue(field).asInt();
                                                    currValue += dp.getValue(field).asInt();
                                                    Log.e(TAG, "\tField: " + field.getName() + Integer.toString(i) + " Value: " + dp.getValue(field));
                                                }
                                            }
                                        }
                                        if((i % 24) == 0)
                                        {
                                            int days = (i/24);
                                            entries.add(new BarEntry(days, currHourValue));
                                            int currYear = determineYear(days);
                                            int nextMonth = determineMonth(days + 1);
                                            currHourValue = 0;
                                            if(nextMonth != currMonth)
                                            {
                                                int monthOfYear = (currYear - 2018) * 12 + currMonth;
                                                years.add(new BarEntry(monthOfYear, currValue));
                                                currValue = 0;
                                                currMonth = nextMonth;
                                            }
                                        }
                                        ++i;
                                    }
                                    if (currHourValue != 0)
                                    {
                                        int days = (i/24);
                                        entries.add(new BarEntry(days, currHourValue));
                                    }
                                    if (currValue != 0)
                                    {
                                        int remainingYear = determineYear(i/24 + 1);
                                        int remainingMonth = determineMonth(i/24 + 1);
                                        int monthOfYear = (remainingYear - 2018) * 12 + remainingMonth;
                                        years.add(new BarEntry(monthOfYear, currValue));
                                    }
                                }
                                drawBarGraph();
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


    int ConvertDateToValue(int year, int month, int day) {
        int offsetToCurrYear = 0;
        if (year == 2019)
            offsetToCurrYear = 365;
        else if (year == 2020)
            offsetToCurrYear = 730;
        else if (year == 2021)
            offsetToCurrYear = 1096;

        int offsetToCurrMonth = month * 31;
        if (month > 1) {
            offsetToCurrMonth -= 3;
            if (year == 2020)
                offsetToCurrMonth++;
        }

        if (month >= 4)
            offsetToCurrMonth--;
        if (month >= 6)
            offsetToCurrMonth--;
        if (month >= 9)
            offsetToCurrMonth--;
        if (month >= 11)
            offsetToCurrMonth--;

        return offsetToCurrYear + offsetToCurrMonth + day;
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

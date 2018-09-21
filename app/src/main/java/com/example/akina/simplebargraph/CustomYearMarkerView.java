package com.example.akina.simplebargraph;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.NumberFormat;
import java.util.Locale;

public class CustomYearMarkerView extends MarkerView {

    protected String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private TextView dateTV;
    private TextView distTV;

    public CustomYearMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays a textview
        dateTV = (TextView) findViewById(R.id.date);
        distTV = (TextView) findViewById(R.id.dist);
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {

        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);

        int saveId = canvas.save();
        // translate to the correct position and draw
        canvas.translate(posX + offset.x, offset.y);//posY + offset.y);
        draw(canvas);
        canvas.restoreToCount(saveId);
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        dateTV.setText(getDateStr(e.getX()));
        distTV.setText("" + NumberFormat.getNumberInstance(Locale.US).format((int)e.getY()) + " Steps"); // set the entry-value as the display text
    }

    @Override
    public MPPointF getOffset() {
        // this will center the marker-view horizontally
        return new MPPointF(-(getWidth() / 2),-10f);
    }

    public String getDateStr(float value) {
        int months = (int) value;

        int year = (months/12) + 2018;
        int month = months%12;
        String monthName = mMonths[month % mMonths.length];
        String yearName = String.valueOf(year);
        return monthName + " " + yearName;
    }
}
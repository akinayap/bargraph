package com.example.akina.simplebargraph;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.NumberFormat;
import java.util.Locale;

public class CustomTimeMarkerView extends MarkerView {

    private TextView dateTV;
    private TextView distTV;

    public CustomTimeMarkerView(Context context, int layoutResource) {
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
        dateTV.setText(getTimeStr(e.getX()));
        distTV.setText("" + NumberFormat.getNumberInstance(Locale.US).format((int)e.getY()) + " Steps"); // set the entry-value as the display text
    }


    @Override
    public MPPointF getOffset() {
        // this will center the marker-view horizontally
        return new MPPointF(-(getWidth() / 2),-10f);
    }

    public String getTimeStr(float value) {
        return (int)(value % 24) + ":00 to " + ((int)(value % 24) + 1) + ":00";
    }
}
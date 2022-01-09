package com.harrysoft.androidbluetoothserial.demoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.harrysoft.androidbluetoothserial.demoapp.listviewitems.BarChartItem;
import com.harrysoft.androidbluetoothserial.demoapp.listviewitems.ChartItem;
import com.harrysoft.androidbluetoothserial.demoapp.listviewitems.LineChartItem;
import com.harrysoft.androidbluetoothserial.demoapp.listviewitems.PieChartItem;

import java.util.ArrayList;
import java.util.List;

public class ChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_chart);

        // initialize the utilities
        Utils.init(this);

        ListView lv = findViewById(R.id.listView1);

        ArrayList<ChartItem> list = new ArrayList<>();

        list.add(new LineChartItem(generateDataLine(), getApplicationContext()));
        list.add(new BarChartItem(generateDataBar(), getApplicationContext()));
        list.add(new PieChartItem(generateDataPie(), getApplicationContext(), "簽巡太太"));

        ChartDataAdapter cda = new ChartDataAdapter(getApplicationContext(), list);
        lv.setAdapter(cda);
    }

    /** adapter that supports 3 different item types */
    private class ChartDataAdapter extends ArrayAdapter<ChartItem> {

        ChartDataAdapter(Context context, List<ChartItem> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            //noinspection ConstantConditions
            return getItem(position).getView(position, convertView, getContext());
        }

        @Override
        public int getItemViewType(int position) {
            // return the views type
            ChartItem ci = getItem(position);
            return ci != null ? ci.getItemType() : 0;
        }

        @Override
        public int getViewTypeCount() {
            return 3; // we have 3 different item-types
        }
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Line data
     */
    private LineData generateDataLine() {

        ArrayList<Entry> values1 = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            values1.add(new Entry(i, (int) (Math.random() * 65) + 40));
        }

        LineDataSet d1 = new LineDataSet(values1, "New DataSet 1");
        d1.setLineWidth(2.5f);
        d1.setCircleRadius(1f);
        d1.setHighLightColor(Color.rgb(224, 150, 150));
        d1.setDrawValues(false);

        d1.setFillColor(Color.rgb(110, 200, 200));
        d1.setFillAlpha(50);
        d1.setDrawFilled(true);

        return new LineData(d1);
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Bar data
     */
    private BarData generateDataBar() {

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            entries.add(new BarEntry(i + 1, (int) (Math.random() * 70) + 30));
        }

        BarDataSet d = new BarDataSet(entries, "New DataSet 2");
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);
        d.setHighLightAlpha(255);

        BarData cd = new BarData(d);
        cd.setBarWidth(0.9f);
        return cd;
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Pie data
     */
    private PieData generateDataPie() {

        ArrayList<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry((float) ((Math.random() * 70) + 30), "正常"));
        entries.add(new PieEntry((float) ((Math.random() * 70) + 30), "輕度"));
        entries.add(new PieEntry((float) ((Math.random() * 70) + 30), "中度"));
        entries.add(new PieEntry((float) ((Math.random() * 70) + 30), "重度"));

        PieDataSet d = new PieDataSet(entries, "");

        d.setValueLinePart1OffsetPercentage(80.f);
        d.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        // space between slices
        d.setSliceSpace(2f);
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);

        return new PieData(d);
    }
}
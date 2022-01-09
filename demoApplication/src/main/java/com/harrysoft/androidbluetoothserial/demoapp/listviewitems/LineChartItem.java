
package com.harrysoft.androidbluetoothserial.demoapp.listviewitems;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.harrysoft.androidbluetoothserial.demoapp.R;

public class LineChartItem extends ChartItem {

    private ViewHolder holder;
    private boolean isRunning = false;
    private Handler handler = new Handler();

    //private final Typeface mTf;

    public LineChartItem(ChartData<?> cd, Context c) {
        super(cd);

        //mTf = Typeface.createFromAsset(c.getAssets(), "OpenSans-Regular.ttf");
    }

    private void addData(float inputData) {
        LineData data = holder.chart.getData();//取得原數據
        ILineDataSet set = data.getDataSetByIndex(0);//取得曲線(因為只有一條，故為0，若有多條則需指定)
        data.addEntry(new Entry(set.getEntryCount(), inputData), 0);//新增數據點
        //更新圖表
        data.notifyDataChanged();
        holder.chart.notifyDataSetChanged();
        holder.chart.setVisibleXRange(0, 50);//設置可見範圍
        holder.chart.moveViewToX(data.getEntryCount());//將可視焦點放在最新一個數據，使圖表可移動
    }
    private void startRun() {
        handler.postDelayed(() -> {
            addData((int) (Math.random() * 60) + 40);
            startRun();
        }, 100);
    }

    @Override
    public int getItemType() {
        return TYPE_LINECHART;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, Context c) {

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(c).inflate(
                    R.layout.list_item_linechart, null);
            holder.chart = convertView.findViewById(R.id.chart);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // apply styling
        // holder.chart.setValueTypeface(mTf);
        holder.chart.getDescription().setEnabled(false);
        holder.chart.setDrawGridBackground(false);

        XAxis xAxis = holder.chart.getXAxis();
        //xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setEnabled(false);
        //xAxis.setTypeface(mTf);
        //xAxis.setDrawGridLines(false);
        //xAxis.setDrawAxisLine(true);

        YAxis leftAxis = holder.chart.getAxisLeft();
        //leftAxis.setTypeface(mTf);
        leftAxis.setLabelCount(5, false);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = holder.chart.getAxisRight();
        //rightAxis.setTypeface(mTf);
        rightAxis.setLabelCount(5, false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        // set data
        holder.chart.setData((LineData) mChartData);

        // do not forget to refresh the chart
        // holder.chart.invalidate();
        holder.chart.animateX(1200);
        holder.chart.getLegend().setEnabled(false);

        startRun();

        return convertView;
    }

    private static class ViewHolder {
        LineChart chart;
    }
}

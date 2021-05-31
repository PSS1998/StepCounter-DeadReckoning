package com.example.stepcounter.graph;

import android.content.Entity;
import android.graphics.Color;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import android.graphics.Typeface;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.stepcounter.Vector;
import com.example.stepcounter.graph.footstep.FootStep;

import java.util.ArrayList;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Plotter {
    private final ScatterChart chart;
    private final Color color = Color.valueOf(Color.rgb(0, 30, 0));

    private void initialPlotter() {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(false);
        chart.setMaxHighlightDistance(50f);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setMaxVisibleValueCount(100000);
        chart.setPinchZoom(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setDrawGridLines(false);
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setTypeface(Typeface.DEFAULT);
        l.setXOffset(5f);
        chart.setDrawBorders(false);
    }


    public Plotter(ScatterChart scatterChart) {
        this.chart = scatterChart;
        this.initialPlotter();
    }

    private int generateColor(long timestamp) {
        Date now = new Date();
        float alpha = Math.max(1 - (now.getTime() - timestamp) / 5000f, 0.5f);
        return Color.argb(alpha, 0, color.green(), 0);
    }

    private int calculateBestRadius(int steps) {
        return 30 - Math.min((int) (steps / 10), 15);
    }

    public void plotFootSteps(ArrayList<FootStep> footSteps, Vector center, double farthestFromCenter) {
        this.initialPlotter();
        int radius = calculateBestRadius(footSteps.size());
        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        for(FootStep footStep: footSteps) {
            Entry entry = new Entry(
                    (float)(footStep.getPosition().getX() + center.getX()),
                    (float)(footStep.getPosition().getY() + center.getY())
            );
            ArrayList<Entry> entrySet = new ArrayList<>();
            entrySet.add(entry);
            ScatterDataSet scatterDataSet = new ScatterDataSet(entrySet, "");
            scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
            scatterDataSet.setScatterShapeHoleColor(this.generateColor(footStep.getTime().getTime()));
            scatterDataSet.setColor(this.generateColor(footStep.getTime().getTime()));
            scatterDataSet.setHighlightEnabled(false);
            scatterDataSet.setLabel("");
            scatterDataSet.setScatterShapeSize(radius);
            dataSets.add(scatterDataSet);
        }
        chart.setData(new ScatterData(dataSets));
        chart.invalidate();
    }
}

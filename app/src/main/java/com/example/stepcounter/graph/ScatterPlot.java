package com.example.stepcounter.graph;

import android.content.Context;
import android.graphics.Color;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

public class ScatterPlot {


    private static ArrayList<Double> xList;
    private static ArrayList<Double> yList;
    private static ScatterPlot scatterPlot;
    public ScatterPlot() {

        xList = new ArrayList<>();
        yList = new ArrayList<>();

    }

    public static ScatterPlot getInstance () {
        if (scatterPlot == null) {
            scatterPlot = new ScatterPlot();
            scatterPlot.addPoint(0,0);
        }
//        ArrayList <Point> points = loadPointsFromSharedPreferences();
//        if (points.size() > 0) {
//            HashMap<String, ArrayList<Double>> pointsMap = Point.convertPointListToMap(points);
//            xList = pointsMap.get("pointsX");
//            yList = pointsMap.get("pointsY");
//        }
        return scatterPlot;
    }

    public GraphicalView getGraphView(Context context) {

        XYSeries mySeries;
        XYSeriesRenderer myRenderer;
        XYMultipleSeriesDataset myMultiSeries;
        XYMultipleSeriesRenderer myMultiRenderer;

        //adding the x-axis data from an ArrayList to a standard array
        double[] xSet = new double[xList.size()];
        for (int i = 0; i < xList.size(); i++)
            xSet[i] = xList.get(i);

        //adding the y-axis data from an ArrayList to a standard array
        double[] ySet = new double[yList.size()];
        for (int i = 0; i < yList.size(); i++)
            ySet[i] = yList.get(i);

        //creating a new sequence using the x-axis and y-axis data
        mySeries = new XYSeries("Position");
        for (int i = 0; i < xSet.length; i++)
            mySeries.add(xSet[i], ySet[i]);

        //defining chart visual properties
        myRenderer = new XYSeriesRenderer();
        myRenderer.setFillPoints(true);
        myRenderer.setPointStyle(PointStyle.CIRCLE);
//        myRenderer.setColor(Color.GREEN);
        myRenderer.setColor(Color.parseColor("#ff0099ff"));

        myMultiSeries = new XYMultipleSeriesDataset();
        myMultiSeries.addSeries(mySeries);

        myMultiRenderer = new XYMultipleSeriesRenderer();
        myMultiRenderer.addSeriesRenderer(myRenderer);

        //setting text graph element sizes
        myMultiRenderer.setPointSize(10); //size of scatter plot points
        myMultiRenderer.setShowLegend(false); //hide legend

        //set chart and label sizes
        myMultiRenderer.setChartTitle("Position");
        myMultiRenderer.setChartTitleTextSize(75);
        myMultiRenderer.setLabelsTextSize(40);

        //setting X labels and Y labels position
        int[] chartMargins = {100, 100, 25, 100}; //top, left, bottom, right
        myMultiRenderer.setMargins(chartMargins);
        myMultiRenderer.setYLabelsPadding(50);
        myMultiRenderer.setXLabelsPadding(10);

        //setting chart min/max
        double bound = getMaxBound();
        myMultiRenderer.setXAxisMin(-bound);
        myMultiRenderer.setXAxisMax(bound);
        myMultiRenderer.setYAxisMin(-bound);
        myMultiRenderer.setYAxisMax(bound);

        //returns the graphical view containing the graphz
        return ChartFactory.getScatterChartView(context, myMultiSeries, myMultiRenderer);
    }

    //add a point to the series
    public void addPoint(double x, double y) {
        xList.add(x);
        yList.add(y);
//        savePointInSharePreferences(new Point(x, y));
    }

    public void clearPoints () {
        xList = new ArrayList<>();
        yList = new ArrayList<>();
        addPoint(0, 0);
    }

    public float getLastXPoint() {
        double x = xList.get(xList.size() - 1);
        return (float)x;
    }

    public float getLastYPoint() {
        double y = yList.get(yList.size() - 1);
        return (float)y;
    }

    private double getMaxBound() {
        double max = 0;
        for (double num : xList)
            if (max < Math.abs(num))
                max = num;
        for (double num : yList)
            if (max < Math.abs(num))
                max = num;
        return (Math.abs(max) / 100) * 100 + 100; //rounding up to the nearest hundred
    }

//    public void savePointInSharePreferences (Point point) {
//        Gson gson = new Gson();
//        ArrayList <Point> points = loadPointsFromSharedPreferences();
//        points.add(point);
//        String json = gson.toJson(points);
//        RoutingActivity.editor.putString(RoutingService.routePoints, json);
//        RoutingActivity.editor.apply();
//    }
//
//    public static ArrayList<Point> loadPointsFromSharedPreferences () {
//        Gson gson = new Gson();
//        String json = RoutingActivity.sharedPreferences.getString(RoutingService.routePoints, null);
//        Type type = new TypeToken<ArrayList<Point>>() {}.getType();
//        ArrayList <Point> points  = gson.fromJson(json, type);
//        if (points == null) {
//            points = new ArrayList<Point>();
//        }
//        return points;
//    }

}

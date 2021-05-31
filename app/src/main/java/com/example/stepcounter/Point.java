package com.example.stepcounter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Point {
    private double pointX;
    private double pointY;

    public Point (double x, double y) {
        pointX = x;
        pointY = y;
    }
    public double getPointX() {
        return pointX;
    }

    public double getPointY() {
        return pointY;
    }

    public static HashMap<String, ArrayList<Double>> convertPointListToMap (ArrayList<Point> points) {
        ArrayList <Double> pointsX = new ArrayList<>();
        ArrayList <Double> pointsY = new ArrayList<>();
        for (Point point: points) {
            pointsX.add(point.getPointX());
            pointsY.add(point.getPointY());
        }
        HashMap <String, ArrayList<Double>> pointsMap = new HashMap<String, ArrayList<Double>>();
        pointsMap.put("pointsX", pointsX);
        pointsMap.put("pointsY", pointsY);
        return pointsMap;
    }
}
